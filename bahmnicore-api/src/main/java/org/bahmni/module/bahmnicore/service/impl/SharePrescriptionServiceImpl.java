package org.bahmni.module.bahmnicore.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.contract.SMS.PrescriptionSMS;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniVisitService;
import org.bahmni.module.bahmnicore.service.SMSService;
import org.bahmni.module.bahmnicore.service.SharePrescriptionService;
import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Map;
import java.util.Date;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

import static org.bahmni.module.bahmnicore.util.BahmniDateUtil.convertUTCToGivenFormat;

@Service
public class SharePrescriptionServiceImpl implements SharePrescriptionService {
    private static Logger logger = LogManager.getLogger(BahmniDrugOrderService.class);
    private BahmniVisitService bahmniVisitService;
    private BahmniDrugOrderService drugOrderService;
    private SMSService smsService;

    private final static String SMS_TIMEZONE = "bahmni.sms.timezone";
    private final static String SMS_DATEFORMAT = "bahmni.sms.dateformat";

    @Autowired
    public SharePrescriptionServiceImpl(BahmniVisitService bahmniVisitService, BahmniDrugOrderService drugOrderService, SMSService smsService) {
        this.bahmniVisitService = bahmniVisitService;
        this.drugOrderService = drugOrderService;
        this.smsService = smsService;
    }

    @Override
    @Transactional(readOnly = true)
    @Authorized({"Send Prescription"})
    public Object sendPresciptionSMS(PrescriptionSMS prescription) {
        Visit visit = bahmniVisitService.getVisitSummary(prescription.getVisitUuid());
        Location location = getParentLocationForVisit(visit.getLocation());
        List<BahmniDrugOrder> drugOrderList = drugOrderService.getBahmniDrugOrdersForVisit(visit.getPatient().getUuid(), visit.getUuid());
        List<String> uniqueProviderList = getUniqueProviderNames(drugOrderList);
        String prescriptionString = getPrescriptionAsString(drugOrderList, new Locale(prescription.getLocale()));
        String prescriptionSMSContent = smsService.getPrescriptionMessage(new Locale(prescription.getLocale()), visit.getStartDatetime(), visit.getPatient(),
                location, uniqueProviderList, prescriptionString);
        return smsService.sendSMS(visit.getPatient().getAttribute("phoneNumber").getValue(), prescriptionSMSContent);
    }

    private Location getParentLocationForVisit(Location location) {
        if (location.getParentLocation() != null && isVisitLocation(location.getParentLocation())) {
            return getParentLocationForVisit(location.getParentLocation());
        } else {
            return location;
        }
    }

    private Boolean isVisitLocation(Location location) {
        return (location.getTags().stream().filter(tag -> tag.getName().equalsIgnoreCase("Visit Location")) != null);
    }

    private Map<BahmniDrugOrder, String> getMergedDrugOrderMap(List<BahmniDrugOrder> drugOrderList) {
        Map<BahmniDrugOrder, String> mergedDrugOrderMap = new LinkedHashMap<>();
        for(BahmniDrugOrder drugOrder : drugOrderList) {
            BahmniDrugOrder foundDrugOrder = mergedDrugOrderMap.entrySet().stream()
                    .map(x -> x.getKey())
                    .filter( existingOrder ->
                            compareDrugOrders(existingOrder, drugOrder) )
                    .findFirst()
                    .orElse(null);
            if (foundDrugOrder!=null) {
                String durationWithUnits = mergedDrugOrderMap.get(foundDrugOrder);
                if(drugOrder.getDurationUnits() == foundDrugOrder.getDurationUnits())
                    durationWithUnits = (foundDrugOrder.getDuration()+drugOrder.getDuration()) + " " + drugOrder.getDurationUnits();
                else
                    durationWithUnits += " + " + drugOrder.getDuration() + " " + drugOrder.getDurationUnits();
                mergedDrugOrderMap.put(foundDrugOrder, durationWithUnits);
            } else {
                mergedDrugOrderMap.put(drugOrder, drugOrder.getDuration()+" "+drugOrder.getDurationUnits());
            }
        }
        return mergedDrugOrderMap;
    }

    private boolean compareDrugOrders(BahmniDrugOrder existingOrder, BahmniDrugOrder newDrugOrder) {
        return (existingOrder.getDrug()!=null && newDrugOrder.getDrug()!=null &&
                areValuesEqual(existingOrder.getDrug().getUuid(), newDrugOrder.getDrug().getUuid())) &&
                areValuesEqual(existingOrder.getDrugNonCoded(), newDrugOrder.getDrugNonCoded()) &&
                areValuesEqual(existingOrder.getInstructions(), newDrugOrder.getInstructions()) &&
                compareDosingInstruction(existingOrder.getDosingInstructions(), newDrugOrder.getDosingInstructions()) &&
                areValuesEqual(existingOrder.getDosingInstructions().getRoute(), newDrugOrder.getDosingInstructions().getRoute()) &&
                areValuesEqual(existingOrder.getDosingInstructions().getAdministrationInstructions(), newDrugOrder.getDosingInstructions().getAdministrationInstructions()) &&
                areValuesEqual(existingOrder.getDosingInstructions().getAsNeeded(), newDrugOrder.getDosingInstructions().getAsNeeded()) &&
                areValuesEqual(existingOrder.getDateStopped(), newDrugOrder.getDateStopped()) &&
                getDateDifferenceInDays(existingOrder.getEffectiveStopDate(), newDrugOrder.getEffectiveStartDate()) <= 1.0 ;
    }

    private Boolean areValuesEqual(Object value1, Object value2) {
        if(value1 != null && value2 != null) {
            return value1.equals(value2);
        }
        return (value1 == null && value2 == null);
    };

    private Boolean compareDosingInstruction(EncounterTransaction.DosingInstructions value1, EncounterTransaction.DosingInstructions value2) {
        String doseAndFrequency1 = value1.getDose() + " " + value1.getDoseUnits() + ", " + value1.getFrequency();
        String doseAndFrequency2 = value2.getDose() + " " + value2.getDoseUnits() + ", " + value2.getFrequency();
        return doseAndFrequency1.equals(doseAndFrequency2);
    };

    private double getDateDifferenceInDays(Date date1, Date date2){
        long diff = date2.getTime() - date1.getTime();
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
    }

    public String getPrescriptionAsString(List<BahmniDrugOrder> drugOrders, Locale locale) {
        Map<BahmniDrugOrder, String> drugOrderDurationMap = getMergedDrugOrderMap(drugOrders);
        String prescriptionString = "";
        int counter = 1;
        for (Map.Entry<BahmniDrugOrder, String> entry : drugOrderDurationMap.entrySet()) {
            prescriptionString += counter++ + ". " + getDrugOrderAsString(entry.getKey(), drugOrderDurationMap.get(entry.getKey()), locale) + "\n";
        }
        return prescriptionString;
    }

    public List<String> getUniqueProviderNames(List<BahmniDrugOrder> drugOrders) {
        Set providerSet = new LinkedHashSet();
        for (BahmniDrugOrder drugOrder : drugOrders) {
            providerSet.add("Dr " + drugOrder.getProvider().getName());
        }
        return new ArrayList<>(providerSet);
    }

    private String getDrugOrderAsString(BahmniDrugOrder drugOrder, String duration, Locale locale) {
        String drugOrderString = drugOrder.getDrug().getName();
        drugOrderString += ", " + (drugOrder.getDosingInstructions().getDose().intValue()) + " " + drugOrder.getDosingInstructions().getDoseUnits();
        drugOrderString += ", " + drugOrder.getDosingInstructions().getFrequency() + "-" + duration;
        drugOrderString += ", start from " + convertUTCToGivenFormat(drugOrder.getEffectiveStartDate(),
                Context.getMessageSourceService().getMessage(SMS_DATEFORMAT, null, locale), Context.getMessageSourceService().getMessage(SMS_TIMEZONE, null, locale));
        if(drugOrder.getDateStopped() != null)
            drugOrderString += ", stopped on " + convertUTCToGivenFormat(drugOrder.getDateStopped(),
                    Context.getMessageSourceService().getMessage(SMS_DATEFORMAT, null, locale), Context.getMessageSourceService().getMessage(SMS_TIMEZONE, null, locale));
        return drugOrderString;
    }

}

package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicore.contract.SMS.PrescriptionSMS;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniVisitService;
import org.bahmni.module.bahmnicore.service.SMSService;
import org.bahmni.test.builder.PersonBuilder;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Visit;
import org.openmrs.Location;
import org.openmrs.Person;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
@PowerMockIgnore("javax.management.*")
public class SharePrescriptionServiceImplIT {

    @Mock
    BahmniDrugOrderService bahmniDrugOrderService;
    @Mock
    BahmniVisitService bahmniVisitService;
    @Mock
    SMSService smsService;
    @InjectMocks
    SharePrescriptionServiceImpl sharePrescriptionService;
    @Mock
    AdministrationService administrationService;
    @Mock
    MessageSourceService messageSourceService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
    }

    @Test
    public void shouldCallSendSMSForSendingPrescriptionSMS() throws Exception {
        PrescriptionSMS prescriptionSMS = new PrescriptionSMS();
        prescriptionSMS.setVisitUuid("visit-uuid");
        prescriptionSMS.setLocale("en");
        Visit visit = createVisitForTest();
        String sampleSMSContent = "Date: 30-01-2023\n" +
                "Prescription For Patient: testPersonName null, M, 13 years.\n" +
                "Doctor: Dr Harry (Bahmni)\n" +
                "1. Paracetamol, 2 tab (s), Once a day-5 Days, start from 30-01-2023\n";

        when(administrationService.getGlobalProperty("bahmni.prescriptionSMSTemplate")).thenReturn("Date: {0}\nPrescription For Patient: {1}, {2}, {3} years.\nDoctor: {4} ({5})\n{6}");
        when(messageSourceService.getMessage("bahmni.sms.timezone", null, new Locale("en"))).thenReturn("IST");
        when(messageSourceService.getMessage("bahmni.sms.dateformat", null, new Locale("en"))).thenReturn("dd-MM-yyyy");
        when(messageSourceService.getMessage("bahmni.sms.url", null, new Locale("en"))).thenReturn(null);
        when(bahmniVisitService.getVisitSummary(prescriptionSMS.getVisitUuid())).thenReturn(visit);
        when(bahmniDrugOrderService.getBahmniDrugOrdersForVisit(visit.getPatient().getUuid(), visit.getUuid())).thenReturn(buildBahmniDrugOrderList());
        when(smsService.getPrescriptionMessage(new Locale(prescriptionSMS.getLocale()), visit.getStartDatetime(), visit.getPatient(),
                visit.getLocation(), Arrays.asList("Dr Harry"), "1. Paracetamol, 2 tab (s), Once a day-5 Days, start from 30-01-2023\n"))
                .thenReturn(sampleSMSContent);
        sharePrescriptionService.sendPresciptionSMS(prescriptionSMS);
        verify(smsService, times(1)).sendSMS("+919999999999", sampleSMSContent);
    }

    private Visit createVisitForTest() throws Exception {
        Date visitDate = new SimpleDateFormat("MMMM d, yyyy", new Locale("en")).parse("January 30, 2023");
        Date birthDate = new SimpleDateFormat("MMMM d, yyyy", new Locale("en")).parse("January 30, 2010");
        Person person = new PersonBuilder().withUUID("puuid").withPersonName("testPersonName").build();
        person.setGender("M");
        person.setBirthdate(birthDate);
        PersonAttribute pa = new PersonAttribute();
        pa.setValue("+919999999999");
        PersonAttributeType pat = new PersonAttributeType();
        pat.setName("phoneNumber");
        pa.setAttributeType(pat);
        person.setAttributes(Stream.of(pa).collect(Collectors.toSet()));

        Visit visit = new VisitBuilder().withPerson(person).withUUID("visit-uuid").withStartDatetime(visitDate).build();
        Location location = new Location();
        location.setName("Bahmni");
        visit.setLocation(location);

        return visit;
    }

    private List<BahmniDrugOrder> buildBahmniDrugOrderList() {
        List<BahmniDrugOrder> bahmniDrugOrderList = new ArrayList<>();
        try {
            EncounterTransaction.Provider provider = createETProvider("1", "Harry");
            Date drugOrderStartDate = new SimpleDateFormat("MMMM d, yyyy", new Locale("en")).parse("January 30, 2023");
            EncounterTransaction.DrugOrder etDrugOrder = createETDrugOrder("1", "Paracetamol", 2.0, "Once a day", drugOrderStartDate, 5);
            BahmniDrugOrder bahmniDrugOrder = createBahmniDrugOrder(provider, etDrugOrder);
            bahmniDrugOrderList.add(bahmniDrugOrder);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return bahmniDrugOrderList;
    }

    private BahmniDrugOrder createBahmniDrugOrder(EncounterTransaction.Provider provider, EncounterTransaction.DrugOrder etDrugOrder) {
        BahmniDrugOrder bahmniDrugOrder = new BahmniDrugOrder();
        bahmniDrugOrder.setDrugOrder(etDrugOrder);
        bahmniDrugOrder.setProvider(provider);
        return bahmniDrugOrder;
    }

    private EncounterTransaction.Provider createETProvider(String uuid, String name) {
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(uuid);
        provider.setName(name);
        return provider;
    }

    private EncounterTransaction.DrugOrder createETDrugOrder(String drugUuid, String drugName, Double dose, String frequency, Date effectiveStartDate, Integer duration) {
        EncounterTransaction.Drug encounterTransactionDrug = new EncounterTransaction.Drug();
        encounterTransactionDrug.setUuid(drugUuid);
        encounterTransactionDrug.setName(drugName);

        EncounterTransaction.DosingInstructions dosingInstructions = new EncounterTransaction.DosingInstructions();
        dosingInstructions.setAdministrationInstructions("{\"instructions\":\"As directed\"}");
        dosingInstructions.setAsNeeded(false);
        dosingInstructions.setDose(dose);
        dosingInstructions.setDoseUnits("tab (s)");
        dosingInstructions.setFrequency(frequency);
        dosingInstructions.setNumberOfRefills(0);
        dosingInstructions.setRoute("UNKNOWN");

        EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
        drugOrder.setOrderType("Drug Order");
        drugOrder.setDrug(encounterTransactionDrug);
        drugOrder.setDosingInstructions(dosingInstructions);
        drugOrder.setDuration(duration);
        drugOrder.setDurationUnits("Days");
        drugOrder.setEffectiveStartDate(effectiveStartDate);
        drugOrder.setEffectiveStopDate(DateUtils.addDays(effectiveStartDate, duration));
        drugOrder.setVoided(false);

        return drugOrder;
    }

}

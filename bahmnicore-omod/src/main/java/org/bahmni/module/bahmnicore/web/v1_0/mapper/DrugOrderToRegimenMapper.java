package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.bahmnicoreui.mapper.DoseInstructionMapper;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class DrugOrderToRegimenMapper {


    public TreatmentRegimen map(List<Order> drugOrders, Set<Concept> headersConfig) throws ParseException {
        TreatmentRegimen treatmentRegimen = new TreatmentRegimen();
        Set<Concept> headers = new LinkedHashSet<>();

        for (Order drugOrder : drugOrders) {
            Date stopDate = getDrugStopDate(drugOrder);
            Date startDate = getDrugStartDate(drugOrder);

            treatmentRegimen.addRow(startDate);
            if (stopDate != null) {
                treatmentRegimen.addRow(stopDate);
            }
        }

        for (RegimenRow regimenRow : treatmentRegimen.getRows()) {
            for (Order drugOrder : drugOrders) {
                DrugOrder order = (DrugOrder) drugOrder;
                headers.add(drugOrder.getConcept());
                String newValue = getValueForField(order, regimenRow.getDate());

                if (!newValue.isEmpty()) {
                    String drugConceptName = order.getConcept().getName().getName();

                    String oldValue = regimenRow.getDrugValue(drugConceptName);
                    String value = oldValue.isEmpty() ? newValue : drugStartedOnTheStopDate(oldValue, newValue);
                    regimenRow.addDrugs(drugConceptName, value);
                }
            }
        }
        Set<EncounterTransaction.Concept> headersConcept;
        if (!CollectionUtils.isEmpty(headersConfig))
            headersConcept = mapHeaders(headersConfig);
        else
            headersConcept = mapHeaders(headers);
        treatmentRegimen.setHeaders(headersConcept);

        return treatmentRegimen;
    }

    private Set<EncounterTransaction.Concept> mapHeaders(Set<Concept> headers) {
        Set<EncounterTransaction.Concept> headersConcept = new LinkedHashSet<>();
        for (Concept header : headers) {
            headersConcept.add(new ConceptMapper().map(header));
        }
        return headersConcept;
    }

    private String drugStartedOnTheStopDate(String oldValue, String newValue) {
        if(oldValue.equals("Stop"))
            return newValue;
        if(newValue.equals("Stop"))
            return oldValue;
        return "Error";
    }

    public String getValueForField(DrugOrder drugOrder, Date rowDate) throws ParseException {
        Date startDate = getDrugStartDate(drugOrder);
        Date stopDate = getDrugStopDate(drugOrder);

        if (startDate.equals(rowDate) || startDate.before(rowDate)) {
            if(stopDate == null || (stopDate.after(rowDate))){
                return getDose(drugOrder);
            }

            if (stopDate.equals(rowDate)) {
                return startDate.equals(stopDate) ? "Error" : "Stop";
            }
        }
        return "";
    }

    private Date getOnlyDate(Date date) throws ParseException {
        if (date == null)
            return null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(sdf.format(date));
    }

    private Date getDrugStartDate(Order drugOrder) throws ParseException {

        return drugOrder.getScheduledDate() != null ?
                getOnlyDate(drugOrder.getScheduledDate()) :
                getOnlyDate(drugOrder.getDateActivated());
    }

    private Date getDrugStopDate(Order drugOrder) throws ParseException {
        return drugOrder.getDateStopped() != null ?
                getOnlyDate(drugOrder.getDateStopped()) :
                getOnlyDate(drugOrder.getAutoExpireDate());
    }

    private String getDose(DrugOrder drugOrder) {
        String dosage = null;
        if (drugOrder.getFrequency() == null) {
            try {
                dosage = DoseInstructionMapper.getFrequency(drugOrder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            if (drugOrder.getDose() != null) {
                dosage = drugOrder.getDose().toString();
            } else {
                dosage = "";
            }
        }
        return dosage;
    }

}

package org.bahmni.module.admin.observation.handler;


import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.observation.CSVObservationHelper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Component
public class Form1CSVObsHandler implements CSVObsHandler {

    private CSVObservationHelper csvObservationHelper;

    @Autowired
    public Form1CSVObsHandler(CSVObservationHelper csvObservationHelper) {
        this.csvObservationHelper = csvObservationHelper;
    }

    @Override
    public List<KeyValue> getRelatedCSVObs(EncounterRow encounterRow) {
        return encounterRow.obsRows.stream().filter(csvObservation -> csvObservationHelper.isForm1Type(csvObservation))
                .collect(Collectors.toList());
    }

    @Override
    public List<EncounterTransaction.Observation> handle(EncounterRow encounterRow) throws ParseException {
        List<EncounterTransaction.Observation> observations = new ArrayList<>();
        List<KeyValue> csvObservations = getRelatedCSVObs(encounterRow);
        for (KeyValue csvObservation : csvObservations) {
            if (isNotBlank(csvObservation.getValue())) {
                List<String> conceptNames = csvObservationHelper.getCSVHeaderParts(csvObservation);
                csvObservationHelper.verifyNumericConceptValue(csvObservation, conceptNames);
                csvObservationHelper.createObservations(observations, encounterRow.getEncounterDate(),
                        csvObservation, conceptNames);
            }
        }
        return observations;
    }

    @Override
    public List<EncounterTransaction.Observation> handle(EncounterRow encounterRow, boolean shouldPerformForm2Validations) throws ParseException {
        return null;
    }

}

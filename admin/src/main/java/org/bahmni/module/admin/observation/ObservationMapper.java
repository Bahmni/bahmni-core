package org.bahmni.module.admin.observation;

import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.observation.handler.CSVObsHandler;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Component(value = "adminObservationMapper")
public class ObservationMapper {

    @Autowired
    private List<CSVObsHandler> csvObsHandlers;

    @Autowired
    @Deprecated
    public ObservationMapper(ConceptService conceptService) {
    }

    public List<EncounterTransaction.Observation> getObservations(EncounterRow encounterRow) throws ParseException {
        final List<EncounterTransaction.Observation> observations = new ArrayList<>();
        for (CSVObsHandler csvObsHandler : csvObsHandlers) {
            observations.addAll(csvObsHandler.handle(encounterRow));
        }
        return observations;
    }

    public List<EncounterTransaction.Observation> getObservations(EncounterRow encounterRow, boolean shouldPerformForm2Validations) throws ParseException {
        final List<EncounterTransaction.Observation> observations = new ArrayList<>();
        for (CSVObsHandler csvObsHandler : csvObsHandlers) {
            final List<EncounterTransaction.Observation> allObs = csvObsHandler.handle(encounterRow, shouldPerformForm2Validations);
            if(allObs != null)
                observations.addAll(allObs);
        }
        return observations;
    }
}

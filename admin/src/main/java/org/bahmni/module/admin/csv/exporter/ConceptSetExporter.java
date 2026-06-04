package org.bahmni.module.admin.csv.exporter;

import org.bahmni.module.admin.concepts.mapper.ConceptSetMapper;
import org.bahmni.module.admin.csv.models.ConceptRows;
import org.bahmni.module.referencedata.labconcepts.contract.Concepts;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptService;
import org.openmrs.api.APIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConceptSetExporter {

    @Autowired
    private ReferenceDataConceptService conceptService;
    private final ConceptSetMapper conceptSetMapper;

    public ConceptSetExporter() {
        conceptSetMapper = new ConceptSetMapper();
    }

    public ConceptRows exportConcepts(String conceptName) {
        Concepts conceptSet = conceptService.getConcept(conceptName);
        if (conceptSet == null) {
            throw new APIException("Concept " + conceptName + " not found");
        }
        ConceptRows conceptRows = conceptSetMapper.mapAll(conceptSet);
        conceptRows.makeCSVReady();
        return conceptRows;
    }
}
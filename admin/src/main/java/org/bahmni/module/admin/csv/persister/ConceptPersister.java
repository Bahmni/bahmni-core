package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.concepts.mapper.ConceptMapper;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConceptPersister implements EntityPersister<ConceptRow> {

    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;

    @Override
    public Messages validate(ConceptRow conceptRow) {
        Messages messages = new Messages();
        if (StringUtils.isEmpty(conceptRow.name)) {
            messages.add("Concept Name not specified\n");
        }
        if (StringUtils.isEmpty(conceptRow.conceptClass)) {
            messages.add("Concept Class not specified\n");
        }
        return messages;
    }

    @Override
    public Messages persist(ConceptRow conceptRow) {
        Concept concept = new ConceptMapper().map(conceptRow);
        referenceDataConceptService.saveConcept(concept);
        return new Messages();
    }
}

package org.bahmni.module.admin.csv.persister;


import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.concepts.mapper.ConceptSetMapper;
import org.bahmni.module.admin.csv.models.ConceptRows;
import org.bahmni.module.admin.csv.models.ConceptSetRow;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ConceptSetPersister implements EntityPersister<ConceptSetRow> {

    @Autowired
    private ReferenceDataConceptService referenceDataConceptService;



    @Override
    public Messages validate(ConceptSetRow conceptSetRow) {
        Messages messages = new Messages();
        if (StringUtils.isEmpty(conceptSetRow.name)) {
            messages.add("Concept Name not specified\n");
        }
        if (StringUtils.isEmpty(conceptSetRow.conceptClass)) {
            messages.add("Concept Class not specified\n");
        }
        for (KeyValue conceptKeyValue: conceptSetRow.getChildren()) {
            if(conceptKeyValue.getValue().equals(conceptSetRow.getName())){
                messages.add("Concept introduces cycle\n");
            }
        }

        return messages;

    }

    private boolean createsCycle(ConceptSetRow newConceptSetRow){

        List<String> descendantNames = getAllDescendantsNames(newConceptSetRow);
        for (String descendantName: descendantNames) {
            if(descendantName.equals(newConceptSetRow.getName())){
                return true;
            }
        }
        return false;
    }

    private List<String> getAllDescendantsNames(ConceptSetRow newConceptSetRow) {
        List<String> descendants = new ArrayList<>();

        List<ConceptRows> conceptRowsCollection = new ArrayList<>();
        for (KeyValue concept: newConceptSetRow.getChildren()) {
            if(StringUtils.isNotEmpty(concept.getValue())) {
                conceptRowsCollection.add(new ConceptSetMapper().mapAll(referenceDataConceptService.getConcept(concept.getValue())));
            }
        }
        for (ConceptRows conceptRows: conceptRowsCollection) {
            for (ConceptSetRow conceptSetRow: conceptRows.getConceptSetRows()) {
                descendants.add(conceptSetRow.getName());
            }
        }
        return descendants;
    }


    @Override
    public Messages persist(ConceptSetRow conceptSetRow) {
        Messages messages = new Messages();
        if(createsCycle(conceptSetRow)){
            messages.add("Concept Set introduces cycle\n");
            return messages;
        }

        ConceptSet concept = new ConceptSetMapper().map(conceptSetRow);
        referenceDataConceptService.saveConcept(concept);
        return new Messages();
    }

}

package org.bahmni.module.admin.csv.persister;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.ConceptReferenceTermRow;
import org.bahmni.module.admin.csv.models.FormerConceptReferenceRow;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptReferenceTermService;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;

@Service
public class ConceptReferenceTermPersister implements EntityPersister<FormerConceptReferenceRow> {
    private static final Logger log = LogManager.getLogger(ConceptReferenceTermPersister.class);

    @Autowired
    private ReferenceDataConceptReferenceTermService referenceTermService;

    @Autowired
    private ConceptService conceptService;


    private UserContext userContext;

    public void init(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public Messages persist(FormerConceptReferenceRow formerConceptReferenceRow) {
        Concept concept = conceptService.getConceptByName(formerConceptReferenceRow.getConceptName());
        concept.getConceptMappings().addAll(getNewConceptMappings(formerConceptReferenceRow.getReferenceTerms(), concept));
        conceptService.saveConcept(concept);

        return new Messages();
    }

    private List<ConceptMap> getNewConceptMappings(List<ConceptReferenceTermRow> referenceTerms, Concept concept) {
        return referenceTerms.stream().map(termRow -> getConceptMap(concept, termRow)).collect(Collectors.toList());
    }

    private ConceptMap getConceptMap(Concept concept, ConceptReferenceTermRow termRow) {
        String code = termRow.getReferenceTermCode();
        String source = termRow.getReferenceTermSource();

        ConceptReferenceTerm conceptReferenceTerm = referenceTermService.getConceptReferenceTerm(code, source);
        ConceptMap conceptMap = new ConceptMap();
        conceptMap.setConceptReferenceTerm(conceptReferenceTerm);
        conceptMap.setConcept(concept);
        conceptMap.setConceptMapType(conceptService.getConceptMapTypeByName(termRow.getReferenceTermRelationship()));
        return conceptMap;
    }

    @Override
    public Messages validate(FormerConceptReferenceRow formerConceptReferenceRow) {
        Messages messages = new Messages();
        Context.openSession();
        Context.setUserContext(userContext);

        String conceptName = formerConceptReferenceRow.getConceptName();
        if (isNull(conceptService.getConceptByName(conceptName)))
            messages.add(String.format("%s concept is not present", conceptName));

        formerConceptReferenceRow.getReferenceTerms().forEach(termRow -> {
            String code = termRow.getReferenceTermCode();
            String source = termRow.getReferenceTermSource();

            try {
                referenceTermService.getConceptReferenceTerm(code, source);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                messages.add(String.format("%s reference term code is not present in %s source", code, source));
            }
            if (isNull(conceptService.getConceptMapTypeByName(termRow.getReferenceTermRelationship())))
                messages.add(String.format("%s concept map type is not present", termRow.getReferenceTermRelationship()));

        });

        Context.flushSession();
        Context.closeSession();

        return messages;
    }
}

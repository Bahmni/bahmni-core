package org.openmrs.module.bahmniemrapi.disposition.mapper;

import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.Order;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.Locale;

@Component
public class BahmniDispositionMapper {

    private static final String EMRAPI_CONCEPT_SOURCE_NAME = "org.openmrs.module.emrapi";

    private ConceptService conceptService;

    @Autowired
    public BahmniDispositionMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    /**
     * Maps an ADT Order (the new clinic's workflow-tracking counterpart to the legacy
     * Disposition Observation) to the same BahmniDisposition contract the old clinic's
     * Observation-based dispositions produce, so both can be rendered interchangeably.
     */
    public BahmniDisposition mapFromOrder(Order order, Set<EncounterTransaction.Provider> providers, Locale locale) {
        BahmniDisposition bahmniDisposition = new BahmniDisposition();
        Concept concept = order.getConcept();
        bahmniDisposition.setCode(getEmrapiMappingCode(concept));
        bahmniDisposition.setConceptName(concept.getUuid());
        bahmniDisposition.setDispositionDateTime(order.getEffectiveStartDate());
        bahmniDisposition.setVoided(order.isVoided());
        bahmniDisposition.setVoidReason(order.getVoidReason());
        bahmniDisposition.setExistingObs(order.getUuid());
        bahmniDisposition.setProviders(providers);
        if (order.getCreator() != null) {
            bahmniDisposition.setCreatorName(order.getCreator().getPersonName().toString());
        }
        if (concept.getPreferredName(locale) != null) {
            bahmniDisposition.setPreferredName(concept.getPreferredName(locale).getName());
        }
        return bahmniDisposition;
    }

    private String getEmrapiMappingCode(Concept concept) {
        if (concept.getConceptMappings() == null) {
            return null;
        }
        for (ConceptMap conceptMap : concept.getConceptMappings()) {
            if (conceptMap.getConceptReferenceTerm() == null
                    || conceptMap.getConceptReferenceTerm().getConceptSource() == null) {
                continue;
            }
            if (EMRAPI_CONCEPT_SOURCE_NAME.equals(conceptMap.getConceptReferenceTerm().getConceptSource().getName())) {
                return conceptMap.getConceptReferenceTerm().getCode();
            }
        }
        return null;
    }

    public BahmniDisposition map(EncounterTransaction.Disposition disposition, Set<EncounterTransaction.Provider> providers, User user , Locale locale){
        BahmniDisposition bahmniDisposition = new BahmniDisposition();
        bahmniDisposition.setAdditionalObs(disposition.getAdditionalObs());
        bahmniDisposition.setCode(disposition.getCode());
        bahmniDisposition.setConceptName(disposition.getConceptName());
        bahmniDisposition.setDispositionDateTime(disposition.getDispositionDateTime());
        bahmniDisposition.setVoided(disposition.isVoided());
        bahmniDisposition.setExistingObs(disposition.getExistingObs());
        bahmniDisposition.setVoidReason(disposition.getVoidReason());
        bahmniDisposition.setProviders(providers);
        bahmniDisposition.setCreatorName(user.getPersonName().toString());
        Concept concept = conceptService.getConcept(disposition.getConceptName());
        if(concept != null && concept.getPreferredName(locale) != null) {
            bahmniDisposition.setPreferredName(concept.getPreferredName(locale).getName());
        }
        return bahmniDisposition;
    }

}

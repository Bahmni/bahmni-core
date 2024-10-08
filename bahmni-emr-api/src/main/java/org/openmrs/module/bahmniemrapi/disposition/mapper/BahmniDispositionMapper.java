package org.openmrs.module.bahmniemrapi.disposition.mapper;

import org.openmrs.Concept;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.Locale;

@Component
public class BahmniDispositionMapper {

    private ConceptService conceptService;

    @Autowired
    public BahmniDispositionMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
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
        if (concept == null && !LocaleUtility.getDefaultLocale().equals(locale)) {
            List<Concept> conceptsByName = Context.getConceptService().getConceptsByName(disposition.getConceptName(), LocaleUtility.getDefaultLocale(), false);
            if (!conceptsByName.isEmpty()) {
                concept = conceptsByName.get(0);
            }
        }
        if(concept != null && concept.getPreferredName(locale) != null) {
            bahmniDisposition.setPreferredName(concept.getPreferredName(locale).getName());
        }
        return bahmniDisposition;
    }

}

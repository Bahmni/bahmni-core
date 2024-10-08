package org.bahmni.module.bahmnicore.util;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.util.LocaleUtility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MiscUtils {
    public static List<Concept> getConceptsForNames(List<String> conceptNames, ConceptService conceptService) {
        //Returning null for the sake of UTs
        if (CollectionUtils.isNotEmpty(conceptNames)) {
            List<Concept> rootConcepts = new ArrayList<>();
            for (String rootConceptName : conceptNames) {
                Concept concept = conceptService.getConceptByName(rootConceptName);
                if (concept == null) {
                    concept = getConceptInDefaultLocale(conceptService, rootConceptName);
                }
                if (concept != null) {
                    rootConcepts.add(concept);
                }
            }
            return rootConcepts;
        }
        return new ArrayList<>();
    }
    private static Concept getConceptInDefaultLocale(ConceptService conceptService, String rootConceptName) {
        if (LocaleUtility.getDefaultLocale().equals(Context.getLocale())) {
            return null;
        }
        List<Concept> conceptsByName = conceptService.getConceptsByName(rootConceptName, LocaleUtility.getDefaultLocale(), false);
        for (Concept concept : conceptsByName) {
            for (ConceptName conceptname : concept.getNames()) {
                if (conceptname.getName().equalsIgnoreCase(rootConceptName) && (conceptname.isPreferred() || conceptname.isFullySpecifiedName())) {
                    return concept;
                }
            }
        }
        return null;
    }

    public static void setUuidsForObservations(Collection<BahmniObservation> bahmniObservations) {
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            if (org.apache.commons.lang3.StringUtils.isBlank(bahmniObservation.getUuid())) {
                bahmniObservation.setUuid(UUID.randomUUID().toString());
            }
        }
    }
	
	public static boolean onlyDigits(String input) {
		Pattern onlyDigits = Pattern.compile("\\d+");
		return onlyDigits.matcher(input).matches();
	}
}

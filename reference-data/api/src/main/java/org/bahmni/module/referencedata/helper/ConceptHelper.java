package org.bahmni.module.referencedata.helper;

import java.util.Locale;
import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.referencedata.contract.ConceptDetails;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.ETObsToBahmniObsMapper;
import org.openmrs.module.emrapi.utils.HibernateLazyLoader;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class ConceptHelper {
    private ConceptService conceptService;

    @Autowired
    public ConceptHelper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }


    public List<Concept> getConceptsForNames(Collection<String> conceptNames) {
        List<Concept> concepts = new ArrayList<>();
        if (conceptNames != null) {
            for (String conceptName : conceptNames) {
                List<Concept> conceptsByName = conceptService.getConceptsByName(conceptName.replaceAll("%20", " "));
                if (CollectionUtils.isNotEmpty(conceptsByName)) {
                    for (Concept concept : conceptsByName) {
                        for (ConceptName conceptNameObj : concept.getNames()) {
                            if (conceptNameObj.getName().equalsIgnoreCase(conceptName) && conceptNameObj.isFullySpecifiedName()) {
                                concepts.add(concept);
                                break;
                            }
                        }
                    }
                }
            }
        }
        return concepts;
    }

    public Set<ConceptDetails> getLeafConceptDetails(List<Concept> obsConcepts, boolean withoutAttributes) {
        if (obsConcepts != null && !obsConcepts.isEmpty()) {
            Set<ConceptDetails> leafConcepts = new LinkedHashSet<>();
            for (Concept concept : obsConcepts) {
                addLeafConcepts(concept, null, leafConcepts, withoutAttributes);
            }
            return leafConcepts;
        }
        return Collections.EMPTY_SET;
    }

    protected void addLeafConcepts(Concept rootConcept, Concept parentConcept, Set<ConceptDetails> leafConcepts, boolean withoutAttributes) {
        if (rootConcept != null) {
            if (rootConcept.isSet()) {
                for (Concept setMember : rootConcept.getSetMembers()) {
                    addLeafConcepts(setMember, rootConcept, leafConcepts, withoutAttributes);
                }
            } else if (!shouldBeExcluded(rootConcept)) {
                Concept conceptToAdd = rootConcept;
                if (parentConcept != null && !withoutAttributes && hasConceptDetailsClass(parentConcept)) {
                    conceptToAdd = parentConcept;
                }
                ConceptDetails conceptDetails = createConceptDetails(conceptToAdd);
                addAttributes(conceptDetails, parentConcept);
                leafConcepts.add(conceptDetails);
            }
        }
    }

    private void addAttributes(ConceptDetails conceptDetails, Concept parentConcept) {
        if (parentConcept != null && hasConceptDetailsClass(parentConcept)) {
            for (Concept concept : parentConcept.getSetMembers()) {
                if ("Unknown".equals(concept.getConceptClass().getName())) {
                    conceptDetails.addAttribute("Unknown Concept", getConceptName(concept, ConceptNameType.FULLY_SPECIFIED));
                }
                if ("Abnormal".equals(concept.getConceptClass().getName())) {
                    conceptDetails.addAttribute("Abnormal Concept", getConceptName(concept, ConceptNameType.FULLY_SPECIFIED));
                }
            }
        }

    }

    private boolean hasConceptDetailsClass(Concept parentConcept) {
        return ETObsToBahmniObsMapper.CONCEPT_DETAILS_CONCEPT_CLASS.equals(parentConcept.getConceptClass().getName());
    }

    private ConceptDetails createConceptDetails(Concept conceptToAdd) {
        Concept concept = new HibernateLazyLoader().load(conceptToAdd);
        String fullNameInLocale = getConceptNameInLocale(concept, ConceptNameType.FULLY_SPECIFIED, false);
        String shortNameInLocale = getConceptNameInLocale(concept, ConceptNameType.SHORT, false);
        String conceptFullName = (fullNameInLocale != null) ? fullNameInLocale : getConceptNameInLocale(concept, ConceptNameType.FULLY_SPECIFIED, true);
        String conceptShortName = (shortNameInLocale != null) ? shortNameInLocale : fullNameInLocale;
        if (conceptShortName == null) {
            String defaultLocaleShortName = getConceptNameInLocale(concept, ConceptNameType.SHORT, true);
            conceptShortName = (defaultLocaleShortName != null) ? defaultLocaleShortName : conceptFullName;
        }
        ConceptDetails conceptDetails = new ConceptDetails();
        conceptDetails.setName(conceptShortName);
        conceptDetails.setFullName(conceptFullName);
        if (concept.isNumeric()) {
            ConceptNumeric numericConcept = (ConceptNumeric) concept;
            conceptDetails.setUnits(numericConcept.getUnits());
            conceptDetails.setHiNormal(numericConcept.getHiNormal());
            conceptDetails.setLowNormal(numericConcept.getLowNormal());
        }
        return conceptDetails;
    }

    private String getConceptName(Concept concept, ConceptNameType conceptNameType){
        String conceptNameInLocale = getConceptNameInLocale(concept, conceptNameType, false);
        return (conceptNameInLocale != null) ? conceptNameInLocale : getConceptNameInLocale(concept, conceptNameType, true);
    }

    private String getConceptNameInLocale(Concept concept, ConceptNameType conceptNameType, boolean isDefaultLocale) {
        Locale locale;
        locale = isDefaultLocale ? LocaleUtility.getDefaultLocale() :
                LocaleUtility.fromSpecification(Context.getAuthenticatedUser().getUserProperty("defaultLocale"));
        ConceptName conceptName = concept.getName(locale, conceptNameType, null);
        return (conceptName != null) ? conceptName.getName() : null;
    }

    private boolean shouldBeExcluded(Concept rootConcept) {
        return ETObsToBahmniObsMapper.ABNORMAL_CONCEPT_CLASS.equals(rootConcept.getConceptClass().getName()) ||
                ETObsToBahmniObsMapper.DURATION_CONCEPT_CLASS.equals(rootConcept.getConceptClass().getName()) ||
                ETObsToBahmniObsMapper.UNKNOWN_CONCEPT_CLASS.equals(rootConcept.getConceptClass().getName());
    }

    public Set<ConceptDetails> getConceptDetails(List<Concept> conceptNames) {
        Set<ConceptDetails> conceptDetails = new LinkedHashSet<>();
        for (Concept concept : conceptNames) {
            if (concept != null) {
                conceptDetails.add(createConceptDetails(concept));
            }
        }
        return conceptDetails;
    }

    public Set<Integer> getConceptIds(List<Concept> conceptNames) {
        Set<Integer> conceptIds = new LinkedHashSet<>();
        for (Concept concept : conceptNames) {
            if (concept != null) {
                conceptIds.add(concept.getConceptId());
            }
        }
        return conceptIds;
    }

    public List<Concept> getParentConcepts(Concept concept) {
        return conceptService.getConceptsByAnswer(concept);
    }

    public Set<String> getChildConceptNames(List<Concept> conceptsForNames) {
        Set<String> conceptDetails = new LinkedHashSet<>();
        getConceptNames(conceptDetails, conceptsForNames);
        return conceptDetails;
    }

    private void getConceptNames(Set<String> conceptDetails, List<Concept> concepts) {
        for (Concept concept : concepts) {
            if (!concept.isRetired()) {
                conceptDetails.add(getConceptName(concept, ConceptNameType.FULLY_SPECIFIED));
            }
            getConceptNames(conceptDetails, concept.getSetMembers());
        }
    }

    public Set<org.bahmni.module.referencedata.contract.ConceptName> getLeafConceptNames(List<Concept> concepts) {
        Set<org.bahmni.module.referencedata.contract.ConceptName> leafConcepts = new LinkedHashSet<>();
        getLeafConceptName(leafConcepts, concepts);
        return leafConcepts;
    }

    private void getLeafConceptName(Set<org.bahmni.module.referencedata.contract.ConceptName> leafConcepts, List<Concept> concepts) {
        for (Concept concept : concepts) {
            if (!concept.isSet() && !concept.isRetired()) {
                String fullySpecifiedName = getConceptName(concept, ConceptNameType.FULLY_SPECIFIED);
                String shortName = getConceptName(concept, ConceptNameType.SHORT);
                leafConcepts.add(new org.bahmni.module.referencedata.contract.ConceptName(fullySpecifiedName, shortName));
            } else if (concept.isSet() && !concept.isRetired()) {
                getLeafConceptName(leafConcepts, concept.getSetMembers());
            }
        }
    }
}

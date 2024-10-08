package org.bahmni.module.referencedata.labconcepts.model.event;

import org.ict4h.atomfeed.server.service.Event;
import java.time.LocalDateTime;
import org.openmrs.Concept;
import org.openmrs.ConceptAttribute;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static org.bahmni.module.referencedata.labconcepts.contract.AllSamples.ALL_SAMPLES;
import static org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels.ALL_TESTS_AND_PANELS;
import static org.bahmni.module.referencedata.labconcepts.contract.Department.DEPARTMENT_CONCEPT_CLASS;
import static org.bahmni.module.referencedata.labconcepts.contract.LabTest.LAB_TEST_CONCEPT_CLASSES;
import static org.bahmni.module.referencedata.labconcepts.contract.Panel.LAB_SET_CONCEPT_CLASS;
import static org.bahmni.module.referencedata.labconcepts.contract.RadiologyTest.RADIOLOGY_TEST_CONCEPT_CLASSES;
import static org.bahmni.module.referencedata.labconcepts.contract.Sample.SAMPLE_CONCEPT_CLASSES;

public class SaleableTypeEvent implements ConceptServiceOperationEvent {

    public static final String RESOURCE_TITLE = "reference data";
    public static final String SALEABLE_ATTR_NAME = "saleable";
    public static final String RESOURCES = "resources";
    private final String url;
    private final String category;
    private List<String> supportedOperations = Arrays.asList("saveConcept", "updateConcept", "retireConcept", "purgeConcept");

    private List<String> unhandledClasses = new ArrayList<String>(){{
        addAll(Arrays.asList(LAB_SET_CONCEPT_CLASS, DEPARTMENT_CONCEPT_CLASS));
        addAll(LAB_TEST_CONCEPT_CLASSES);
        addAll(RADIOLOGY_TEST_CONCEPT_CLASSES);
        addAll(SAMPLE_CONCEPT_CLASSES);
    }};
    private List<String> unhandledConcepsByName = Arrays.asList(ALL_SAMPLES, ALL_TESTS_AND_PANELS);

    public SaleableTypeEvent(String url, String category) {
        this.url = url;
        this.category = category;
    }

    @Override
    public Event asAtomFeedEvent(Object[] arguments) throws URISyntaxException {
        Concept concept = (Concept) arguments[0];
        String url = String.format(this.url, RESOURCES, concept.getUuid());
        return new Event(UUID.randomUUID().toString(), RESOURCE_TITLE, LocalDateTime.now(), new URI(url), url, this.category);
    }

    @Override
    public Boolean isApplicable(String operation, Object[] arguments) {
        if (supportedOperations.contains(operation)
                && arguments.length > 0 && arguments[0] instanceof Concept) {
            Concept concept = (Concept) arguments[0];
            if (!shouldRaiseEvent(concept)) {
                return false;
            }
            Collection<ConceptAttribute> activeAttributes = concept.getActiveAttributes();
            return activeAttributes.stream().filter(a -> a.getAttributeType().getName().equalsIgnoreCase(SALEABLE_ATTR_NAME)).findFirst().isPresent();
        }
        return false;
    }

    private boolean shouldRaiseEvent(Concept concept) {
        boolean result = unhandledClasses.stream().anyMatch(concept.getConceptClass().getName()::equalsIgnoreCase);
        if (result) {
            return false;
        }
        ConceptName conceptName = concept.getName(Context.getLocale());
        if (conceptName != null) {
            return !unhandledConcepsByName.stream().anyMatch(conceptName.getName()::equalsIgnoreCase);
        }

        return true;
    }
}

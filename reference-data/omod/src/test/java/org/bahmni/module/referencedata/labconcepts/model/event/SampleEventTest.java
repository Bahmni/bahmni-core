package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.bahmni.module.referencedata.labconcepts.model.Operation;
import org.bahmni.test.builder.ConceptBuilder;
import org.ict4h.atomfeed.server.service.Event;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.getConceptSets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class SampleEventTest {
    public static final String SAMPLE_CONCEPT_UUID = "aebc57b7-0683-464e-ac48-48b8838abdfc";

    public static final String SPECIMEN_CONCEPT_UUID = "aebc57b7-0683-464e-ac48-48b8838abdff";

    private Concept concept;

    private Concept specimenConcept;

    @Mock
    private ConceptService conceptService;
    private Concept parentConcept;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        concept = new ConceptBuilder().withClass("Sample").withUUID(SAMPLE_CONCEPT_UUID).build();

        specimenConcept = new ConceptBuilder().withClass("Specimen").withUUID(SPECIMEN_CONCEPT_UUID).build();

        parentConcept = new ConceptBuilder().withName(AllSamples.ALL_SAMPLES).withSetMember(concept).build();

        List<ConceptSet> conceptSets = getConceptSets(parentConcept, concept);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);

        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);
    }


    @Test
    public void createEventForSampleEvent() throws Exception {
        Event event = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept}).get(0);
        Event anotherEvent = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept}).get(0);
        assertNotNull(event);
        assertFalse(event.getUuid().equals(anotherEvent.getUuid()));
        assertEquals(event.getTitle(), "sample");
        assertEquals(event.getCategory(), "lab");
    }

    @Test
    public void createSampleEventForSpecimenConcept() throws Exception {
        Event event = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{specimenConcept}).get(0);
        Event anotherEvent = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{specimenConcept}).get(0);
        assertNotNull(event);
        assertFalse(event.getUuid().equals(anotherEvent.getUuid()));
        assertEquals(event.getTitle(), "sample");
        assertEquals(event.getCategory(), "lab");
    }

    @Test
    public void shouldNotCreateEventForSampleEventIfThereIsDifferentConceptClass() throws Exception {
        concept = new ConceptBuilder().withClass("procedure").withClassUUID("some").withUUID(SAMPLE_CONCEPT_UUID).build();
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept});
        assertTrue(events.isEmpty());
    }

    @Test
    public void shouldCreateEventForSampleEventIfParentConceptIsMissing() throws Exception {
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(new ArrayList<ConceptSet>());
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept});
        assertNotNull(events.get(0));
        assertEquals(events.get(0).getTitle(), "sample");
        assertEquals(events.get(0).getCategory(), "lab");
    }


    @Test
    public void shouldCreateEventForSampleEventIfParentConceptIsWrong() throws Exception {
        parentConcept = new ConceptBuilder().withName("Some wrong name").withSetMember(concept).build();
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(getConceptSets(parentConcept, concept));
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{concept});
        assertNotNull(events.get(0));
        assertEquals(events.get(0).getTitle(), "sample");
        assertEquals(events.get(0).getCategory(), "lab");
    }

    @Test
    public void createEventForSampleWithParentConceptMissing() throws Exception {
        Concept sampleConcept = new ConceptBuilder().withClass("Sample").withUUID("SampleUUID").build();
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{sampleConcept});
        Event event = events.get(0);
        assertNotNull(event);
        assertEquals(event.getTitle(), ConceptServiceEventFactory.SAMPLE);
        assertEquals(event.getCategory(), ConceptServiceEventFactory.LAB);
    }

}

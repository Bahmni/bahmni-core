package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
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
public class LabTestEventTest {
    public static final String TEST_CONCEPT_UUID = "aebc57b7-0683-464e-ac48-48b8838abdfc";
    public static final String LAB_TEST_CONCEPT_UUID = "9b11d2d1-c7ea-40f7-8616-be9bec4c6bb7";

    private Concept conceptWithLabTestClass;
    private Concept conceptWithTestClass;

    @Mock
    private ConceptService conceptService;
    private Concept parentConcept;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        conceptWithLabTestClass = new ConceptBuilder().withClass("LabTest").withUUID(LAB_TEST_CONCEPT_UUID).build();
        conceptWithTestClass = new ConceptBuilder().withClass("Test").withUUID(TEST_CONCEPT_UUID).build();

        parentConcept = new ConceptBuilder().withName(AllTestsAndPanels.ALL_TESTS_AND_PANELS).withSetMember(conceptWithLabTestClass).build();
        parentConcept.addSetMember(conceptWithTestClass);

        List<ConceptSet> conceptSets = getConceptSets(parentConcept, conceptWithLabTestClass);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);

        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);
    }


    @Test
    public void createEventForTestEventIfConceptClassIsLabTestOrTest() throws Exception {
        Event eventForLabTestConceptClass = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithLabTestClass}).get(0);
        Event anotherEventForLabTestConceptClass = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithLabTestClass}).get(0);
        Event eventForTestConceptClass =  new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithTestClass}).get(0);
        Event anotherEventForTestConceptClass =  new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithTestClass}).get(0);
        assertNotNull(eventForLabTestConceptClass);
        assertNotNull(eventForTestConceptClass);
        assertFalse(eventForLabTestConceptClass.getUuid().equals(anotherEventForLabTestConceptClass.getUuid()));
        assertEquals(eventForLabTestConceptClass.getTitle(), ConceptServiceEventFactory.TEST);
        assertEquals(eventForLabTestConceptClass.getCategory(), ConceptServiceEventFactory.LAB);
        assertFalse(eventForTestConceptClass.getUuid().equals(anotherEventForTestConceptClass.getUuid()));
        assertEquals(eventForTestConceptClass.getTitle(), ConceptServiceEventFactory.TEST);
        assertEquals(eventForTestConceptClass.getCategory(), ConceptServiceEventFactory.LAB);
    }

    @Test
    public void shouldCreateEventForCaseInsensitiveConceptClassMatches() throws Exception {
        Concept conceptWithClassLabTest = new ConceptBuilder().withClass("LabTest").withUUID(LAB_TEST_CONCEPT_UUID).build();
        Concept conceptWithClasslabtest = new ConceptBuilder().withClass("labtest").withUUID("9b11d2d1-c7ea-40f7-8616-be9bec4c6b98").build();
        Event eventForLabTestConceptClass = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithClassLabTest}).get(0);
        Event eventForlabtestConceptClass = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithClasslabtest}).get(0);
        assertNotNull(eventForLabTestConceptClass);
        assertNotNull(eventForlabtestConceptClass);

    }

    @Test
    public void shouldNotCreateEventForTestEventIfThereIsDifferentConceptClass() throws Exception {
        conceptWithLabTestClass = new ConceptBuilder().withClassUUID("some").withClass("some").withUUID(TEST_CONCEPT_UUID).build();
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithLabTestClass});
        assertTrue(events.isEmpty());
    }

    @Test
    public void shouldCreateEventForTestEventIfParentConceptIsMissing() throws Exception {
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(new ArrayList<ConceptSet>());
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithLabTestClass});
        Event event = events.get(0);
        assertNotNull(event);
        assertEquals(event.getTitle(), ConceptServiceEventFactory.TEST);
        assertEquals(event.getCategory(), ConceptServiceEventFactory.LAB);
    }


    @Test
    public void shouldCreateEventForTestEventIfParentConceptIsWrong() throws Exception {
        parentConcept = new ConceptBuilder().withName("Some wrong name").withSetMember(conceptWithLabTestClass).build();
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(getConceptSets(parentConcept, conceptWithLabTestClass));
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{conceptWithLabTestClass});
        Event event = events.get(0);
        assertNotNull(event);
        assertEquals(event.getTitle(), ConceptServiceEventFactory.TEST);
        assertEquals(event.getCategory(), ConceptServiceEventFactory.LAB);
    }


    @Test
    public void createEventForTestWithParentConceptMissing() throws Exception {
        Concept testConcept = new ConceptBuilder().withUUID("testUUID").withClass("LabTest").build();
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{testConcept});
        Event event = events.get(0);
        assertNotNull(event);
        assertEquals(event.getTitle(), ConceptServiceEventFactory.TEST);
        assertEquals(event.getCategory(), ConceptServiceEventFactory.LAB);
    }

}

package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.test.builder.ConceptBuilder;
import org.ict4h.atomfeed.server.service.Event;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;

import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.getConceptSets;
import static org.bahmni.module.referencedata.labconcepts.model.event.ConceptOperationEvent.isChildOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ConceptOperationEventTest {
    public static final String URL = "url";
    public static final String CATEGORY = "category";
    public static final String TITLE = "title";
    private ConceptOperationEvent conceptOperationEvent;
    private Object[] arguments;
    private Concept childConcept;

    @Mock
    private ConceptService conceptService;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        conceptOperationEvent = new SampleEvent(URL, CATEGORY, TITLE);
        Concept concept = new ConceptBuilder().withUUID("UUID").build();
        arguments = new Object[]{concept};
        childConcept = new ConceptBuilder().withName("Child").build();
        Concept parentConcept = new ConceptBuilder().withName("Parent").withSetMember(childConcept).build();
        List<ConceptSet> conceptSets = getConceptSets(parentConcept, childConcept);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        Locale defaultLocale = new Locale("en", "GB");
        contextMockedStatic.when(Context::getConceptService).thenReturn(conceptService);
        contextMockedStatic.when(Context::getLocale).thenReturn(defaultLocale);
    }

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }



    @Test
    public void triggerAtomfeedEvent() throws Exception {
        Event event = conceptOperationEvent.asAtomFeedEvent(arguments);
        assertEquals(CATEGORY, event.getCategory());
        assertEquals(TITLE, event.getTitle());
        assertEquals(URL, event.getUri().toString());
    }

    @Test
    public void isConceptChildOfParentConcept() throws Exception {
        assertTrue(isChildOf(childConcept, "Parent"));
    }

    @Test
    public void isConceptNotAChildOfParentConcept() throws Exception {
        assertFalse(isChildOf(childConcept, "Not Parent"));
    }

}
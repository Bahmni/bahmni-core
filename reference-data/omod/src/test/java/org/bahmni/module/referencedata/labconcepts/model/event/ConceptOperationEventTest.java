package org.bahmni.module.referencedata.labconcepts.model.event;

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

import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.getConceptSets;
import static org.bahmni.module.referencedata.labconcepts.model.event.ConceptOperationEvent.isChildOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class ConceptOperationEventTest {
    public static final String URL = "url";
    public static final String CATEGORY = "category";
    public static final String TITLE = "title";
    private ConceptOperationEvent conceptOperationEvent;
    private Object[] arguments;
    private Concept childConcept;

    @Mock
    private ConceptService conceptService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        conceptOperationEvent = new SampleEvent(URL, CATEGORY, TITLE);
        Concept concept = new ConceptBuilder().withUUID("UUID").build();
        arguments = new Object[]{concept};
        childConcept = new ConceptBuilder().withName("Child").build();
        Concept parentConcept = new ConceptBuilder().withName("Parent").withSetMember(childConcept).build();
        List<ConceptSet> conceptSets = getConceptSets(parentConcept, childConcept);
        PowerMockito.mockStatic(Context.class);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);
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
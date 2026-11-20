package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.model.Operation;
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
import org.openmrs.ConceptClass;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

public class AllTestsPanelsConceptSetEventTest {
    private Concept parentConcept;
    protected Concept testConcept;
    protected Concept panelConcept;
    @Mock
    private ConceptService conceptService;
    @Mock
    private UserContext userContext;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Locale defaultLocale = new Locale("en", "GB");
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getLocale).thenReturn(defaultLocale);
        contextMockedStatic.when(Context::getConceptService).thenReturn(conceptService);
        testConcept = new ConceptBuilder().withClass(LabTest.LAB_TEST_CONCEPT_CLASSES.get(0)).build();
        panelConcept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).build();
        parentConcept = new ConceptBuilder().withName(AllTestsAndPanels.ALL_TESTS_AND_PANELS).withClass("ConvSet").withSetMember(testConcept).withSetMember(panelConcept).build();
    }

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }

    @Test
    public void shouldCreateOneEventForAllTestsAndPanelsAndSetMembers() throws Exception {
        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{parentConcept});
        assertEquals(events.size(), 1);
        Event event = events.get(0);
        assertThat(event.getUri().toString(), containsString(parentConcept.getUuid()));
        assertEquals(ConceptServiceEventFactory.TESTS_AND_PANEL, event.getTitle());
        assertEquals("lab", event.getCategory());
    }
}

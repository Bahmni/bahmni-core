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
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class AllTestsPanelsConceptSetEventTest {
    private Concept parentConcept;
    protected Concept testConcept;
    protected Concept panelConcept;
    @Mock
    private ConceptService conceptService;
    @Mock
    private UserContext userContext;

    @Before
    public void setUp() {

        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        when(Context.getConceptService()).thenReturn(conceptService);
        testConcept = new ConceptBuilder().withClass(LabTest.LAB_TEST_CONCEPT_CLASSES.get(0)).build();
        panelConcept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).build();
        parentConcept = new ConceptBuilder().withName(AllTestsAndPanels.ALL_TESTS_AND_PANELS).withClass("ConvSet").withSetMember(testConcept).withSetMember(panelConcept).build();
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

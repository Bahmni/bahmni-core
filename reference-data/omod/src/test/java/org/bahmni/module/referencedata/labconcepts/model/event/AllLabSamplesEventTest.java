package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
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

public class AllLabSamplesEventTest {

    private Concept parentConcept;
    @Mock
    private ConceptService conceptService;
    @Mock
    private UserContext userContext;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Locale defaultLocale = new Locale("en", "GB");
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getLocale).thenReturn(defaultLocale);
        contextMockedStatic.when(Context::getConceptService).thenReturn(conceptService);
        Concept concept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).build();
        Concept anotherConcept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).build();
        parentConcept = new ConceptBuilder().withName(AllSamples.ALL_SAMPLES).withClass("ConvSet").withSetMember(concept).withSetMember(anotherConcept).build();

    }

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }

    @Test
    public void shouldCreateOneEventForAllLabSamplesAndSetMembers() throws Exception {

        List<Event> events = new Operation(ConceptService.class.getMethod("saveConcept", Concept.class)).apply(new Object[]{parentConcept});
        assertEquals(events.size(), 1);
        Event event = events.get(0);
        assertThat(event.getUri().toString(), containsString(parentConcept.getUuid()));
        assertEquals(event.getTitle(), ConceptServiceEventFactory.LAB_SAMPLE);
        assertEquals(event.getCategory(), "lab");

    }
}

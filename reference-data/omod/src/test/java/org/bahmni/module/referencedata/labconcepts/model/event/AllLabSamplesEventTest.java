package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
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
public class AllLabSamplesEventTest {

    private Concept parentConcept;
    @Mock
    private ConceptService conceptService;
    @Mock
    private UserContext userContext;

    @Before
    public void setup() {
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        when(Context.getConceptService()).thenReturn(conceptService);
        Concept concept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).build();
        Concept anotherConcept = new ConceptBuilder().withClassUUID(ConceptClass.LABSET_UUID).build();
        parentConcept = new ConceptBuilder().withName(AllSamples.ALL_SAMPLES).withClass("ConvSet").withSetMember(concept).withSetMember(anotherConcept).build();

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

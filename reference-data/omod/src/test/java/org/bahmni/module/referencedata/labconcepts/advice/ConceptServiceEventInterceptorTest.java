package org.bahmni.module.referencedata.labconcepts.advice;

import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.bahmni.module.referencedata.labconcepts.contract.Sample;
import org.bahmni.module.referencedata.labconcepts.model.event.SampleEventTest;
import org.bahmni.test.builder.ConceptBuilder;
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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class ConceptServiceEventInterceptorTest {
    @Mock
    private BahmniEventPublisher eventPublisher;
    @Mock
    private ConceptService conceptService;

    private ConceptServiceEventInterceptor publishedFeed;

    private Concept concept;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        concept = new ConceptBuilder().withClass(Sample.SAMPLE_CONCEPT_CLASSES.get(0)).withUUID(SampleEventTest.SAMPLE_CONCEPT_UUID).build();

        Concept parentConcept = new ConceptBuilder().withName(AllSamples.ALL_SAMPLES).withSetMember(concept).build();

        List<ConceptSet> conceptSets = getConceptSets(parentConcept, concept);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);

        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getConceptService()).thenReturn(conceptService);
        PowerMockito.when(Context.getLocale()).thenReturn(defaultLocale);

        publishedFeed = new ConceptServiceEventInterceptor(eventPublisher);
    }

    public static List<ConceptSet> getConceptSets(Concept parentConcept, Concept conceptMember) {
        List<ConceptSet> conceptSets = new ArrayList<>();
        ConceptSet conceptSet = createConceptSet(parentConcept, conceptMember);
        conceptSets.add(conceptSet);
        return conceptSets;
    }

    public static List<ConceptSet> getConceptSets(ConceptSet conceptSet) {
        List<ConceptSet> conceptSets = new ArrayList<>();
        conceptSets.add(conceptSet);
        return conceptSets;
    }

    public static ConceptSet createConceptSet(Concept parentConcept, Concept conceptMember) {
        ConceptSet conceptSet = new ConceptSet();
        conceptSet.setConceptSet(parentConcept);
        conceptSet.setConcept(conceptMember);
        return conceptSet;
    }

    @Test
    public void shouldPublishEventAfterUpdateConceptOperation() throws Throwable {
        Method method = ConceptService.class.getMethod("saveConcept", Concept.class);
        Object[] objects = new Object[]{concept};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(eventPublisher).publishEvent(any(EMREvent.class));
    }

    @Test
    public void shouldPublishEventAfterEveryUpdateConceptOperation() throws Throwable {
        Method method = ConceptService.class.getMethod("saveConcept", Concept.class);
        Object[] objects = new Object[]{concept};
        int updates = 2;
        for (int i = 0; i < updates; i++) {
            publishedFeed.afterReturning(null, method, objects, null);
        }
        verify(eventPublisher, times(updates)).publishEvent(any(EMREvent.class));
    }

    @Test
    public void shouldPublishEventAfterSaveConceptOperation() throws Throwable {
        Method method = ConceptService.class.getMethod("saveConcept", Concept.class);
        Object[] objects = new Object[]{concept};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(eventPublisher).publishEvent(any(EMREvent.class));
    }

    @Test
    public void shouldPublishEventAfterEverySaveConceptOperation() throws Throwable {
        Method method = ConceptService.class.getMethod("saveConcept", Concept.class);
        Object[] objects = new Object[]{concept};
        int updates = 2;
        for (int i = 0; i < updates; i++) {
            publishedFeed.afterReturning(null, method, objects, null);
        }
        verify(eventPublisher, times(updates)).publishEvent(any(EMREvent.class));
    }
}

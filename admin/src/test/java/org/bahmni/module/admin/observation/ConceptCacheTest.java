package org.bahmni.module.admin.observation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ConceptCacheTest {


    @Mock
    private ConceptService conceptService;

    @Before
    public void before() {
        initMocks(this);
    }

    @Test
    public void shouldGetConceptByName() {
        Concept expectedConcept = new Concept();
        String conceptName = "conceptName";
        when(conceptService.getConceptByName(conceptName)).thenReturn(expectedConcept);

        ConceptCache conceptCache = new ConceptCache(conceptService);
        assertEquals(conceptCache.getConcept(conceptName), expectedConcept);
    }

    @Test
    public void shouldCacheConcepts() {
        Concept expectedConcept = new Concept();
        String conceptName = "conceptName";
        when(conceptService.getConceptByName(conceptName)).thenReturn(expectedConcept);

        ConceptCache conceptCache = new ConceptCache(conceptService);
        assertEquals(conceptCache.getConcept(conceptName), expectedConcept);
        assertEquals(conceptCache.getConcept(conceptName), expectedConcept);
        verify(conceptService, times(1)).getConceptByName(conceptName);
    }
}
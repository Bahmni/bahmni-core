package org.bahmni.module.bahmnicore.web.v1_0.search;


import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BahmniConceptSearchHandlerTest {

    @Mock
    @Qualifier("conceptService")
    private ConceptService conceptService;

    @Mock
    RequestContext requestContext;

    @Mock
    UserContext userContext;

    @InjectMocks
    private BahmniConceptSearchHandler bahmniConceptSearchHandler;


    @Test
    public void shouldSearchByQuestion() {
        SearchConfig searchConfig = bahmniConceptSearchHandler.getSearchConfig();
        assertEquals(searchConfig.getId(), "byFullySpecifiedName");
    }

    @Test
    public void shouldSupportVersions1_8To2() {
        SearchConfig searchConfig = bahmniConceptSearchHandler.getSearchConfig();
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.8.* - 2.*"));
    }

    @Test
    public void shouldSearchByGivenLocale_whenLocaleIsSpecified() {
        Assume.assumeFalse(
                "Test exercises the non-default branch; skip if defaultLocale == fr",
                LocaleUtility.getDefaultLocale().equals(Locale.FRENCH));

        List<ConceptSearchResult> conceptSearchResults = new ArrayList<>();
        ConceptSearchResult result =  new ConceptSearchResult();
        Concept concept = new Concept();
        concept.setId(123);
        ConceptName conceptNameFullySpecified = new ConceptName();
        conceptNameFullySpecified.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        conceptNameFullySpecified.setName("Nutritional Values");
        concept.setNames(Collections.singleton(conceptNameFullySpecified));
        result.setConcept(concept);
        conceptSearchResults.add(result);

        // Ordering matters: requested locale first, default second.
        List<Locale> expectedLocaleList = Arrays.asList(Locale.FRENCH, LocaleUtility.getDefaultLocale());

        when(conceptService.getConcepts(anyString(), anyList(), anyBoolean(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Integer.class), isNull())).thenReturn(conceptSearchResults);
        when(requestContext.getLimit()).thenReturn(10);
        when(requestContext.getParameter("locale")).thenReturn("fr");
        when(requestContext.getParameter("name")).thenReturn("Nutritional Values");


        NeedsPaging<Concept> searchResults = (NeedsPaging<Concept>) bahmniConceptSearchHandler.search(requestContext);

        verify(conceptService, times(1)).getConcepts("Nutritional Values", expectedLocaleList, false, null, null, null, null, null, 0, null);
        assertEquals(1, searchResults.getPageOfResults().size());
        assertEquals(new Integer(123) , searchResults.getPageOfResults().get(0).getId());
    }

    @Test
    public void shouldIncludeDefaultLocale_whenNonDefaultLocaleIsSpecified() {
        Locale spanish = LocaleUtility.fromSpecification("es");
        Assume.assumeFalse(
                "Test exercises the non-default branch; skip if defaultLocale == es",
                LocaleUtility.getDefaultLocale().equals(spanish));

        List<ConceptSearchResult> conceptSearchResults = new ArrayList<>();
        ConceptSearchResult result = new ConceptSearchResult();
        Concept concept = new Concept();
        concept.setId(456);
        ConceptName conceptNameFullySpecified = new ConceptName();
        conceptNameFullySpecified.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        conceptNameFullySpecified.setName("Lab Samples");
        concept.setNames(Collections.singleton(conceptNameFullySpecified));
        result.setConcept(concept);
        conceptSearchResults.add(result);

        // Ordering matters: requested locale first, default second.
        List<Locale> expectedLocaleList = Arrays.asList(spanish, LocaleUtility.getDefaultLocale());

        when(conceptService.getConcepts(anyString(), anyList(), anyBoolean(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Integer.class), isNull())).thenReturn(conceptSearchResults);
        when(requestContext.getLimit()).thenReturn(10);
        when(requestContext.getParameter("locale")).thenReturn("es");
        when(requestContext.getParameter("name")).thenReturn("Lab Samples");

        NeedsPaging<Concept> searchResults = (NeedsPaging<Concept>) bahmniConceptSearchHandler.search(requestContext);

        verify(conceptService, times(1)).getConcepts("Lab Samples", expectedLocaleList, false, null, null, null, null, null, 0, null);
        assertEquals(1, searchResults.getPageOfResults().size());
        assertEquals(new Integer(456), searchResults.getPageOfResults().get(0).getId());
    }

    @Test
    public void shouldNotDuplicateDefaultLocale_whenRequestedLocaleEqualsDefault() {
        Locale defaultLocale = LocaleUtility.getDefaultLocale();
        Assume.assumeTrue(
                "Test requires defaultLocale to round-trip via fromSpecification",
                defaultLocale.equals(LocaleUtility.fromSpecification(defaultLocale.toString())));

        List<ConceptSearchResult> conceptSearchResults = new ArrayList<>();
        ConceptSearchResult result = new ConceptSearchResult();
        Concept concept = new Concept();
        concept.setId(789);
        ConceptName conceptNameFullySpecified = new ConceptName();
        conceptNameFullySpecified.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        conceptNameFullySpecified.setName("Lab Samples");
        concept.setNames(Collections.singleton(conceptNameFullySpecified));
        result.setConcept(concept);
        conceptSearchResults.add(result);

        List<Locale> expectedLocaleList = Collections.singletonList(defaultLocale);

        when(conceptService.getConcepts(anyString(), anyList(), anyBoolean(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Integer.class), isNull())).thenReturn(conceptSearchResults);
        when(requestContext.getLimit()).thenReturn(10);
        when(requestContext.getParameter("locale")).thenReturn(defaultLocale.toString());
        when(requestContext.getParameter("name")).thenReturn("Lab Samples");

        NeedsPaging<Concept> searchResults = (NeedsPaging<Concept>) bahmniConceptSearchHandler.search(requestContext);

        verify(conceptService, times(1)).getConcepts("Lab Samples", expectedLocaleList, false, null, null, null, null, null, 0, null);
        assertEquals(1, searchResults.getPageOfResults().size());
        assertEquals(new Integer(789), searchResults.getPageOfResults().get(0).getId());
    }

    @Test
    public void shouldSearchByLoggedInLocaleAndDefaultLocale_whenLocaleIsNotSpecified() {
        when(requestContext.getParameter("name")).thenReturn("Nutritional Values");

        bahmniConceptSearchHandler.search(requestContext);
        List<Locale> localeList = new ArrayList<>();
        localeList.add(LocaleUtility.getDefaultLocale());

        verify(conceptService, times(1)).getConcepts("Nutritional Values", localeList, false, null, null, null, null, null, 0, null);
    }
}

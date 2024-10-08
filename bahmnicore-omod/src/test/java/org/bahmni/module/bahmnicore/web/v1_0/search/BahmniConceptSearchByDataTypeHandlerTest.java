package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.ConceptService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;


@RunWith(MockitoJUnitRunner.class)
public class BahmniConceptSearchByDataTypeHandlerTest {

    public static final String DATA_TYPES = "dataTypes";
    public static final String NAME = "ConceptName";

    @Mock
    ConceptService conceptService;
    @Mock
    RequestContext requestContext;

    @InjectMocks
    BahmniConceptSearchByDataTypeHandler bahmniConceptSearchByDataTypeHandler;

    @Before
    public void setup() throws Exception {
        initMocks(this);
    }

    @Test
    public void shouldSearchByDataType() {
        SearchConfig searchConfig = bahmniConceptSearchByDataTypeHandler.getSearchConfig();
        assertThat(searchConfig.getId(), is(equalTo("byDataType")));
    }

    @Test
    public void shouldSupportVersions1_9To2() {
        SearchConfig searchConfig = bahmniConceptSearchByDataTypeHandler.getSearchConfig();
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.9.* - 2.*"));
    }

    @Test
    public void shouldDelegateSearchOfConceptsToConceptService() {
        List<ConceptSearchResult> conceptSearchResults = new ArrayList<>();
        ConceptSearchResult result =  new ConceptSearchResult();
        Concept concept = new Concept();
        concept.setId(10);
        result.setConcept(concept);
        conceptSearchResults.add(result);
        List<ConceptDatatype> conceptDatatypes = new ArrayList<>();
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setId(1);
        conceptDatatypes.add(conceptDatatype);
        List<Locale> localeList = new ArrayList<>();
        localeList.add(Locale.ENGLISH);
        when(conceptService.getConceptDatatypeByName(DATA_TYPES)).thenReturn(conceptDatatype);
        when(conceptService.getConcepts(NAME, localeList, false, null, null, conceptDatatypes,
                 null, null, 0, 10)).thenReturn(conceptSearchResults);

        when(requestContext.getParameter("name")).thenReturn(NAME);
        when(requestContext.getParameter("dataTypes")).thenReturn(DATA_TYPES);
        when(requestContext.getParameter("locale")).thenReturn(Locale.ENGLISH.toString());
        when(requestContext.getLimit()).thenReturn(10);

        NeedsPaging<Concept> searchResults = (NeedsPaging<Concept>) bahmniConceptSearchByDataTypeHandler.search(requestContext);

        assertThat(searchResults.getPageOfResults().size(), is(equalTo(1)));
        assertThat(searchResults.getPageOfResults().get(0).getId(), is(equalTo(10)));
    }
}

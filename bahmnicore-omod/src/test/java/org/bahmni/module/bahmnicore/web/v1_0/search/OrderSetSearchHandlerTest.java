package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.service.BahmniOrderSetService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.OrderSet;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class OrderSetSearchHandlerTest {
    public static final String QUERY = "q";

    @Mock
    private BahmniOrderSetService bahmniOrderSetService;
    private OrderSetSearchHandler orderSetSearchHandler;

    @Mock
    RequestContext requestContext;

    @Before
    public void before() {
        initMocks(this);
        orderSetSearchHandler = new OrderSetSearchHandler(bahmniOrderSetService);
    }

    @Test
    public void shouldSearchByQuery() {
        SearchConfig searchConfig = orderSetSearchHandler.getSearchConfig();
        assertThat(searchConfig.getId(), is(equalTo("byQuery")));
    }

    @Test
    public void shouldSupportVersions1_9To2() {
        SearchConfig searchConfig = orderSetSearchHandler.getSearchConfig();
        assertTrue(searchConfig.getSupportedOpenmrsVersions().contains("1.9.* - 2.*"));
    }
    @Test
    public void shouldDelegateSearchOfOrdersetToBahmniOrderSetService() {
        List<OrderSet> resultOrderSets = new ArrayList<>();
        when(bahmniOrderSetService.getOrderSetByQuery(QUERY)).thenReturn(resultOrderSets);
        when(requestContext.getParameter("q")).thenReturn(QUERY);

        NeedsPaging<Concept> searchResults = (NeedsPaging<Concept>) orderSetSearchHandler.search(requestContext);

        assertThat(searchResults.getPageOfResults().size(), is(equalTo(0)));
    }
}

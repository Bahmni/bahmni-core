package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.service.BahmniOrderSetService;
import org.openmrs.OrderSet;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OrderSetSearchHandler implements SearchHandler {
    public static final String QUERY = "q";
    private BahmniOrderSetService bahmniOrderSetService;

    @Autowired
    public OrderSetSearchHandler(BahmniOrderSetService bahmniOrderSetService) {
        this.bahmniOrderSetService = bahmniOrderSetService;
    }

    @Override
    public SearchConfig getSearchConfig() {
        return new SearchConfig("byQuery", RestConstants.VERSION_1 + "/bahmniorderset", Arrays.asList("1.9.* - 2.*"),
                new SearchQuery.Builder("Allows you to find OrderSets by search query").withRequiredParameters("q").build());

    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {
        String query = requestContext.getParameter(QUERY);
        List<OrderSet> orderSets = bahmniOrderSetService.getOrderSetByQuery(query);


        return new NeedsPaging<>(orderSets, requestContext);
    }
}

package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.service.OrderService;
import org.openmrs.Order;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OrderSearchHandler implements SearchHandler{

    private OrderService bahmniOrderService;

    @Autowired
    public OrderSearchHandler(OrderService bahmniOrderService) {
        this.bahmniOrderService = bahmniOrderService;
    }

    @Override
    public SearchConfig getSearchConfig() {
        return new SearchConfig("byOrderType", RestConstants.VERSION_1 + "/order", Arrays.asList("1.9.* - 2.*"),
                new SearchQuery.Builder("Allows you to find orders by orderType for a patient").withRequiredParameters("patientUuid", "orderTypeUuid").build());
    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {
        String patientUuid = requestContext.getParameter("patientUuid");
        String orderTypeUuid = requestContext.getParameter("orderTypeUuid");
        List<Order> allOrders = bahmniOrderService.getAllOrders(patientUuid, orderTypeUuid, requestContext.getStartIndex(), requestContext.getLimit(), null);
        return new AlreadyPaged<>(requestContext, allOrders, false);
    }
}

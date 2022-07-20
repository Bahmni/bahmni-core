package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.service.BahmniOrderService;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.OrderResource1_10;

@Resource(name = RestConstants.VERSION_1 + "/order", supportedClass = Order.class, supportedOpenmrsVersions = {"1.10.* - 2.*"}, order = 0)
public class BahmniOrderResource extends OrderResource1_10 {

    @Override
    protected void delete(Order delegate, String reason, RequestContext context) throws ResponseException {
        deleteChildOrder(delegate, context);
    }

    private void deleteChildOrder(Order order, RequestContext context) {
        Order nextOrder = Context.getService(BahmniOrderService.class).getChildOrder(order);

        if (nextOrder != null)
            deleteChildOrder(nextOrder, context);

        super.delete(order, "Voided by User", context);
    }
}

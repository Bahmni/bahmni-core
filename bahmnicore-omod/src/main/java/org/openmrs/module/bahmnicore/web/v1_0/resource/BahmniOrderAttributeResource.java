package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.Order;
import org.openmrs.OrderAttribute;
import org.openmrs.OrderAttributeType;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeCrudResource1_9;

import java.util.Collection;
import java.util.List;

@SubResource(parent = BahmniOrderResource.class, path = "attribute", supportedClass = OrderAttribute.class, supportedOpenmrsVersions = {
        "2.2.* - 9.*" })
public class BahmniOrderAttributeResource extends BaseAttributeCrudResource1_9<OrderAttribute, Order, BahmniOrderResource> {

    @PropertySetter("attributeType")
    public static void setAttributeType(OrderAttribute instance, OrderAttributeType attr) {
        instance.setAttributeType(attr);
    }

    @Override
    public Order getParent(OrderAttribute instance) {
        return instance.getOrder();
    }

    @Override
    public void setParent(OrderAttribute orderAttribute, Order order) {
        orderAttribute.setOrder(order);

    }

    @Override
    public PageableResult doGetAll(Order parent, RequestContext context)
            throws ResponseException {
        return new NeedsPaging<OrderAttribute>((List<OrderAttribute>) parent.getActiveAttributes(),
                context);
    }

    @Override
    public OrderAttribute getByUniqueId(String uniqueId) {
        return Context.getService(OrderService.class).getOrderAttributeByUuid(uniqueId);
    }

    @Override
    protected void delete(OrderAttribute delegate, String reason, RequestContext context)
            throws ResponseException {
        delegate.setVoided(true);
        delegate.setVoidReason(reason);
        Context.getService(OrderService.class).saveOrder(delegate.getOrder(),null);
    }

    @Override
    public OrderAttribute newDelegate() {
        return new OrderAttribute();
    }

    @Override
    public OrderAttribute save(OrderAttribute delegate) {
        //TODO Implement saving of order attributes through DAO. Order service cannot be leveraged since order is immutable.
        throw new UnsupportedOperationException("Saving order attributes through sub resource is not supported");
    }

    @Override
    public void purge(OrderAttribute orderAttribute, RequestContext requestContext)
            throws ResponseException {
        throw new UnsupportedOperationException("Cannot purge OrderAttribute");
    }
}
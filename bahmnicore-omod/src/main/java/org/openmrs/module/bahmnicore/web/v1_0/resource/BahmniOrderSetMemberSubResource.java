package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.OrderSet;
import org.openmrs.OrderSetMember;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.SubResource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.ArrayList;
import java.util.List;

@SubResource(parent = BahmniOrderSetResource.class, path = "bahmniordersetmember", supportedClass = OrderSetMember.class, supportedOpenmrsVersions = { "1.12.* - 2.*"})
public class BahmniOrderSetMemberSubResource extends DelegatingSubResource<OrderSetMember, OrderSet, BahmniOrderSetResource> {

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        if (rep instanceof DefaultRepresentation) {
            description.addProperty("uuid");
            description.addProperty("display");
            description.addProperty("retired");
            description.addProperty("orderType", Representation.REF);
            description.addProperty("orderTemplate");
            description.addProperty("concept", Representation.REF);
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof FullRepresentation) {
            description.addProperty("uuid");
            description.addProperty("display");
            description.addProperty("retired");
            description.addProperty("orderType", Representation.DEFAULT);
            description.addProperty("orderTemplate");
            description.addProperty("concept", Representation.DEFAULT);
            description.addProperty("auditInfo", findMethod("getAuditInfo"));
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof RefRepresentation) {
            description.addProperty("uuid");
            description.addProperty("display");
            description.addProperty("concept", Representation.REF);
            description.addSelfLink();
        }
        return null;
    }

    @PropertyGetter("display")
    public String getDisplayString(OrderSetMember orderSetMember) {
        return orderSetMember.getDescription();
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getCreatableProperties()
     */
    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("orderType");
        description.addProperty("orderTemplate");
        description.addProperty("concept");
        description.addProperty("retired");
        return description;
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResource#getUpdatableProperties()
     */
    @Override
    public DelegatingResourceDescription getUpdatableProperties() {
        DelegatingResourceDescription creatableProperties = getCreatableProperties();
        return creatableProperties;
    }

    @Override
    public OrderSetMember getByUniqueId(String uniqueId) {
        return Context.getOrderSetService().getOrderSetMemberByUuid(uniqueId);
    }

    @Override
    protected void delete(OrderSetMember orderSetMember, String reason, RequestContext context) throws ResponseException {
        OrderSet orderSet = orderSetMember.getOrderSet();
        orderSet.retireOrderSetMember(orderSetMember);
        Context.getOrderSetService().saveOrderSet(orderSet);
    }

    @Override
    public OrderSetMember newDelegate() {
        return new OrderSetMember();
    }

    @Override
    public OrderSetMember save(OrderSetMember delegate) {
        OrderSet parent = delegate.getOrderSet();
        parent.addOrderSetMember(delegate);
        Context.getOrderSetService().saveOrderSet(parent);
        return delegate;
    }

    @Override
    public void purge(OrderSetMember orderSetMember, RequestContext context) throws ResponseException {
        OrderSet orderSet = orderSetMember.getOrderSet();
        orderSet.removeOrderSetMember(orderSetMember);
        Context.getOrderSetService().saveOrderSet(orderSet);
    }

    @Override
    public OrderSet getParent(OrderSetMember instance) {
        return instance.getOrderSet();
    }

    @Override
    public void setParent(OrderSetMember instance, OrderSet orderSet) {
        instance.setOrderSet(orderSet);
    }

    @Override
    public PageableResult doGetAll(OrderSet parent, RequestContext context) throws ResponseException {
        List<OrderSetMember> orderSetMembers = new ArrayList<OrderSetMember>();
        if (parent != null) {
            for (OrderSetMember orderSetMember : parent.getOrderSetMembers()) {
                orderSetMembers.add(orderSetMember);
            }
        }
        return new NeedsPaging<OrderSetMember>(orderSetMembers, context);
    }
}

package org.openmrs.module.bahmnicore.web.v1_0.resource;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.OrderSet;
import org.openmrs.OrderSetMember;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.MetadataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/bahmniorderset", supportedClass = OrderSet.class, supportedOpenmrsVersions = { "1.12.* - 2.*"})
public class BahmniOrderSetResource extends MetadataDelegatingCrudResource<OrderSet> {

    @Override
    public OrderSet getByUniqueId(String uniqueId) {
        OrderSet orderSet = Context.getOrderSetService().getOrderSetByUuid(uniqueId);
        orderSet.setOrderSetMembers(orderSet.getUnRetiredOrderSetMembers());
        return orderSet;
    }

    @Override
    public OrderSet newDelegate() {
        return new OrderSet();
    }

    @Override
    public OrderSet save(OrderSet orderSet) {
        if(CollectionUtils.isNotEmpty(orderSet.getOrderSetMembers())){
            for(OrderSetMember orderSetMember : orderSet.getOrderSetMembers()) {
                if (null != orderSetMember.getConcept() && StringUtils.isNotEmpty(orderSetMember.getConcept().getUuid())) {
                    orderSetMember.setConcept(Context.getConceptService().getConceptByUuid(orderSetMember.getConcept().getUuid()));
                }
                if(null != orderSetMember.getOrderType() && StringUtils.isNotEmpty(orderSetMember.getOrderType().getUuid())) {
                    orderSetMember.setOrderType(Context.getOrderService().getOrderTypeByUuid(orderSetMember.getOrderType().getUuid()));
                }
            }
        }
        return Context.getOrderSetService().saveOrderSet(orderSet);
    }

    @PropertySetter("orderSetMembers")
    public static void setOrderSetMembers(OrderSet instance, List<OrderSetMember> orderSetMembers){
        instance.setOrderSetMembers(orderSetMembers);
    }

    @PropertyGetter("orderSetMembers")
    public static List<OrderSetMember> getOrderSetMembers(OrderSet instance){
        return instance.getUnRetiredOrderSetMembers();
    }

    @Override
    public void purge(OrderSet delegate, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    /**
     * @see org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource#doGetAll(org.openmrs.module.webservices.rest.web.RequestContext)
     */
    @Override
    protected NeedsPaging<OrderSet> doGetAll(RequestContext context) {
        List<OrderSet> orderSets = Context.getOrderSetService().getOrderSets(context.getIncludeAll());
        for (OrderSet orderSet : orderSets) {
            orderSet.setOrderSetMembers(orderSet.getUnRetiredOrderSetMembers());
        }
        return new NeedsPaging<OrderSet>(orderSets, context);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        if (rep instanceof DefaultRepresentation) {
            description.addProperty("uuid");
            description.addProperty("display");
            description.addProperty("name");
            description.addProperty("description");
            description.addProperty("retired");
            description.addProperty("operator");
            description.addProperty("orderSetMembers", Representation.REF);
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof FullRepresentation) {
            description.addProperty("uuid");
            description.addProperty("display");
            description.addProperty("name");
            description.addProperty("description");
            description.addProperty("retired");
            description.addProperty("operator");
            description.addProperty("orderSetMembers", Representation.DEFAULT);
            description.addProperty("auditInfo", findMethod("getAuditInfo"));
            description.addSelfLink();
            return description;
        } else {
            return null;
        }
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription d = super.getCreatableProperties();
        d.addProperty("operator");
        d.addProperty("orderSetMembers");
        return d;
    }
}

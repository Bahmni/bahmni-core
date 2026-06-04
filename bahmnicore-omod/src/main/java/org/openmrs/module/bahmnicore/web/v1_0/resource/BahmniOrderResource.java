package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.service.BahmniOrderService;
import org.openmrs.Order;
import org.openmrs.OrderAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.OrderResource1_10;
import java.util.Collection;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/order", supportedClass = Order.class, supportedOpenmrsVersions = {"1.10.* - 2.*"}, order = 0)
public class BahmniOrderResource extends OrderResource1_10 {

    @Override
    protected void delete(Order delegate, String reason, RequestContext context) throws ResponseException {
        deleteChildOrder(delegate, context);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = super.getRepresentationDescription(rep);
            description.addProperty("attributes", Representation.REF);
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = super.getRepresentationDescription(rep);
            description.addProperty("attributes", Representation.DEFAULT);
            return description;
        }
        return null;
    }

    @PropertyGetter("attributes")
    public static Collection<OrderAttribute> getAttributes(Order instance) {
        return instance.getActiveAttributes();
    }

    @PropertySetter("attributes")
    public static void setAttributes(Order instance, List<OrderAttribute> attrs) {
        for (OrderAttribute attr : attrs) {
            instance.addAttribute(attr);
        }
    }

    @Override
    public List<String> getPropertiesToExposeAsSubResources() {
        List<String> propertiesToExposeAsSubResources = super.getPropertiesToExposeAsSubResources();
        propertiesToExposeAsSubResources.add("attributes");
        return propertiesToExposeAsSubResources;
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription creatableProperties = super.getCreatableProperties();
        creatableProperties.addProperty("attributes");
        return creatableProperties;
    }

    @Override
    public Order save(Order delegate) {
        CustomDatatypeUtil.saveAttributesIfNecessary(delegate);
        return super.save(delegate);
    }

    private void deleteChildOrder(Order order, RequestContext context) {
        Order nextOrder = Context.getService(BahmniOrderService.class).getChildOrder(order);

        if (nextOrder != null)
            deleteChildOrder(nextOrder, context);

        super.delete(order, "Voided by User", context);
    }
}

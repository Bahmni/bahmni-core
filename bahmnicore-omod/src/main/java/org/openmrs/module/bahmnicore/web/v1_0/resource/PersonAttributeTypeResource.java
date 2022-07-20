package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PersonAttributeTypeResource1_8;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;
import org.openmrs.util.OpenmrsUtil;

@Resource(name = RestConstants.VERSION_1 + "/personattributetype", supportedClass = PersonAttributeType.class, supportedOpenmrsVersions = {"1.12.* - 2.*"}, order = 0)
public class PersonAttributeTypeResource extends PersonAttributeTypeResource1_8 {
    @PropertyGetter("concept")
    public Object getConcept(PersonAttributeType delegate) {
        if (OpenmrsUtil.nullSafeEquals(delegate.getFormat(), Concept.class.getCanonicalName())) {
            Concept concept = Context.getConceptService().getConcept(delegate.getForeignKey());
            return ConversionUtil.convertToRepresentation(concept, new NamedRepresentation("bahmniFullAnswers"));
        }
        return null;
    }
}

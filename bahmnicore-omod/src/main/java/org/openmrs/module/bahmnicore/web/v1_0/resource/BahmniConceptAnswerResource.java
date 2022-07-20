package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.web.v1_0.contract.BahmniConceptAnswer;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.CustomRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/bahmniconceptanswer", supportedClass = BahmniConceptAnswer.class, supportedOpenmrsVersions = {"1.9.* - 2.*"}, order = 0)
public class BahmniConceptAnswerResource extends DelegatingCrudResource<BahmniConceptAnswer> {

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if(rep instanceof CustomRepresentation){
            return null;
        }
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("concept");
        description.addProperty("drug");
        return description;
    }

    @Override
    public BahmniConceptAnswer getByUniqueId(String s) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    protected void delete(BahmniConceptAnswer bahmniConceptAnswer, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public BahmniConceptAnswer newDelegate() {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public BahmniConceptAnswer save(BahmniConceptAnswer bahmniConceptAnswer) {
        throw new ResourceDoesNotSupportOperationException();
    }

    @Override
    public void purge(BahmniConceptAnswer bahmniConceptAnswer, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException();
    }

}

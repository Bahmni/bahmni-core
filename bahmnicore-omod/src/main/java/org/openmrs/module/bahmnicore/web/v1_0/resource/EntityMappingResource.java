package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.contract.entityMapping.Entity;
import org.openmrs.Concept;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.RepHandler;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ConversionException;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1 + "/entitymapping", supportedClass = Entity.class, supportedOpenmrsVersions = {"1.9.* - 2.*"})
public class EntityMappingResource extends DelegatingCrudResource<Entity> {

    @Override
    public Entity getByUniqueId(String entity1Uuid) {
        return null;
    }

    @Override
    protected void delete(Entity entity, String s, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("deletion of entity mapping is not supported");
    }

    @Override
    public Entity newDelegate() {
        return new Entity();
    }

    @Override
    public Entity save(Entity entity) {
        throw new ResourceDoesNotSupportOperationException("Save of entity mapping is not supported");
    }

    @Override
    public void purge(Entity entity, RequestContext requestContext) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("Purge of entity mapping is not supported");
    }

    @RepHandler(DefaultRepresentation.class)
    public SimpleObject asDefault(Entity delegate) throws ConversionException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("entity", Representation.DEFAULT);
        if (!delegate.getMappings().isEmpty() && delegate.getMappings().get(0) instanceof Concept) {
            description.addProperty("mappings", new NamedRepresentation("bahmni"));
        } else {
            description.addProperty("mappings", Representation.DEFAULT);
        }
       return convertDelegateToRepresentation(delegate, description);
    }


    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation representation) {
        if (representation instanceof DefaultRepresentation) {
            return null;
        } else {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("entity", Representation.DEFAULT);
            description.addProperty("mappings", Representation.DEFAULT);
            return description;
        }
    }

    @Override
    public Object retrieve(String uuid, RequestContext context) throws ResponseException {
        throw new ResourceDoesNotSupportOperationException("Retrieve of entity mapping is not supported");
    }
}

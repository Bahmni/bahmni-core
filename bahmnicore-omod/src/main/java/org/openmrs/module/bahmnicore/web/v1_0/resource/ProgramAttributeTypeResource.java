package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.bahmni.module.bahmnicore.customdatatype.datatype.CodedConceptDatatype;
import org.bahmni.module.bahmnicore.model.bahmniPatientProgram.ProgramAttributeType;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.bahmni.module.bahmnicore.util.MiscUtils;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_9.BaseAttributeTypeCrudResource1_9;
import org.openmrs.util.OpenmrsUtil;

@Resource(name = RestConstants.VERSION_1 + "/programattributetype", supportedClass = ProgramAttributeType.class, supportedOpenmrsVersions = {"1.12.*","2.0.*", "2.1.*"})
public class ProgramAttributeTypeResource extends BaseAttributeTypeCrudResource1_9<ProgramAttributeType> {

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

        if(rep instanceof RefRepresentation){
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("display");
            description.addProperty("description");
            description.addProperty("retired");
            description.addSelfLink();
            return description;
        }
        return super.getRepresentationDescription(rep);
    }

    @Override
    public ProgramAttributeType getByUniqueId(String uuid) {
        return Context.getService(BahmniProgramWorkflowService.class).getProgramAttributeTypeByUuid(uuid);
    }

    @Override
    public ProgramAttributeType newDelegate() {
        return new ProgramAttributeType();
    }

    @Override
    public ProgramAttributeType save(ProgramAttributeType programAttributeType) {
        return Context.getService(BahmniProgramWorkflowService.class).saveProgramAttributeType(programAttributeType);
    }

    @Override
    public void purge(ProgramAttributeType programAttributeType, RequestContext requestContext) throws ResponseException {
        Context.getService(BahmniProgramWorkflowService.class).purgeProgramAttributeType(programAttributeType);
    }

    @Override
    protected NeedsPaging<ProgramAttributeType> doGetAll(RequestContext context) throws ResponseException {
        return new NeedsPaging<>(Context.getService(BahmniProgramWorkflowService.class).getAllProgramAttributeTypes(),
                context);
    }

    @PropertyGetter("concept")
    public Object getConcept(ProgramAttributeType delegate) {
        if (OpenmrsUtil.nullSafeEquals(delegate.getDatatypeClassname(), CodedConceptDatatype.class.getCanonicalName())) {
            Concept concept;
            String id = delegate.getDatatypeConfig();
            if (MiscUtils.onlyDigits(id)) {
            	concept = Context.getConceptService().getConcept(Integer.valueOf(id));
            } else {
            	concept = Context.getConceptService().getConceptByUuid(id);
            }
            return ConversionUtil.convertToRepresentation(concept, Representation.FULL);
        }
        return null;
    }
}

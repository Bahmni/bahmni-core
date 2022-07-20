package org.openmrs.module.bahmnicore.web.v1_0.resource;


import org.openmrs.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.PropertySetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingSubResource;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_10.ProgramEnrollmentResource1_10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Resource(name = RestConstants.VERSION_1 + "/bahmniprogramenrollment", supportedClass = PatientProgram.class, supportedOpenmrsVersions = {"1.12.* - 2.*"}, order = 0)
public class BahmniProgramEnrollmentResource extends ProgramEnrollmentResource1_10 {

    @PropertySetter("attributes")
    public static void setAttributes(PatientProgram instance, List<PatientProgramAttribute> attrs) {
        for (PatientProgramAttribute attr : attrs) {
            instance.addAttribute(attr);
        }
    }

    @PropertyGetter("attributes")
    public static Collection<PatientProgramAttribute> getAttributes(PatientProgram instance) {
        return instance.getActiveAttributes();
    }

    @PropertyGetter("states")
    public static List<SimpleObject> getStates(PatientProgram instance) throws Exception {
        List<SimpleObject> states = new ArrayList<>();
        for(PatientState state: instance.getStates()){
            if (!state.isVoided()) {
                states.add(getPatientState(state));
            }
        }
        return states;
    }

    private static SimpleObject getPatientState(PatientState patientState) throws Exception {
        DelegatingSubResource patientStateResource = (DelegatingSubResource)Context.getService(RestService.class).getResourceBySupportedClass(PatientState.class);

        SimpleObject state = new SimpleObject();
        state.put("auditInfo", patientStateResource.getAuditInfo(patientState));
        state.put("uuid", patientState.getUuid());
        state.put("startDate", ConversionUtil.convertToRepresentation(patientState.getStartDate(), Representation.DEFAULT));
        state.put("endDate", ConversionUtil.convertToRepresentation(patientState.getEndDate(), Representation.DEFAULT));
        state.put("voided", patientState.getVoided());
        state.put("state", ConversionUtil.convertToRepresentation(patientState.getState(), Representation.REF));
        return state;
    }

    @Override
    public PatientProgram newDelegate() {
        return new PatientProgram();
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        DelegatingResourceDescription parentRep = super.getRepresentationDescription(rep);
        if (rep instanceof DefaultRepresentation) {
            parentRep.addProperty("attributes", Representation.REF);
            return parentRep;
        } else if (rep instanceof FullRepresentation) {
            parentRep.addProperty("states");
            parentRep.addProperty("attributes", Representation.DEFAULT);
            return parentRep;
        } else {
            return null;
        }
    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() {
        DelegatingResourceDescription delegatingResourceDescription = super.getCreatableProperties();
        delegatingResourceDescription.addProperty("attributes");
        return delegatingResourceDescription;
    }

    @Override
    public DelegatingResourceDescription getUpdatableProperties() {
        DelegatingResourceDescription delegatingResourceDescription = super.getUpdatableProperties();
        delegatingResourceDescription.addProperty("attributes");
        delegatingResourceDescription.addProperty("voidReason");
        return delegatingResourceDescription;
    }

    public PatientProgram getByUniqueId(String uniqueId) {
        return Context.getService(BahmniProgramWorkflowService.class).getPatientProgramByUuid(uniqueId);
    }

    protected void delete(PatientProgram delegate, String reason, RequestContext context) throws ResponseException {
        if (!delegate.isVoided().booleanValue()) {
            Context.getService(BahmniProgramWorkflowService.class).voidPatientProgram(delegate, reason);
        }
    }

    public void purge(PatientProgram delegate, RequestContext context) throws ResponseException {
        Context.getService(BahmniProgramWorkflowService.class).purgePatientProgram(delegate);
    }

    @Override
    public List<String> getPropertiesToExposeAsSubResources() {
        return Arrays.asList("attributes");
    }

    public PatientProgram save(PatientProgram delegate) {
        return Context.getService(BahmniProgramWorkflowService.class).savePatientProgram(delegate);
    }

    protected PageableResult doSearch(RequestContext context) {
        String patientUuid = context.getRequest().getParameter("patient");
        String patientProgramUuid = context.getRequest().getParameter("patientProgramUuid");
        if (patientProgramUuid != null) {
            return searchByProgramUuid(context, patientProgramUuid);
        } else if (patientUuid != null) {
            return searchByPatientUuid(context, patientUuid);
        } else {
            return super.doSearch(context);
        }
    }

    private PageableResult searchByPatientUuid(RequestContext context, String patientUuid) {
        PatientService patientService = Context.getPatientService();
        Patient patient = patientService.getPatientByUuid(patientUuid);
        if (patient == null) {
            return new EmptySearchResult();
        } else {
            List patientPrograms = Context.getService(BahmniProgramWorkflowService.class).getPatientPrograms(patient, (Program) null, (Date) null, (Date) null, (Date) null, (Date) null, context.getIncludeAll());
            return new NeedsPaging(patientPrograms, context);
        }
    }

    private PageableResult searchByProgramUuid(RequestContext context, String patientProgramUuid) {
        PatientProgram byUniqueId = getByUniqueId(patientProgramUuid);
        if (byUniqueId == null) {
            return new EmptySearchResult();
        } else {
            return new AlreadyPaged<>(context, Collections.singletonList(byUniqueId), false);
        }
    }
}

package org.bahmni.module.bahmnicore.openmrsadvice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class PatientAdvice extends BaseAdvice implements AfterReturningAdvice {

    private static final String TEMPLATE = "/openmrs/ws/rest/v1/patient/%s?v=full";
    private static final String CATEGORY = "patient";
    private static final String TITLE = "Patient";
    private static final String SAVE_PATIENT_METHOD = "savePatient";
    private static final String EVENT_RAISE_FLAG_GP = "eventoutbox.publish.eventsForPatient";
    private static final String URL_TEMPLATE_GP = "eventoutbox.publish.urlTemplateForPatient";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;

    public PatientAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (method.getName().equals(SAVE_PATIENT_METHOD) && shouldRaiseEvent()) {
            Patient patient = (Patient) returnValue;
            String patientUuid = patient.getUuid();
            String restUrl = getPatientUrl(patientUuid);
            EMREvent<Patient> emrEvent = new EMREvent<>(patient, CATEGORY, TITLE, null, restUrl);
            eventPublisher.publishEvent(emrEvent);
            log.info("Successfully published EMREvent with uuid: {}", patientUuid);
        }
    }

    @Override
    protected String getDefaultUrlTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getEventRaiseFlagGlobalProperty() {
        return EVENT_RAISE_FLAG_GP;
    }

    @Override
    protected String getUrlTemplateGlobalProperty() {
        return URL_TEMPLATE_GP;
    }
    private String getPatientUrl(String patientUuid) {
        String globalProperty = Context.getAdministrationService().getGlobalProperty(getUrlTemplateGlobalProperty());
        if(globalProperty == null || globalProperty.isEmpty()) {
            globalProperty = getDefaultUrlTemplate();
        }
        return String.format(globalProperty, patientUuid);
    }
}

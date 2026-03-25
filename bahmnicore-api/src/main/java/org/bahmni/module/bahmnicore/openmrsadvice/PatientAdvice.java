package org.bahmni.module.bahmnicore.openmrsadvice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Set;

public class PatientAdvice extends BaseAdvice implements AfterReturningAdvice {

    private static final String TEMPLATE = "/openmrs/ws/rest/v1/patient/{uuid}?v=full";
    private static final String CATEGORY = "patient";
    private static final String TITLE = "Patient";
    private static final String SAVE_PATIENT_METHOD = "savePatient";
    private static final String eventRaiseFlagGP = "atomfeed.publish.eventsForPatient";
    private static final String urlTemplateGP = "atomfeed.publish.urlTemplateForPatient";

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
            String restUrl = getUrlPattern(patientUuid);
            EMREvent<Patient> emrEvent = new EMREvent<>(patient, CATEGORY, TITLE, null, restUrl);
            eventPublisher.publishEvent(emrEvent);
            log.info("Successfully published EMREvent with uuid: " + patientUuid);
        }
    }

    @Override
    protected String getDefaultUrlTemplate() {
        return TEMPLATE;
    }

    @Override
    protected String getEventRaiseFlagGlobalProperty() {
        return eventRaiseFlagGP;
    }

    @Override
    protected String getUrlTemplateGlobalProperty() {
        return urlTemplateGP;
    }
}

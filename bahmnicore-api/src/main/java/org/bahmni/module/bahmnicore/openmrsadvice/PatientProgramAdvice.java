package org.bahmni.module.bahmnicore.openmrsadvice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class PatientProgramAdvice extends BaseAdvice implements AfterReturningAdvice {

    private static final String DEFAULT_PATIENT_PROGRAM_URL_PATTERN = "/openmrs/ws/rest/v1/programenrollment/{uuid}?v=full";
    private static final String CATEGORY = "programenrollment";
    private static final String TITLE = "Program Enrollment";
    private static final String SAVE_PATIENT_PROGRAM_METHOD = "savePatientProgram";
    private static final String RAISE_PATIENT_PROGRAM_EVENT_GLOBAL_PROPERTY = "eventoutbox.publish.eventsForPatientProgramStateChange";
    private static final String PATIENT_PROGRAM_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForProgramStateChange";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;

    public PatientProgramAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (method.getName().equals(SAVE_PATIENT_PROGRAM_METHOD) && shouldRaiseEvent()) {
            PatientProgram patientProgram = (PatientProgram) returnValue;
            String programUuid = patientProgram.getUuid();
            String restUrl = getUrlPattern(programUuid);
            EMREvent<PatientProgram> emrEvent = new EMREvent<>(patientProgram, CATEGORY, TITLE, null, restUrl);
            eventPublisher.publishEvent(emrEvent);
            log.info("Successfully published EMREvent with uuid: {}", programUuid);
        }
    }

    @Override
    protected String getDefaultUrlTemplate() {
        return DEFAULT_PATIENT_PROGRAM_URL_PATTERN;
    }

    @Override
    protected String getEventRaiseFlagGlobalProperty() {
        return RAISE_PATIENT_PROGRAM_EVENT_GLOBAL_PROPERTY;
    }

    @Override
    protected String getUrlTemplateGlobalProperty() {
        return PATIENT_PROGRAM_EVENT_URL_PATTERN_GLOBAL_PROPERTY;
    }
}

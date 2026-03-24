package org.bahmni.module.bahmnicore.openmrsadvice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.eventoutbox.EventAction;
import org.openmrs.PatientProgram;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PatientProgramAdvice extends BaseAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

    private static final String DEFAULT_PATIENT_PROGRAM_URL_PATTERN = "/openmrs/ws/rest/v1/programenrollment/{uuid}?v=full";
    private static final String CATEGORY = "programenrollment";
    private static final String TITLE = "Program Enrollment";
    private static final String SAVE_PATIENT_PROGRAM_METHOD = "savePatientProgram";
    private static final String RAISE_PATIENT_PROGRAM_EVENT_GLOBAL_PROPERTY = "eventoutbox.publish.eventsForPatientProgramStateChange";
    private static final String PATIENT_PROGRAM_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForProgramStateChange";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;
    private final ThreadLocal<Map<String, Integer>> threadLocal = new ThreadLocal<>();
    private final String PATIENT_PROGRAM_ID_KEY = "patientProgramId";
    private final Set<String> adviceMethodNames = Sets.newHashSet(SAVE_PATIENT_PROGRAM_METHOD);

    public PatientProgramAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (adviceMethodNames.contains(method.getName()) && shouldRaiseEvent()) {
            Map<String, Integer> patientProgramInfo = threadLocal.get();
            if (patientProgramInfo != null) {
                EventAction action = patientProgramInfo.get(PATIENT_PROGRAM_ID_KEY) == null ? EventAction.CREATED : EventAction.UPDATED;
                threadLocal.remove();

                PatientProgram patientProgram = (PatientProgram) returnValue;
                String programUuid = patientProgram.getUuid();
                String restUrl = getUrlPattern(programUuid);
                EMREvent<PatientProgram> emrEvent = new EMREvent<>(patientProgram, action, CATEGORY, TITLE, null, restUrl);
                eventPublisher.publishEvent(emrEvent);
                log.info("Successfully published EMREvent with uuid: " + programUuid);
            }
        }
    }

    @Override
    public void before(Method method, Object[] objects, Object o) {
        if (adviceMethodNames.contains(method.getName()) && shouldRaiseEvent()) {
            PatientProgram patientProgram = (PatientProgram) objects[0];
            Map<String, Integer> patientProgramInfo = new HashMap<>(1);
            patientProgramInfo.put(PATIENT_PROGRAM_ID_KEY, patientProgram.getId());
            threadLocal.set(patientProgramInfo);
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

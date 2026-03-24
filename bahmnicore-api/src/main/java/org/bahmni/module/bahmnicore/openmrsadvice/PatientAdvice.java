package org.bahmni.module.bahmnicore.openmrsadvice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.eventoutbox.EventAction;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PatientAdvice extends BaseAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

    private static final String TEMPLATE = "/openmrs/ws/rest/v1/patient/{uuid}?v=full";
    private static final String CATEGORY = "patient";
    private static final String TITLE = "Patient";
    private static final String SAVE_PATIENT_METHOD = "savePatient";
    private static final String eventRaiseFlagGP = "eventoutbox.publish.eventsForPatient";
    private static final String urlTemplateGP = "eventoutbox.publish.urlTemplateForPatient";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;
    private final ThreadLocal<Map<String, Integer>> threadLocal = new ThreadLocal<>();
    private final String PATIENT_ID_KEY = "patientId";
    private final Set<String> adviceMethodNames = Sets.newHashSet(SAVE_PATIENT_METHOD);

    public PatientAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (adviceMethodNames.contains(method.getName()) && shouldRaiseEvent()) {
            Map<String, Integer> patientInfo = threadLocal.get();
            if (patientInfo != null) {
                EventAction action = patientInfo.get(PATIENT_ID_KEY) == null ? EventAction.CREATED : EventAction.UPDATED;
                threadLocal.remove();

                Patient patient = (Patient) returnValue;
                String patientUuid = patient.getUuid();
                String restUrl = getUrlPattern(patientUuid);
                EMREvent<Patient> emrEvent = new EMREvent<>(patient, action, CATEGORY, TITLE, null, restUrl);
                eventPublisher.publishEvent(emrEvent);
                log.info("Successfully published EMREvent with uuid: " + patientUuid);
            }
        }
    }

    @Override
    public void before(Method method, Object[] objects, Object o) {
        if (adviceMethodNames.contains(method.getName()) && shouldRaiseEvent()) {
            Patient patient = (Patient) objects[0];
            Map<String, Integer> patientInfo = new HashMap<>(1);
            patientInfo.put(PATIENT_ID_KEY, patient.getId());
            threadLocal.set(patientInfo);
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

package org.bahmni.module.bahmnicore.events.advice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.BahmniEventType;
import org.bahmni.module.bahmnicore.events.PatientEvent;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.bahmni.module.bahmnicore.events.BahmniEventType.BAHMNI_PATIENT_CREATED;
import static org.bahmni.module.bahmnicore.events.BahmniEventType.BAHMNI_PATIENT_UPDATED;


public class PatientAdvice implements AfterReturningAdvice, MethodBeforeAdvice {
	
	private final Logger log = LogManager.getLogger(PatientAdvice.class);
	private final BahmniEventPublisher eventPublisher;
	private final ThreadLocal<Map<String,Integer>> threadLocal = new ThreadLocal<>();
	private final String PATIENT_ID_KEY = "patientId";
	private final Set<String> adviceMethodNames = Sets.newHashSet("savePatient");

	public PatientAdvice() {
		this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
	}

	@Override
	public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
		if (adviceMethodNames.contains(method.getName())) {
			Map<String, Integer> patientInfo = threadLocal.get();
			if (patientInfo != null) {
				BahmniEventType eventType = patientInfo.get(PATIENT_ID_KEY) == null ? BAHMNI_PATIENT_CREATED : BAHMNI_PATIENT_UPDATED;
				threadLocal.remove();

				Patient patient = (Patient) returnValue;
				PatientEvent patientEvent =new PatientEvent(eventType,patient);
				eventPublisher.publishEvent(patientEvent);

				log.info("Successfully published event with uuid : " + patient.getUuid());
			}
		}
	}
	@Override
	public void before(Method method, Object[] objects, Object o) {
		if (adviceMethodNames.contains(method.getName())) {
			Patient patient = (Patient) objects[0];

			Map<String, Integer> patientInfo = new HashMap<>(1);
			patientInfo.put(PATIENT_ID_KEY, patient.getId());
			threadLocal.set(patientInfo);
		}
	}
}

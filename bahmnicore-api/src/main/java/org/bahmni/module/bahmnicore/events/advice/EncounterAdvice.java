package org.bahmni.module.bahmnicore.events.advice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.BahmniEventType;
import org.bahmni.module.bahmnicore.events.EncounterEvent;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.bahmni.module.bahmnicore.events.BahmniEventType.BAHMNI_ENCOUNTER_CREATED;
import static org.bahmni.module.bahmnicore.events.BahmniEventType.BAHMNI_ENCOUNTER_UPDATED;


public class EncounterAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;
    private final ThreadLocal<Map<String, Integer>> threadLocal = new ThreadLocal<>();
    private final String ENCOUNTER_ID_KEY = "encounterId";
    private final Set<String> adviceMethodNames = Sets.newHashSet("saveEncounter");

    public EncounterAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (adviceMethodNames.contains(method.getName())) {
            Map<String, Integer> encounterInfo = threadLocal.get();
            if (encounterInfo != null) {
                BahmniEventType eventType = encounterInfo.get(ENCOUNTER_ID_KEY) == null ? BAHMNI_ENCOUNTER_CREATED : BAHMNI_ENCOUNTER_UPDATED;
                threadLocal.remove();

                Encounter encounter = (Encounter) returnValue;
                EncounterEvent encounterEvent = new EncounterEvent(eventType, encounter);
                eventPublisher.publishEvent(encounterEvent);

                log.info("Successfully published event with uuid : " + encounter.getUuid());
            }
        }
    }

    @Override
    public void before(Method method, Object[] objects, Object o) {
        if (adviceMethodNames.contains(method.getName())) {
            Encounter encounter = (Encounter) objects[0];
            Map<String, Integer> encounterInfo = new HashMap<>(1);
            encounterInfo.put(ENCOUNTER_ID_KEY, encounter.getId());
            threadLocal.set(encounterInfo);
        }
    }
}

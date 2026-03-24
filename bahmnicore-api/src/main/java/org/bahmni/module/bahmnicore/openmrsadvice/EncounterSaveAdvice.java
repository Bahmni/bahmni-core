package org.bahmni.module.bahmnicore.openmrsadvice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.eventoutbox.EventAction;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EncounterSaveAdvice extends BaseAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

    public static final String ENCOUNTER_REST_URL = "/openmrs/ws/rest/v1/encounter/{uuid}?v=full";
    public static final String TITLE = "Encounter";
    public static final String CATEGORY = "Encounter";
    private static final String SAVE_METHOD = "saveEncounter";
    private static final String eventRaiseFlagGP = "eventoutbox.publish.eventsForEncounter";
    private static final String urlTemplateGP = "eventoutbox.publish.urlTemplateForEncounter";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;
    private final ThreadLocal<Map<String, Integer>> threadLocal = new ThreadLocal<>();
    private final String ENCOUNTER_ID_KEY = "encounterId";
    private final Set<String> adviceMethodNames = Sets.newHashSet(SAVE_METHOD);

    public EncounterSaveAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (adviceMethodNames.contains(method.getName()) && shouldRaiseEvent()) {
            Map<String, Integer> encounterInfo = threadLocal.get();
            if (encounterInfo != null) {
                EventAction action = encounterInfo.get(ENCOUNTER_ID_KEY) == null ? EventAction.CREATED : EventAction.UPDATED;
                threadLocal.remove();

                Encounter encounter = (Encounter) returnValue;
                String encounterUuid = encounter.getUuid();
                String restUrl = getUrlPattern(encounterUuid);
                EMREvent<Encounter> emrEvent = new EMREvent<>(encounter, action, CATEGORY, TITLE, null, restUrl);
                eventPublisher.publishEvent(emrEvent);
                log.info("Successfully published EMREvent with uuid: " + encounterUuid);
            }
        }
    }

    @Override
    public void before(Method method, Object[] objects, Object o) {
        if (adviceMethodNames.contains(method.getName()) && shouldRaiseEvent()) {
            Encounter encounter = (Encounter) objects[0];
            Map<String, Integer> encounterInfo = new HashMap<>(1);
            encounterInfo.put(ENCOUNTER_ID_KEY, encounter.getId());
            threadLocal.set(encounterInfo);
        }
    }

    @Override
    protected String getDefaultUrlTemplate() {
        return ENCOUNTER_REST_URL;
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

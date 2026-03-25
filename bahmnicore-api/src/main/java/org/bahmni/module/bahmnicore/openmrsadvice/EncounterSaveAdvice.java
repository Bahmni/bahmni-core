package org.bahmni.module.bahmnicore.openmrsadvice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Set;

public class EncounterSaveAdvice implements AfterReturningAdvice {

    public static final String ENCOUNTER_REST_URL = getEncounterFeedUrl();
    public static final String TITLE = "Encounter";
    public static final String CATEGORY = "Encounter";
    private static final String SAVE_METHOD = "save";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;

    public EncounterSaveAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (method.getName().equals(SAVE_METHOD)) {
            Encounter encounter = (Encounter) returnValue;
            String encounterUuid = encounter.getUuid();
            String restUrl = String.format(ENCOUNTER_REST_URL, encounterUuid);
            EMREvent<Encounter> emrEvent = new EMREvent<>(encounter, CATEGORY, TITLE, null, restUrl);
            eventPublisher.publishEvent(emrEvent);
            log.info("Successfully published EMREvent with uuid: " + encounterUuid);
        }
    }

    private static String getEncounterFeedUrl() {
        return Context.getAdministrationService().getGlobalProperty("encounter.feed.publish.url");
    }
}

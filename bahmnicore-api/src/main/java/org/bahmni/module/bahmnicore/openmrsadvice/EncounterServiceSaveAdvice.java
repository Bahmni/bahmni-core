package org.bahmni.module.bahmnicore.openmrsadvice;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;

public class EncounterServiceSaveAdvice implements AfterReturningAdvice {

    public static final String ENCOUNTER_REST_URL = getEncounterFeedUrl();
    public static final String TITLE = "Encounter";
    public static final String CATEGORY = "Encounter";
    private static final String SAVE_ENCOUNTER_METHOD = "saveEncounter";
    private static final String ENCOUNTER_TYPE_INVESTIGATION = "INVESTIGATION";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;

    public EncounterServiceSaveAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (method.getName().equals(SAVE_ENCOUNTER_METHOD)) {
            Encounter encounter = (Encounter) returnValue;
            if (ENCOUNTER_TYPE_INVESTIGATION.equals(encounter.getEncounterType().getName())) {
                String encounterUuid = encounter.getUuid();
                String restUrl = String.format(ENCOUNTER_REST_URL, encounterUuid);
                EMREvent<Encounter> emrEvent = new EMREvent<>(encounter, CATEGORY, TITLE, null, restUrl);
                eventPublisher.publishEvent(emrEvent);
                log.info("Successfully published EMREvent with uuid: {}", encounterUuid);
            }
        }
    }

    private static String getEncounterFeedUrl() {
        return Context.getAdministrationService().getGlobalProperty("bahmnievents.encounter.feed.publish.url");
    }
}

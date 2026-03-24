package org.bahmni.module.bahmnicore.openmrsadvice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.eventoutbox.EventAction;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PersonRelationshipAdvice extends BaseAdvice implements AfterReturningAdvice, MethodBeforeAdvice {

    private static final String DEFAULT_RELATIONSHIP_URL_PATTERN = "/openmrs/ws/rest/v1/relationship/{uuid}?v=full";
    private static final String CATEGORY = "relationship";
    private static final String TITLE = "Relationship";
    private static final String SAVE_RELATIONSHIP_METHOD = "saveRelationship";
    private static final String RAISE_RELATIONSHIP_EVENT_GLOBAL_PROPERTY = "eventoutbox.publish.eventsForPatientRelationshipChange";
    private static final String RELATIONSHIP_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "eventoutbox.event.urlPatternForPatientRelationshipChange";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;
    private final ThreadLocal<Map<String, Integer>> threadLocal = new ThreadLocal<>();
    private final String RELATIONSHIP_ID_KEY = "relationshipId";
    private final Set<String> adviceMethodNames = Sets.newHashSet(SAVE_RELATIONSHIP_METHOD);

    public PersonRelationshipAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (adviceMethodNames.contains(method.getName()) && shouldRaiseEvent()) {
            Map<String, Integer> relationshipInfo = threadLocal.get();
            if (relationshipInfo != null) {
                EventAction action = relationshipInfo.get(RELATIONSHIP_ID_KEY) == null ? EventAction.CREATED : EventAction.UPDATED;
                threadLocal.remove();

                Relationship relationship = (Relationship) returnValue;
                String relationshipUuid = relationship.getUuid();
                String restUrl = getUrlPattern(relationshipUuid);
                EMREvent<Relationship> emrEvent = new EMREvent<>(relationship, action, CATEGORY, TITLE, restUrl, restUrl);
                eventPublisher.publishEvent(emrEvent);
                log.info("Successfully published EMREvent with uuid: " + relationshipUuid);
            }
        }
    }

    @Override
    public void before(Method method, Object[] objects, Object o) {
        if (adviceMethodNames.contains(method.getName()) && shouldRaiseEvent()) {
            Relationship relationship = (Relationship) objects[0];
            Map<String, Integer> relationshipInfo = new HashMap<>(1);
            relationshipInfo.put(RELATIONSHIP_ID_KEY, relationship.getId());
            threadLocal.set(relationshipInfo);
        }
    }

    @Override
    protected String getDefaultUrlTemplate() {
        return DEFAULT_RELATIONSHIP_URL_PATTERN;
    }

    @Override
    protected String getEventRaiseFlagGlobalProperty() {
        return RAISE_RELATIONSHIP_EVENT_GLOBAL_PROPERTY;
    }

    @Override
    protected String getUrlTemplateGlobalProperty() {
        return RELATIONSHIP_EVENT_URL_PATTERN_GLOBAL_PROPERTY;
    }
}

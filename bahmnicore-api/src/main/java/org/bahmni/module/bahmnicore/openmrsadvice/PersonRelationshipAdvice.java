package org.bahmni.module.bahmnicore.openmrsadvice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.Relationship;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Set;

public class PersonRelationshipAdvice extends BaseAdvice implements AfterReturningAdvice {

    private static final String DEFAULT_RELATIONSHIP_URL_PATTERN = "/openmrs/ws/rest/v1/relationship/{uuid}?v=full";
    private static final String CATEGORY = "relationship";
    private static final String TITLE = "Relationship";
    private static final String SAVE_RELATIONSHIP_METHOD = "saveRelationship";
    private static final String RAISE_RELATIONSHIP_EVENT_GLOBAL_PROPERTY = "bahmnievents.publish.eventsForPatientRelationshipChange";
    private static final String RELATIONSHIP_EVENT_URL_PATTERN_GLOBAL_PROPERTY = "bahmnievents.event.urlPatternForPatientRelationshipChange";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;

    public PersonRelationshipAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (method.getName().equals(SAVE_RELATIONSHIP_METHOD) && shouldRaiseEvent()) {
            Relationship relationship = (Relationship) returnValue;
            String relationshipUuid = relationship.getUuid();
            String restUrl = getUrlPattern(relationshipUuid);
            EMREvent<Relationship> emrEvent = new EMREvent<>(relationship, CATEGORY, TITLE, null, restUrl);
            eventPublisher.publishEvent(emrEvent);
            log.info("Successfully published EMREvent with uuid: " + relationshipUuid);
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

package org.bahmni.module.bahmnicore.openmrsadvice;

import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.Set;

public class LocationAdvice extends BaseAdvice implements AfterReturningAdvice {

    private static final String TEMPLATE = "/openmrs/ws/rest/v1/location/{uuid}?v=full";
    private static final String CATEGORY = "location";
    private static final String TITLE = "Location";
    private static final String SAVE_METHOD = "saveLocation";
    private static final String eventRaiseFlagGP = "atomfeed.publish.eventsForLocation";
    private static final String urlTemplateGP = "atomfeed.publish.urlTemplateForLocation";

    private final Logger log = LogManager.getLogger(this.getClass());
    private final BahmniEventPublisher eventPublisher;

    public LocationAdvice() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object target) {
        if (method.getName().equals(SAVE_METHOD) && shouldRaiseEvent()) {
            Location location = (Location) returnValue;
            String locationUuid = location.getUuid();
            String restUrl = getUrlPattern(locationUuid);
            EMREvent<Location> emrEvent = new EMREvent<>(location, CATEGORY, TITLE, null, restUrl);
            eventPublisher.publishEvent(emrEvent);
            log.info("Successfully published EMREvent with uuid: " + locationUuid);
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

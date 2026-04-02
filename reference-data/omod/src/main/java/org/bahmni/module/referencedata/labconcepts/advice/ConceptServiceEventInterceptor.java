package org.bahmni.module.referencedata.labconcepts.advice;

import org.bahmni.module.bahmnicore.events.eventPublisher.BahmniEventPublisher;
import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.referencedata.labconcepts.model.Operation;
import org.openmrs.api.context.Context;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public class ConceptServiceEventInterceptor implements AfterReturningAdvice {

    private final BahmniEventPublisher eventPublisher;

    public ConceptServiceEventInterceptor() {
        this.eventPublisher = Context.getRegisteredComponent("bahmniEventPublisher", BahmniEventPublisher.class);
    }

    public ConceptServiceEventInterceptor(BahmniEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object conceptService) {
        Operation operation = new Operation(method);
        List<EMREvent<?>> events = operation.apply(arguments);
        if (isNotEmpty(events)) {
            for (EMREvent<?> event : events) {
                eventPublisher.publishEvent(event);
            }
        }
    }
}

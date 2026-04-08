package org.bahmni.module.referencedata.labconcepts.advice;

import org.bahmni.module.eventoutbox.EMREvent;
import org.bahmni.module.referencedata.events.ReferenceDataEventPublisher;
import org.bahmni.module.referencedata.labconcepts.model.Operation;
import org.openmrs.api.context.ServiceContext;
import org.springframework.aop.AfterReturningAdvice;

import java.lang.reflect.Method;
import java.util.List;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

public class ConceptServiceEventInterceptor implements AfterReturningAdvice {

    private ReferenceDataEventPublisher eventPublisher;

    public ConceptServiceEventInterceptor() {
    }

    public ConceptServiceEventInterceptor(ReferenceDataEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setEventPublisher(ReferenceDataEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void afterReturning(Object returnValue, Method method, Object[] arguments, Object conceptService) {
        Operation operation = new Operation(method);
        List<EMREvent<?>> events = operation.apply(arguments);
        if (isNotEmpty(events)) {
            for (EMREvent<?> event : events) {
                getEventPublisher().publishEvent(event);
            }
        }
    }

    private ReferenceDataEventPublisher getEventPublisher() {
        if (eventPublisher == null) {
            eventPublisher = ServiceContext.getInstance().getApplicationContext().getBean(ReferenceDataEventPublisher.class);
        }
        return eventPublisher;
    }
}

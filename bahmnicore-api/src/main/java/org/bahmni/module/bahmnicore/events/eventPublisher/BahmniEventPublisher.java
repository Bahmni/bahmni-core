package org.bahmni.module.bahmnicore.events.eventPublisher;

import org.bahmni.module.bahmnicore.events.BahmniEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class BahmniEventPublisher implements ApplicationEventPublisherAware {

    private ApplicationEventPublisher eventPublisher;

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }
    public void publishEvent(BahmniEvent event) {
        this.eventPublisher.publishEvent(event);
    }
}
package org.bahmni.module.bahmnicore.events;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

import java.time.LocalDateTime;
import java.util.UUID;

public class BahmniEvent {

    private static final long version = 1L;
    public UserContext userContext;
    public String eventId;
    public BahmniEventType eventType;
    public String payloadId;
    public LocalDateTime publishedDateTime;

    public BahmniEvent(BahmniEventType bahmniEventType) {
        this.eventType = bahmniEventType;
        this.eventId = UUID.randomUUID().toString();
        this.publishedDateTime = LocalDateTime.now();
        this.userContext= Context.getUserContext();
        this.payloadId="";
    }
}


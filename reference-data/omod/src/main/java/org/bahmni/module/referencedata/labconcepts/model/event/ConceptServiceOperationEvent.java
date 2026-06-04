package org.bahmni.module.referencedata.labconcepts.model.event;

import org.ict4h.atomfeed.server.service.Event;

import java.net.URISyntaxException;

public interface ConceptServiceOperationEvent {
    public Event asAtomFeedEvent(Object[] arguments) throws URISyntaxException;
    public Boolean isApplicable(String operation, Object[] arguments);
}

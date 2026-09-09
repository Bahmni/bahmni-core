package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.module.eventoutbox.EMREvent;

public interface ConceptServiceOperationEvent {
    EMREvent<?> asEMREvent(Object[] arguments);
    Boolean isApplicable(String operation, Object[] arguments);
}

package org.bahmni.module.bahmnicore.events;

import org.openmrs.Encounter;

public class EncounterEvent extends BahmniEvent {

    private Encounter encounter;

    public EncounterEvent(BahmniEventType bahmniEventType, Encounter encounter) {
        super(bahmniEventType);
        this.encounter = encounter;
        this.payloadId=encounter.getUuid();
    }

    public Encounter getEncounter() {
        return encounter;
    }
}


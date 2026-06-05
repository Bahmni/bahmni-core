package org.openmrs.module.bahmniemrapi.encountertransaction.impl;

import org.openmrs.Encounter;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.springframework.stereotype.Component;

@Component
public class MockEncounterTransactionHandler implements EncounterTransactionHandler {
    public int numberOfTimesSaveWasCalled = 0;

    @Override
    public void forRead(Encounter encounter, EncounterTransaction encounterTransaction) {

    }

    @Override
    public void forSave(Encounter encounter, EncounterTransaction encounterTransaction) {
        numberOfTimesSaveWasCalled++;
    }
}

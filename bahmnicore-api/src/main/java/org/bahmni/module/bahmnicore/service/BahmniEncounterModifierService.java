package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.encounter.data.EncounterModifierData;
import org.bahmni.module.bahmnicore.encounterModifier.exception.CannotModifyEncounterException;

import java.io.IOException;

public interface BahmniEncounterModifierService {
    public EncounterModifierData getModifiedEncounter(EncounterModifierData encounterModifierData) throws IllegalAccessException, IOException, InstantiationException, CannotModifyEncounterException;
}

package org.bahmni.module.bahmnicore.encounterModifier;

import org.bahmni.module.bahmnicore.contract.encounter.data.EncounterModifierData;
import org.bahmni.module.bahmnicore.encounterModifier.exception.CannotModifyEncounterException;

public abstract class EncounterModifier {
    public abstract EncounterModifierData run(EncounterModifierData encounterModifierData) throws CannotModifyEncounterException;
}

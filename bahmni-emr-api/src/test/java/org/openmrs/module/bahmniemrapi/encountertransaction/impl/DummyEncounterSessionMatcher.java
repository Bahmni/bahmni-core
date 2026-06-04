package org.openmrs.module.bahmniemrapi.encountertransaction.impl;

import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher;
import org.springframework.stereotype.Component;

@Component
public class DummyEncounterSessionMatcher implements BaseEncounterMatcher{
    @Override
    public Encounter findEncounter(Visit visit, EncounterParameters encounterParameters) {
        return null;
    }
}

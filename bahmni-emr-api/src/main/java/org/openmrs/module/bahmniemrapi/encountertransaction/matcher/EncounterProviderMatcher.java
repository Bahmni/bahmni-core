package org.openmrs.module.bahmniemrapi.encountertransaction.matcher;

import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.EncounterParameters;
import org.openmrs.module.emrapi.encounter.matcher.BaseEncounterMatcher;

import java.util.Map;
import java.util.Set;


public class EncounterProviderMatcher implements BaseEncounterMatcher {

    @Override
    public Encounter findEncounter(Visit visit, EncounterParameters encounterParameters) {
        EncounterType encounterType = encounterParameters.getEncounterType();
        Provider provider = null;
        if (encounterParameters.getProviders() != null && encounterParameters.getProviders().iterator().hasNext())
            provider = encounterParameters.getProviders().iterator().next();

        if (encounterType == null) {
            throw new IllegalArgumentException("Encounter Type not found");
        }

        if (visit.getEncounters() != null) {
            for (Encounter encounter : visit.getEncounters()) {
                if (encounterType.equals(encounter.getEncounterType()) && isSameProvider(provider, encounter)) {
                    return encounter;
                }
            }
        }
        return null;
    }

    private boolean isSameProvider(Provider provider, Encounter encounter) {
        final Map<EncounterRole, Set<Provider>> providersByRoles = encounter.getProvidersByRoles();
        if (provider == null || providersByRoles.isEmpty()) {
            return false;
        }

        for (Set<Provider> providers : providersByRoles.values()) {
            for (Provider encounterProvider : providers) {
                if(encounterProvider.getPerson().getId().equals(provider.getPerson().getId())){
                    return  true;
                }
            }
        }
        return false;
    }
}

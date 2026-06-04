package org.bahmni.module.elisatomfeedclient.api.worker;

import org.openmrs.Encounter;
import org.openmrs.Provider;
import org.openmrs.api.ProviderService;

public class ProviderHelper {
    private ProviderService providerService;

    public ProviderHelper(ProviderService providerService) {
        this.providerService = providerService;
    }

    public static Provider getProviderFrom(Encounter encounter) {
        return encounter.getEncounterProviders().iterator().next().getProvider();
    }

    public Provider getProviderByUuidOrReturnDefault(String providerUuid, String defaultProviderIdentifier) {
        Provider provider = providerService.getProviderByUuid(providerUuid);
        if (provider != null) {
            return provider;
        }
        if (defaultProviderIdentifier != null) {
            return providerService.getProviderByIdentifier(defaultProviderIdentifier);
        }
        return null;
    }

}

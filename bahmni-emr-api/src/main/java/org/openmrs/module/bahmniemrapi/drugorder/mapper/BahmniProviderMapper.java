package org.openmrs.module.bahmniemrapi.drugorder.mapper;

import org.openmrs.Provider;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class BahmniProviderMapper {
    public EncounterTransaction.Provider map(Provider provider) {
        EncounterTransaction.Provider result = new EncounterTransaction.Provider();
        result.setUuid(provider.getUuid());
        result.setName(provider.getName());
        return result;
    }
}

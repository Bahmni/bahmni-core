package org.openmrs.module.bahmniemrapi.drugorder.mapper;

import org.junit.Test;
import org.openmrs.Provider;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class BahmniProviderMapperTest {

    @Test
    public void shouldMapOpenMRSProviderToEncounterTransactionProvider() {
        Provider openMRSProvider = new Provider();
        openMRSProvider.setUuid("86526ed5-3c11-11de-a0ba-001e378eb671");
        openMRSProvider.setName("Superman");
        EncounterTransaction.Provider provider = new BahmniProviderMapper().map(openMRSProvider);
        assertThat(provider.getUuid(), is(equalTo(openMRSProvider.getUuid())));
        assertThat(provider.getName(), is(equalTo(openMRSProvider.getName())));
    }
}
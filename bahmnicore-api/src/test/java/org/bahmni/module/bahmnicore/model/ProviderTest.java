package org.bahmni.module.bahmnicore.model;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProviderTest {

    private String providerName = "providerName";
    private String providerUuid = "provider-uuid";
    private Provider provider;

    @Before
    public void setUp() {
        provider = new Provider();
        provider.setProviderName(providerName);
        provider.setUuid(providerUuid);
    }

    @Test
    public void shouldReturnTrueWhenTwoProvidersAreSameByReference() {

        assertEquals(provider, provider);
    }

    @Test
    public void shouldReturnFalseWhenOneOfTheProvidersIsNull() {

        assertNotEquals(provider, null);
    }

    @Test
    public void shouldReturnFalseWhenTypeOfTheObjectDoesNotEqualToProvider() {

        assertNotEquals(provider, "");
    }

    @Test
    public void shouldReturnFalseWhenProviderNameDoesNotMatch() {
        Provider otherProvider = new Provider();
        otherProvider.setProviderName("some provider name");
        otherProvider.setUuid(providerUuid);

        assertNotEquals(provider, otherProvider);
    }

    @Test
    public void shouldReturnTrueWhenProviderNameAndUuidMatches() {

        Provider otherProvider = new Provider();
        otherProvider.setProviderName(providerName);
        otherProvider.setUuid(providerUuid);

        assertEquals(provider, otherProvider);
    }
}
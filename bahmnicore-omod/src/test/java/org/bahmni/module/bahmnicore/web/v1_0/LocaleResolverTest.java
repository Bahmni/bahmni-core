package org.bahmni.module.bahmnicore.web.v1_0;

import static org.bahmni.module.bahmnicore.web.v1_0.LocaleResolver.identifyLocale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.openmrs.util.LocaleUtility;

import java.util.Locale;

public class LocaleResolverTest {

    @Test
    public void shouldReturnDefaultLocaleIfNull() {
        Locale locale = identifyLocale(null);
        assertEquals(LocaleUtility.getDefaultLocale(), locale);
    }

    @Test
    public void shouldReturnDefaultLocaleIfEmpty() {
        Locale locale = identifyLocale("");
        assertEquals(LocaleUtility.getDefaultLocale(), locale);
    }

    @Test
    public void shouldReturnParsedLocaleIfValid() {
        Locale locale = identifyLocale("en_US");
        assertEquals(new Locale("en", "US"), locale);
    }

    @Test(expected = AssertionError.class)
    public void shouldThrowExceptionIfInvalidLocale() {
        identifyLocale("invalid");
        fail("Should have thrown exception");
    }

}
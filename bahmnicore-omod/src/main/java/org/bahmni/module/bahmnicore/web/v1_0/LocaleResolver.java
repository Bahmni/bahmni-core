package org.bahmni.module.bahmnicore.web.v1_0;

import org.openmrs.api.APIException;
import org.openmrs.util.LocaleUtility;

import java.util.Locale;

public class LocaleResolver {

    public static Locale identifyLocale(String locale) {
        if (locale != null && !locale.isEmpty()) {
            Locale searchLocale = LocaleUtility.fromSpecification(locale);
            if (searchLocale.getLanguage().isEmpty()) {
                throw new APIException("Invalid locale: " + locale);
            }
            return searchLocale;
        } else {
            return LocaleUtility.getDefaultLocale();
        }
    }
}

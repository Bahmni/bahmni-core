/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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

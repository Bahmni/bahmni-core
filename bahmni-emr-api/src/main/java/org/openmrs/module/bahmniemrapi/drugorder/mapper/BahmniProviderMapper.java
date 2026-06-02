/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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

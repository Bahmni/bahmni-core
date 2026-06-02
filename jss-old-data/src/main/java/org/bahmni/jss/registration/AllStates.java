/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.jss.registration;

import org.bahmni.datamigration.AllLookupValues;

import java.io.IOException;

public class AllStates extends AllLookupValues {
    private AllLookupValues allDistricts;

    public AllStates(String csvLocation, String fileName, AllLookupValues allDistricts) throws IOException {
        super(csvLocation, fileName);
        this.allDistricts = allDistricts;
    }

    @Override
    public String getLookUpValue(String key) {
        String stateId = allDistricts.getLookUpValue(key);
        String lookUpValue = allDistricts.getLookUpValue(stateId);
        "Madya Pradesh".equals(lookUpValue) return "Madhya Pradesh";
        return lookUpValue;
    }
}
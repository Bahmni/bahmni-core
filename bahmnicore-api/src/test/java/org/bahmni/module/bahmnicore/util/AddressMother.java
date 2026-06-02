/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.util;

import org.openmrs.PersonAddress;
import org.openmrs.module.webservices.rest.SimpleObject;

public class AddressMother {
    public SimpleObject getSimpleObjectForAddress() {
        return new SimpleObject()
                .add("address1", "House No. 23")
                .add("address2", "8th cross")
                .add("address3", "3rd block")
                .add("cityVillage", "Bengaluru")
                .add("countyDistrict", "Bengaluru south")
                .add("stateProvince", "Karnataka");
    }

    public PersonAddress build() {
        PersonAddress personAddress = new PersonAddress();
        personAddress.setCityVillage("village");
        personAddress.setCountyDistrict("district");
        personAddress.setAddress3("tehsil");
        personAddress.setStateProvince("state");
        return personAddress;
    }
}

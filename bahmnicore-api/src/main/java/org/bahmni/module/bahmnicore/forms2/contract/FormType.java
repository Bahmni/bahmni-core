/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.forms2.contract;

public enum FormType {

    FORMS1("v1"), FORMS2("v2");

    private final String type;

    FormType(String type) {
        this.type = type;
    }


    public static FormType valueOfType(String value) {
        for(FormType v : values()) {
            if (v.type.equalsIgnoreCase(value)) return v;
        }
        throw new IllegalArgumentException();
    }

    public String getType() {
        return this.type;
    }


}

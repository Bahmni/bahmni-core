/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.datamigration.request.patient;

import static org.bahmni.datamigration.DataScrub.scrubData;

public class PatientAttribute {
    private String attributeType;
    private String name;
    private String value;

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = scrubData(name);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = scrubData(value);
    }
}
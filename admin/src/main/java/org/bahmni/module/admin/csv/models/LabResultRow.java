/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.csv.models;

import org.apache.commons.lang3.StringUtils;

public class LabResultRow {
    private String test;
    private String result;

    public LabResultRow() {
    }

    public LabResultRow(String test, String result) {
        this.test = test;
        this.result = result;
    }

    public String getTest() {
        return test;
    }

    public LabResultRow setTest(String test) {
        this.test = test;
        return this;
    }

    public String getResult() {
        return result;
    }

    public LabResultRow setResult(String result) {
        this.result = result;
        return this;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(test) && StringUtils.isBlank(result);
    }
}

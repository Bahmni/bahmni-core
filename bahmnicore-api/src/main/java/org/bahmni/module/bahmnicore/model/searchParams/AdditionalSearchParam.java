/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.model.searchParams;

public class AdditionalSearchParam {

    private String additionalSearchHandler;
    private String tests;

    public AdditionalSearchParam(String additionalSearchHandler, String tests) {
        this.additionalSearchHandler = additionalSearchHandler;
        this.tests = tests;
    }

    public AdditionalSearchParam() {
    }

    public String getAdditionalSearchHandler() {
        return additionalSearchHandler;
    }

    public void setAdditionalSearchHandler(String additionalSearchHandler) {
        this.additionalSearchHandler = additionalSearchHandler;
    }

    public String getTests(){
        return tests;
    }

    public void setTests(String tests){
        this.tests = tests;
    }
}



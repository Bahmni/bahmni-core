/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.contract;

public class AllTestsAndPanels extends Resource {
    public static final String ALL_TESTS_AND_PANELS = "All_Tests_and_Panels";

    private String description;

    private TestsAndPanels testsAndPanels;

    public TestsAndPanels getTestsAndPanels() {
        return testsAndPanels;
    }

    public void setTestsAndPanels(TestsAndPanels testsAndPanels) {
        this.testsAndPanels = testsAndPanels;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

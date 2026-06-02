/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.csv.models;

import org.junit.Test;
import org.springframework.util.Assert;

public class MultipleEncounterRowTest {
    @Test
    public void isEmptyReturnsTrueForEmptyRow() {
        Assert.isTrue(new MultipleEncounterRow().getNonEmptyEncounterRows().isEmpty(), "No data in encounter");

        MultipleEncounterRow emptyEncounterRow = new MultipleEncounterRowBuilder().getEmptyMultipleEncounterRow("GAN12345");
        Assert.isTrue(emptyEncounterRow.getNonEmptyEncounterRows().isEmpty(), "No data in encounter");
    }
}
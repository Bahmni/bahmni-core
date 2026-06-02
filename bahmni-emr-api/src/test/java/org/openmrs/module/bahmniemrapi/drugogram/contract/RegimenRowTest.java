/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.drugogram.contract;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class RegimenRowTest {

    @Test
    public void shouldReturnEmptyStringWhenDrugValueIsAbsent() {
        RegimenRow regimenRow = new RegimenRow();
        assertEquals("", regimenRow.getDrugValue("Paracetamol"));
    }

    @Test
    public void shouldGetDrugValueForDrugConceptName() {
        RegimenRow regimenRow = new RegimenRow();
        regimenRow.addDrugs("Paracetamol", "300.0");
        assertEquals("300.0", regimenRow.getDrugValue("Paracetamol"));
    }
}
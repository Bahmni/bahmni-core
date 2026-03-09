/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.elisatomfeedclient.api.domain;

import org.joda.time.LocalDate;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class OpenElisPatientTest {

    @Test
    public void shouldReturnBirthDateAsDate() throws Exception {
        OpenElisPatient openElisPatient = new OpenElisPatient();
        LocalDate today = LocalDate.now();
        openElisPatient.setDateOfBirth(today.toString("yyyy-MM-dd"));

        assertEquals(today.toDate(), openElisPatient.getDateOfBirthAsDate());
    }

}

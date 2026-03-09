/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.Age;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.junit.Test;
import org.openmrs.Patient;

import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class BirthDateMapperTest {

    @Test
    public void shouldMapFromPatientToBahmniPatient() {
        Patient patient = new Patient();
        patient.setBirthdate(new Date());

        BirthDateMapper mapper = new BirthDateMapper();
        BahmniPatient bahmniPatient = mapper.mapFromPatient(null, patient);

        assertEquals(patient.getBirthdate(),bahmniPatient.getBirthdate());
        assertEquals(new Age(0,0,0), bahmniPatient.getAge());
    }
}

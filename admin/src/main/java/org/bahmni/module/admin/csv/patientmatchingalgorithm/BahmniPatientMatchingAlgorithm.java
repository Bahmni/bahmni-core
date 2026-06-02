/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.csv.patientmatchingalgorithm;

import org.bahmni.csv.KeyValue;
import org.openmrs.Patient;

import java.util.List;

public class BahmniPatientMatchingAlgorithm extends PatientMatchingAlgorithm {
    @Override
    public Patient run(List<Patient> patientList, List<KeyValue> patientAttributes) {
        return patientList.size() > 0 ? patientList.get(0) : null;
    }
}

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
import org.bahmni.module.admin.csv.patientmatchingalgorithm.exception.CannotMatchPatientException;
import org.openmrs.Patient;

import java.util.List;

public abstract class PatientMatchingAlgorithm {
    public String valueFor(String keyToSearch, List<KeyValue> patientAttributes) {
        for (KeyValue patientAttributeKeyValue : patientAttributes) {
            if (patientAttributeKeyValue.getKey().equalsIgnoreCase(keyToSearch)) {
                return patientAttributeKeyValue.getValue();
            }
        }
        return null;
    }

    public abstract Patient run(List<Patient> patientList, List<KeyValue> patientAttributes) throws CannotMatchPatientException;
}

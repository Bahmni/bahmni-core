/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.csv.patientmatchingalgorithm.exception;

import org.openmrs.Patient;

import java.util.List;

public class CannotMatchPatientException extends Exception {
    private String patientIds = "";

    public CannotMatchPatientException() {
    }

    public CannotMatchPatientException(List<Patient> patients) {
        this.patientIds = getPatientIds(patients);
    }

    @Override
    public String getMessage() {
        return "No matching patients found. Potential matches:'" + patientIds + "'";
    }

    @Override
    public String toString() {
        return "CannotMatchPatientException{Potential matches patientIds='" + patientIds + "'}";
    }

    private String getPatientIds(List<Patient> patients) {
        if (patients == null)
            return "";

        StringBuffer patientIdsBuffer = new StringBuffer();
        for (Patient patient : patients) {
            patientIdsBuffer.append(patient.getPatientIdentifier().getIdentifier()).append(", ");
        }
        return patientIdsBuffer.toString();
    }
}

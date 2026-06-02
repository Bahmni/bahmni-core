/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.events;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

public class PatientEvent extends BahmniEvent {

    private Patient patient;

    public PatientEvent(BahmniEventType bahmniEventType, Patient patient) {
        super(bahmniEventType);
        this.patient = patient;
        this.payloadId=patient.getUuid();
    }

    public Patient getPatient() {
        return patient;
    }
}


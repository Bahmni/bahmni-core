/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.events;

public enum BahmniEventType {
    BAHMNI_PATIENT_CREATED("bahmni-patient"),
    BAHMNI_PATIENT_UPDATED("bahmni-patient"),
    BAHMNI_ENCOUNTER_CREATED("bahmni-encounter"),
    BAHMNI_ENCOUNTER_UPDATED("bahmni-encounter");

    private final String topic;
    BahmniEventType(String topic) {
        this.topic = topic;
    }
    public String topic() {
        return topic;
    }
}
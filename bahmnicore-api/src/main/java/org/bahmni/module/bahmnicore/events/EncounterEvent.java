/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.events;

import org.openmrs.Encounter;

public class EncounterEvent extends BahmniEvent {

    private Encounter encounter;

    public EncounterEvent(BahmniEventType bahmniEventType, Encounter encounter) {
        super(bahmniEventType);
        this.encounter = encounter;
        this.payloadId=encounter.getUuid();
    }

    public Encounter getEncounter() {
        return encounter;
    }
}


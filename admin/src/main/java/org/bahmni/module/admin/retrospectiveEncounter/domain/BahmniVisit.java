/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.retrospectiveEncounter.domain;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BahmniVisit {
    private Visit visit;

    public BahmniVisit(Visit visit) {
        this.visit = visit;
    }

    public List<Obs> obsFor(String requestedEncounterType) {
        List<Obs> allObs = new ArrayList<>();
        for (Encounter anEncounter : visit.getEncounters()) {
            if (anEncounter.getEncounterType().getName().equals(requestedEncounterType)) {
                Set<Obs> obs = anEncounter.getObs();
                allObs.addAll(obs);
            }
        }
        return allObs;
    }
}

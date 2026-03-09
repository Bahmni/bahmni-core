/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service;

import org.openmrs.Encounter;
import org.openmrs.Visit;

import java.util.List;

public interface BahmniVisitService {
    public Visit getLatestVisit(String patientUuid, String conceptName);

    Visit getVisitSummary(String visitUuid);

    List<Encounter> getAdmitAndDischargeEncounters(Integer visitId);
}

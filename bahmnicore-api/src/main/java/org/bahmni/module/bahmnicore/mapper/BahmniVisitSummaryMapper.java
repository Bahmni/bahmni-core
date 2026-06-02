/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.bahmni.module.bahmnicore.contract.visit.VisitSummary;
import org.openmrs.Encounter;
import org.openmrs.Visit;

import java.util.List;

public class BahmniVisitSummaryMapper {
    public VisitSummary map(Visit visit, List<Encounter> admissionAndDischargeEncounters) {
        VisitSummary visitSummary = new VisitSummary();
        visitSummary.setUuid(visit.getUuid());
        visitSummary.setStartDateTime(visit.getStartDatetime());
        visitSummary.setStopDateTime(visit.getStopDatetime());
        visitSummary.setVisitType(visit.getVisitType().getName());
        mapAdmissionAndDischargeDetails(admissionAndDischargeEncounters, visitSummary);

        return visitSummary;
    }

    private void mapAdmissionAndDischargeDetails(List<Encounter> admissionAndDischargeEncounters, VisitSummary visitSummary) {
        if (CollectionUtils.isNotEmpty(admissionAndDischargeEncounters)) {
            for (Encounter encounter : admissionAndDischargeEncounters) {
                VisitSummary.IPDDetails details = new VisitSummary.IPDDetails();
                details.setUuid(encounter.getUuid());
                details.setDate(encounter.getEncounterDatetime());
                details.setProvider(encounter.getEncounterProviders().iterator().next().getProvider().getName());
                if (CollectionUtils.isNotEmpty(encounter.getAllObs())){
                    details.setNotes(encounter.getAllObs().iterator().next().getValueText());
                }
                if (encounter.getEncounterType().getName().equalsIgnoreCase("ADMISSION")) {
                    visitSummary.setAdmissionDetails(details);
                } else {
                    visitSummary.setDischargeDetails(details);
                }
            }
        }
    }
}

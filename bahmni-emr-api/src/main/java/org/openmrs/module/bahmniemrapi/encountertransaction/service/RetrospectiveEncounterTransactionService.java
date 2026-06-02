/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.encountertransaction.service;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class RetrospectiveEncounterTransactionService {
    protected final VisitMatcher visitMatcher;

    @Autowired
    public RetrospectiveEncounterTransactionService(VisitMatcher visitMatcher) {
        this.visitMatcher = visitMatcher;
    }

    public BahmniEncounterTransaction updatePastEncounters(BahmniEncounterTransaction bahmniEncounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate) {
        if (!BahmniEncounterTransaction.isRetrospectiveEntry(bahmniEncounterTransaction.getEncounterDateTime())) {
            return bahmniEncounterTransaction;
        }

        Visit matchingVisit = visitMatcher.getVisitFor(patient, bahmniEncounterTransaction.getVisitType(),
                bahmniEncounterTransaction.getEncounterDateTime(), visitStartDate, visitEndDate, bahmniEncounterTransaction.getLocationUuid());
        bahmniEncounterTransaction.setVisitUuid(matchingVisit.getUuid());

        // TODO : Mujir - this should not happen here. Just set the visitType. BahmniEncounterTransaction should handle string visitTypes.
        bahmniEncounterTransaction.setVisitTypeUuid(matchingVisit.getVisitType().getUuid());

        return bahmniEncounterTransaction.updateForRetrospectiveEntry(bahmniEncounterTransaction.getEncounterDateTime());
    }
}


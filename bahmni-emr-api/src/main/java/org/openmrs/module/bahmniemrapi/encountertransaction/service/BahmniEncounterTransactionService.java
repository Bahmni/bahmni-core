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
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterSearchParameters;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;

public interface BahmniEncounterTransactionService {
    BahmniEncounterTransaction save(BahmniEncounterTransaction encounterTransaction);
    BahmniEncounterTransaction save(BahmniEncounterTransaction encounterTransaction, Patient patient, Date visitStartDate, Date visitEndDate);
    EncounterTransaction find(BahmniEncounterSearchParameters encounterSearchParameters);
    void delete(BahmniEncounterTransaction bahmniEncounterTransaction);
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.laborder.service;

import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface LabOrderResultsService {
    LabOrderResults getAll(Patient patient, List<Visit> visits, int numberOfAccessions);

    List<LabOrderResult> getAllForConcepts(Patient patient, Collection<String> concepts, List<Visit> visits, Date startDate, Date endDate);
}

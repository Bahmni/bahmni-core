/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.disposition.service;

import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;

import java.util.List;
import java.util.Locale;

public interface BahmniDispositionService {

    List<BahmniDisposition> getDispositionByVisitUuid(String visitUuid);
    List<BahmniDisposition> getDispositionByVisits(List<Visit> visits);

    List<BahmniDisposition> getDispositionByVisitUuid(String visitUuid , Locale locale);
    List<BahmniDisposition> getDispositionByVisits(List<Visit> visits , Locale locale);

}

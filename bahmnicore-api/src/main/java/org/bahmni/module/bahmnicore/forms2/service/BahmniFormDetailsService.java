/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.forms2.service;

import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.bahmni.module.bahmnicore.forms2.contract.FormType;

import java.util.Collection;

public interface BahmniFormDetailsService {
    Collection<FormDetails> getFormDetails(String patientUuid, FormType formType, int numberOfVisits);

    Collection<FormDetails> getFormDetails(String patientUuid, FormType formType, String visitUuid, String patientProgramUuid);
}

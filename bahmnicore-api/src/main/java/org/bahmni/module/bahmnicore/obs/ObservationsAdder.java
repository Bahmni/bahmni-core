/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.obs;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

public interface ObservationsAdder {

    void addObservations(Collection<BahmniObservation> observations, List<String> conceptNames) throws ParseException;
}

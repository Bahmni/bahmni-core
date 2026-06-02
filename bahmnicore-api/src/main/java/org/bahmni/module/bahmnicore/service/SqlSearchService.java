/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service;

import org.openmrs.annotation.Authorized;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.List;
import java.util.Map;

public interface SqlSearchService {

    @Authorized
    public List<SimpleObject> search(String sqlQuery, Map<String, String[]> params);

}

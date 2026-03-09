/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.config.service;

import org.bahmni.module.admin.config.model.BahmniConfig;

import java.util.List;

public interface BahmniConfigService {
    BahmniConfig get(String appName, String configName);

    List<BahmniConfig> getAllFor(String appName);

    BahmniConfig save(BahmniConfig bahmniConfig);

    BahmniConfig update(BahmniConfig bahmniConfig);

    List<String> getAll();
}

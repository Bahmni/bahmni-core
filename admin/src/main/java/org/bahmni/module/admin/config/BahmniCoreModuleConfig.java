/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.config;

import org.bahmni.module.bahmnicommons.api.configuration.ModuleAppConfig;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class BahmniCoreModuleConfig implements ModuleAppConfig {
    @Override
    public String getModuleName() {
        return "core";
    }

    @Override
    public List<String> getGlobalAppProperties() {
        return Arrays.asList(
                "bahmni.encounterType.default",
                "clinic.helpDeskNumber",
                "cdss.enable",
                "obs.conceptMappingsForOT", //should be moved to OT module
                "drugOrder.drugOther",
                "concept.reasonForDeath",
                "bahmni.relationshipTypeMap",
                "bahmni.primaryIdentifierType");
    }
}

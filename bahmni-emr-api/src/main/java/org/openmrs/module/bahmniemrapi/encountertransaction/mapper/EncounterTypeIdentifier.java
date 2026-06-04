/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.EncounterType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmnimapping.services.BahmniLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class EncounterTypeIdentifier {

    private BahmniLocationService bahmniLocationService;
    private EncounterService encounterService;
    private AdministrationService administrationService;

    @Autowired
    public EncounterTypeIdentifier(BahmniLocationService bahmniLocationService, EncounterService encounterService, @Qualifier("adminService") AdministrationService administrationService) {
        this.bahmniLocationService = bahmniLocationService;
        this.encounterService = encounterService;
        this.administrationService = administrationService;
    }

    public EncounterType getEncounterTypeFor(String encounterTypeString, String locationUuid) {

        if (StringUtils.isNotEmpty(encounterTypeString)) {
            return encounterService.getEncounterType(encounterTypeString);
        } else {
            return getEncounterTypeFor(locationUuid);
        }
    }

    public EncounterType getEncounterTypeFor(String locationUuid) {
        EncounterType encounterType = bahmniLocationService.getEncounterType(locationUuid);
        if (encounterType == null){
            return getDefaultEncounterType();
        }
        return encounterType;
    }

    public EncounterType getDefaultEncounterType() {
        String defaultEncounterType = administrationService.getGlobalProperty("bahmni.encounterType.default");
        return encounterService.getEncounterType(defaultEncounterType);
    }


}

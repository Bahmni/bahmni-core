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
package org.openmrs.module.bahmnimapping.services.impl;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.EncounterType;
import org.openmrs.api.APIException;
import org.openmrs.module.bahmnimapping.dao.LocationEncounterTypeMapDao;
import org.openmrs.module.bahmnimapping.services.BahmniLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BahmniLocationServiceImpl implements BahmniLocationService {
    private LocationEncounterTypeMapDao locationEncounterTypeMapDao;

    @Autowired
    public BahmniLocationServiceImpl(LocationEncounterTypeMapDao locationEncounterTypeMapDao) {
        this.locationEncounterTypeMapDao = locationEncounterTypeMapDao;
    }

    @Override
    public List<EncounterType> getEncounterTypes(String locationUuid) {
        if(StringUtils.isBlank(locationUuid)) return new ArrayList<>();
        return locationEncounterTypeMapDao.getEncounterTypes(locationUuid);
    }

    @Override
    public EncounterType getEncounterType(String locationUuid) {
        List<EncounterType> encounterTypes = getEncounterTypes(locationUuid);
        if (encounterTypes.size() == 1) {
            return encounterTypes.get(0);
        }
        if (encounterTypes.size() > 1){
            throw new APIException("The location is mapped to multiple encounter types. Please specify a encounter type for encounter");
        }
        return null;
    }
}

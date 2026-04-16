/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmnimapping.services.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.openmrs.EncounterType;
import org.openmrs.api.APIException;
import org.openmrs.module.bahmnimapping.dao.LocationEncounterTypeMapDao;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniLocationServiceImplTest {
    @Mock
    private LocationEncounterTypeMapDao locationEncounterTypeMapDao;
    private BahmniLocationServiceImpl bahmniLocationService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        initMocks(this);
        bahmniLocationService = new BahmniLocationServiceImpl(locationEncounterTypeMapDao);
    }

    @Test
    public void getEncounterTypeShouldRaiseErrorWhenLocationIsMappedToMultipleEncounterTypes() throws Exception {
        String locationUuid = UUID.randomUUID().toString();
        when(locationEncounterTypeMapDao.getEncounterTypes(locationUuid)).thenReturn(Arrays.asList(new EncounterType(), new EncounterType()));

        expectedException.expect(APIException.class);
        expectedException.expectMessage("The location is mapped to multiple encounter types. Please specify a encounter type for encounter");
        bahmniLocationService.getEncounterType(locationUuid);
    }
}
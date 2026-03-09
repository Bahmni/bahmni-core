/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@Ignore
public class PersonNameDaoImplIT extends BaseIntegrationTest {
	
	@Autowired
	PersonNameDaoImpl personNameDao;
	
	@Test
    @Ignore
	public void shouldRetrievePatientListIfLastNameExists() throws Exception {
		executeDataSet("apiTestData.xml");
		String key = "familyName";
		assertEquals(2, personNameDao.getUnique(key, "singh").size());
		assertEquals(2, personNameDao.getUnique(key, "Singh").size());
		assertEquals(1, personNameDao.getUnique(key, "Banka").size());
		assertEquals(3, personNameDao.getUnique(key, "sin").size());
	}
	
	@Test
    @Ignore
	public void shouldReturnMaxOf20Results() throws Exception {
		executeDataSet("apiTestData.xml");
		String key = "familyName";
		assertTrue(personNameDao.getUnique(key, "test").size() <= 20);
	}
}

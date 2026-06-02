/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.model;

import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import static junit.framework.Assert.assertEquals;

public class BahmniNameTest {
	
	@Test
	public void shouldCreateNameFromSimpleObject() {
		String givenName = "SomeGivenName";
		String middleName = "SomeMiddleName";
		String familyName = "SomeFamilyName";
		SimpleObject nameObject = new SimpleObject().add("givenName", givenName).add("middleName", middleName).add(
		    "familyName", familyName);
		
		BahmniName name = new BahmniName(nameObject);
		
		assertEquals(givenName, name.getGivenName());
		assertEquals(familyName, name.getFamilyName());
	}
}

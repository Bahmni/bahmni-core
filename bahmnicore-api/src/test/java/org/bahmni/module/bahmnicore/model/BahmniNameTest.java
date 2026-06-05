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

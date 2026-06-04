package org.bahmni.module.bahmnicore.model;

import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import static org.junit.Assert.assertEquals;

public class BahmniPersonAttributeTest {
	
	@Test
	public void shouldCreatePersonAttributeFromSimpleObject() {
		String value = "someCaste";
		String attributeUUId = "casteAttributeUUId";
		SimpleObject personAttributeObject = new SimpleObject().add("attributeType", attributeUUId).add("value", value);
		
		BahmniPersonAttribute personAttribute = new BahmniPersonAttribute(personAttributeObject);
		
		assertEquals(attributeUUId, personAttribute.getPersonAttributeUuid());
		assertEquals(value, personAttribute.getValue());
	}
}

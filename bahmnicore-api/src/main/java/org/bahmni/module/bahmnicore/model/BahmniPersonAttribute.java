package org.bahmni.module.bahmnicore.model;

import java.util.LinkedHashMap;

public class BahmniPersonAttribute {
	
	private String personAttributeUuid;
	
	private String value;

    public BahmniPersonAttribute(String personAttributeUuid, String value) {
        this.personAttributeUuid = personAttributeUuid;
        this.value = value;
    }

    public BahmniPersonAttribute(LinkedHashMap post) {
		SimpleObjectExtractor extractor = new SimpleObjectExtractor(post);
		personAttributeUuid = extractor.extract("attributeType");
        Object extractValue = extractor.extract("value");

        if (extractValue instanceof String) {
            value = (String) extractValue;
        } else {
            LinkedHashMap extractValue1 = (LinkedHashMap) extractValue;
            value = (String) extractValue1.get("display");
        }
	}
	
	public String getPersonAttributeUuid() {
		return personAttributeUuid;
	}
	
	public String getValue() {
		return value;
	}
}

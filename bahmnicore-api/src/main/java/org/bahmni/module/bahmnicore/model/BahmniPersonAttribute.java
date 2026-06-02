/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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

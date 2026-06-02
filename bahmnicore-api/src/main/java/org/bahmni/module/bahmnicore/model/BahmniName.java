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

public class BahmniName {
	private String givenName;
	
	private String familyName;

    public BahmniName(LinkedHashMap post) {
		SimpleObjectExtractor extractor = new SimpleObjectExtractor(post);
		givenName = extractor.extract("givenName");
		familyName = extractor.extract("familyName");
	}

    public BahmniName(String givenName, String familyName) {
        this.givenName = givenName;
        this.familyName = familyName;
    }

    public String getGivenName() {
		return givenName;
	}
	
	public String getFamilyName() {
		return familyName;
	}

    public String getFullName() {
        return String.format("%s %s", givenName, familyName);
    }
}

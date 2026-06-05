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

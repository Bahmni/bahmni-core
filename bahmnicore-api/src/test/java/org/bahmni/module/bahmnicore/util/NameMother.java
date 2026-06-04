package org.bahmni.module.bahmnicore.util;

import org.openmrs.PersonName;
import org.openmrs.module.webservices.rest.SimpleObject;

public class NameMother {

    private String firstName;
    private String lastName;
    private String middleName;

    public NameMother() {
        firstName = "first";
        lastName = "last";
        middleName = "middle";
    }

    public SimpleObject getSimpleObjectForName() {
		return new SimpleObject().add("givenName", firstName).add("familyName", lastName).add("middleName", middleName);
	}

    public NameMother withName(String firstName, String middleName, String lastName) {
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        return this;
    }

    public PersonName build() {
        return new PersonName(firstName, middleName, lastName);
    }
}

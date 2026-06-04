package org.bahmni.test.builder;

import org.openmrs.Person;

public class PersonBuilder {

    private final Person person;

    public PersonBuilder() {
        person = new Person();
    }

    public PersonBuilder withUUID(String patientUuid) {
        person.setUuid(patientUuid);
        return this;
    }

    public Person build() {
        return person;
    }
}

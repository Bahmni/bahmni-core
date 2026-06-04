package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.Person;
import org.openmrs.PersonName;

import java.util.HashSet;
import java.util.Set;

public class PersonBuilder {

    private final Person person;

    public PersonBuilder() {
        person = new Person();
    }

    public PersonBuilder withUUID(String patientUuid) {
        person.setUuid(patientUuid);
        return this;
    }

    public PersonBuilder withPersonName(String personNameValue) {
        PersonName personName = new PersonName();
        personName.setGivenName(personNameValue);
        personName.setId(2);
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(personName);
        person.setNames(personNames);
        return this;
    }

    public Person build() {
        return person;
    }
}

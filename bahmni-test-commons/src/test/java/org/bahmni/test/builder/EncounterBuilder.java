package org.bahmni.test.builder;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.VisitType;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EncounterBuilder {
    private final Encounter encounter;

    public EncounterBuilder() {
        encounter = new Encounter();
    }

    private Set<EncounterProvider> createEncounterProviders() {
        EncounterProvider encounterprovider = new EncounterProvider();
        Provider provider = new Provider(1234);

        Person person = new Person(2345);
        Set<PersonName> personNames = new HashSet<PersonName>();
        PersonName name = new PersonName("Yogesh", "", "Jain");
        name.setPreferred(true);
        personNames.add(name);
        person.setNames(personNames);

        provider.setPerson(person);
        encounterprovider.setProvider(provider);
        Set<EncounterProvider> encounterProviders = new HashSet<EncounterProvider>();
        encounterProviders.add(encounterprovider);
        return encounterProviders;
    }

    public Encounter build() {
        return encounter;
    }

    public EncounterBuilder withDefaults() {
        Visit visit = new Visit();
        VisitType visitType = new VisitType();
        visitType.setUuid(UUID.randomUUID().toString());
        visit.setVisitType(visitType);
        visit.setUuid(UUID.randomUUID().toString());
        encounter.setVisit(visit);
        encounter.setUuid(UUID.randomUUID().toString());

        Patient patient = new Patient();
        patient.setUuid(UUID.randomUUID().toString());
        encounter.setPatient(patient);

        EncounterType encounterType = new EncounterType();
        encounterType.setUuid(UUID.randomUUID().toString());
        encounter.setEncounterType(encounterType);

        Location location = new Location();
        location.setUuid(UUID.randomUUID().toString());
        encounter.setLocation(location);

        encounter.setEncounterProviders(createEncounterProviders());
        return this;
    }

    public EncounterBuilder withVisit(Visit visit) {
        encounter.setVisit(visit);
        return this;
    }

    public EncounterBuilder withPerson(Person person) {
        encounter.setPatient(new Patient(person));
        return this;
    }

    public EncounterBuilder withUUID(String uuid) {
        encounter.setUuid(uuid);
        return this;
    }

    public EncounterBuilder withDatetime(Date dateTime) {
        encounter.setEncounterDatetime(dateTime);
        return this;
    }
}

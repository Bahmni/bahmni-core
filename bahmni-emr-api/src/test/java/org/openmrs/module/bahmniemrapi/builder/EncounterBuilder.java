package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.User;
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
        Visit visit = new Visit();
        VisitType visitType = new VisitType();
        visitType.setUuid(UUID.randomUUID().toString());
        visit.setVisitType(visitType);
        visit.setUuid(UUID.randomUUID().toString());
        encounter.setVisit(visit);
        encounter.setUuid(UUID.randomUUID().toString());

        Patient patient = new Patient(123456);
        patient.setUuid(UUID.randomUUID().toString());
        encounter.setPatient(patient);

        EncounterType encounterType = new EncounterType();
        encounterType.setUuid(UUID.randomUUID().toString());
        encounter.setEncounterType(encounterType);

        Location location = new Location();
        location.setUuid(UUID.randomUUID().toString());
        encounter.setLocation(location);
    }

    public Encounter build() {
        return encounter;
    }


    public EncounterBuilder withVisit(Visit visit) {
        encounter.setVisit(visit);
        return this;
    }

    public EncounterBuilder withPatient(Person person) {
        encounter.setPatient(new Patient(person));
        return this;
    }

    public EncounterBuilder withUUID(String uuid) {
        encounter.setUuid(uuid);
        return this;
    }

    public EncounterBuilder withDateCreated(Date date) {
        encounter.setDateCreated(date);
        return this;
    }

    public EncounterBuilder withLocation(Location location) {
        encounter.setLocation(location);
        return this;
    }

    public EncounterBuilder withProvider(Person person) {
        Provider provider = new Provider();
        provider.setPerson(person);
        HashSet<EncounterProvider> encounterProviders = new HashSet<>();
        EncounterProvider encounterProvider = new EncounterProvider();
        encounterProvider.setProvider(provider);
        encounterProviders.add(encounterProvider);
        encounter.setEncounterProviders(encounterProviders);
        return this;
    }

    public EncounterBuilder withEncounterType(EncounterType encounterType) {
        encounter.setEncounterType(encounterType);
        return this;
    }

    public EncounterBuilder withDatetime(Date date) {
        encounter.setEncounterDatetime(date);
        return this;
    }

    public EncounterBuilder withEncounterProviders(Set<EncounterProvider> encounterProviders) {
        encounter.setEncounterProviders(encounterProviders);
        return this;
    }

    public EncounterBuilder withCreator(User user) {
        encounter.setCreator(user);
        return this;
    }
}

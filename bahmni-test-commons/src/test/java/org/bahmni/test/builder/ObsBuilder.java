package org.bahmni.test.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

public class ObsBuilder {

    private final Obs obs;

    public ObsBuilder() {
        obs = new Obs();
    }

    public ObsBuilder withPerson(Person person) {
        obs.setPerson(person);
        return this;
    }

    public ObsBuilder withEncounter(Encounter encounter) {
        obs.setEncounter(encounter);
        return this;
    }

    public ObsBuilder withConcept(Concept concept) {
        obs.setConcept(concept);
        return this;
    }

    public ObsBuilder withConcept(String conceptName) {
        Concept concept = new ConceptBuilder().withName(conceptName).build();
        obs.setConcept(concept);
        return this;
    }

    public ObsBuilder withConcept(ConceptName conceptName) {
        Concept concept = new ConceptBuilder().withName(conceptName).build();
        obs.setConcept(concept);
        return this;
    }

    public ObsBuilder withConcept(String conceptName, Locale locale) {
        Concept concept = new ConceptBuilder().withName(conceptName, locale).build();
        obs.setConcept(concept);
        return this;
    }

    public ObsBuilder withValue(String value) {
        obs.setValueText(value);
        return this;
    }

    public ObsBuilder withValue(Double value) {
        obs.setValueNumeric(value);
        return this;
    }

    public ObsBuilder withValue(Concept value) {
        obs.setValueCoded(value);
        setValueCodedName(obs);
        return this;
    }

    public ObsBuilder withUUID(String obsUuid) {
        obs.setUuid(obsUuid);
        return this;
    }

    private void setValueCodedName(Obs anObs) {
        Concept concept = anObs.getConcept();
        if (concept != null)
            anObs.setValueCodedName(concept.getName());
    }

    public ObsBuilder withDatetime(Date datetime) {
        obs.setObsDatetime(datetime);
        return this;
    }

    public ObsBuilder withGroupMembers(Obs... groupMember) {
        obs.setGroupMembers(new HashSet<>(Arrays.asList(groupMember)));
        return this;
    }

    public Obs build() {
        return obs;
    }
}

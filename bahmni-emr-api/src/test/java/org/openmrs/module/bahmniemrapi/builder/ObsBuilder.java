package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.User;
import org.openmrs.util.LocaleUtility;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

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

    private void setValueCodedName(Obs anObs) {
        Concept concept = anObs.getConcept();
        if (concept != null)
            anObs.setValueCodedName(concept.getName(LocaleUtility.getDefaultLocale()));
    }

    public ObsBuilder withDatetime(Date datetime) {
        obs.setObsDatetime(datetime);
        return this;
    }

    public ObsBuilder withGroupMembers(Obs... groupMember) {
        obs.setGroupMembers(new HashSet<>(Arrays.asList(groupMember)));
        return this;
    }

    public ObsBuilder withCreator(User user){
        obs.setCreator(user);
        return this;
    }

    public ObsBuilder withVoided() {
        obs.setVoided(true);
        return this;
    }

    public Obs build() {
        return obs;
    }
}

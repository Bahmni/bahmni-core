package org.bahmni.module.admin.builder;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;

public class BahmniObservationBuilder {
    private BahmniObservation bahmniObservation;

    public BahmniObservationBuilder() {
        this.bahmniObservation = new BahmniObservation();
    }

    public BahmniObservation build() {
        return bahmniObservation;
    }

    public BahmniObservationBuilder withConcept(String conceptName) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setName(conceptName);
        bahmniObservation.setConcept(concept);
        return this;
    }

    public BahmniObservationBuilder withValue(String value) {
        bahmniObservation.setValue(value);
        return this;
    }

    public BahmniObservationBuilder withEncounterDate(Date encounterDate) {
        bahmniObservation.setEncounterDateTime(encounterDate);
        return this;
    }

    public BahmniObservationBuilder withSetMember(BahmniObservation member) {
        bahmniObservation.addGroupMember(member);
        return this;
    }
}

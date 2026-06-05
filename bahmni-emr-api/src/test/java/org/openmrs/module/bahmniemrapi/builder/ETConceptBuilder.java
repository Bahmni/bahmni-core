package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class ETConceptBuilder {
    private EncounterTransaction.Concept concept;

    public ETConceptBuilder() {
        concept = new EncounterTransaction.Concept();
    }

    public EncounterTransaction.Concept build() {
        return concept;
    }

    public ETConceptBuilder withName(String name) {
        concept.setName(name);
        return this;
    }

    public ETConceptBuilder withUuid(String uuid) {
        concept.setUuid(uuid);
        return this;
    }

    public ETConceptBuilder withSet(boolean isSet) {
        concept.setSet(isSet);
        return this;
    }

    public ETConceptBuilder withClass(String className) {
        concept.setConceptClass(className);
        return this;
    }
}

package org.bahmni.test.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;

public class DrugBuilder {
    private final Drug drug;

    public DrugBuilder() {
        drug = new Drug();
    }

    public Drug build() {
        return drug;
    }

    public DrugBuilder withName(String drugName) {
        drug.setName(drugName);
        return this;
    }

    public DrugBuilder withConcept(Concept concept) {
        drug.setConcept(concept);
        return this;
    }

    public DrugBuilder withConcept(String conceptName) {
        Concept concept = new ConceptBuilder().withName(conceptName).withClassUUID(ConceptClass.DRUG_UUID).build();
        drug.setConcept(concept);
        return this;
    }

    public DrugBuilder withDosageForm(String dosageForm) {
        Concept dosage = new ConceptBuilder().withName(dosageForm).build();
        drug.setDosageForm(dosage);
        return this;
    }

    public DrugBuilder withDosageForm(Concept dosageForm) {
        drug.setDosageForm(dosageForm);
        return this;
    }

    public DrugBuilder withStrength(String strength) {
        drug.setStrength(strength);
        return this;
    }

    public DrugBuilder withMaximumDosage(Double maximumDosage) {
        drug.setMaximumDailyDose(maximumDosage);
        return this;
    }

    public DrugBuilder withMinimumDosage(Double minimumDosage) {
        drug.setMinimumDailyDose(minimumDosage);
        return this;
    }
}
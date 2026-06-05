package org.bahmni.module.referencedata.labconcepts.model;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;

public class DrugMetaData {
    private Concept drugConcept;
    private Concept dosageForm;
    private ConceptClass drugConceptClass;
    private Drug existingDrug;
    public DrugMetaData() {
    }

    public DrugMetaData(Drug existingDrug, Concept drugConcept, Concept dosageFormConcept, ConceptClass drugConceptClass) {
        this.existingDrug = existingDrug;
        this.drugConcept = drugConcept;
        this.dosageForm = dosageFormConcept;
        this.drugConceptClass = drugConceptClass;
    }

    public Concept getDrugConcept() {
        return drugConcept;
    }

    public void setDrugConcept(Concept drugConcept) {
        this.drugConcept = drugConcept;
    }

    public Concept getDosageForm() {
        return dosageForm;
    }

    public void setDosageForm(Concept dosageForm) {
        this.dosageForm = dosageForm;
    }

    public ConceptClass getDrugConceptClass() {
        return drugConceptClass;
    }

    public void setDrugConceptClass(ConceptClass drugConceptClass) {
        this.drugConceptClass = drugConceptClass;
    }

    public Drug getExistingDrug() {
        return existingDrug;
    }

    public void setExistingDrug(Drug existingDrug) {
        this.existingDrug = existingDrug;
    }
}

package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.openmrs.Drug;

public class DrugMetaDataMapper {

    public org.openmrs.Drug map(DrugMetaData drugMetaData) {
        Drug drug = null;

        if (drugMetaData.getExistingDrug() != null) {
            drug = drugMetaData.getExistingDrug();
        } else {
            drug = new Drug();
        }

        drug.setDosageForm(drugMetaData.getDosageForm());
        drug.setConcept(drugMetaData.getDrugConcept());
        return drug;
    }
}

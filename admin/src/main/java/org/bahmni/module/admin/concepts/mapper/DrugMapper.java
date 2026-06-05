package org.bahmni.module.admin.concepts.mapper;

import org.bahmni.module.admin.csv.models.DrugRow;
import org.bahmni.module.referencedata.labconcepts.contract.Drug;

public class DrugMapper {
    public Drug map(DrugRow drugRow) {
        Drug drug = new Drug();
        drug.setUuid(drugRow.getUuid());
        drug.setName(drugRow.getName());
        drug.setGenericName(drugRow.getGenericName());
        drug.setDosageForm(drugRow.getDosageForm());
        drug.setStrength(drugRow.getStrength());
        drug.setMinimumDose(drugRow.getMinimumDose());
        drug.setMaximumDose(drugRow.getMaximumDose());
        drug.setCombination(drugRow.getCombination());
        return drug;
    }
}

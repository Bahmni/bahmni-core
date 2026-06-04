package org.bahmni.module.referencedata.labconcepts.validator;

import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.openmrs.ConceptClass;

import java.util.ArrayList;
import java.util.List;

public class DrugMetaDataValidator extends Validator {
    public void validate(DrugMetaData drugMetaData) {
        List<String> errors = new ArrayList<>();
        if (drugMetaData.getDrugConcept() != null && drugMetaData.getDrugConcept().getConceptClass() !=null && !drugMetaData.getDrugConcept().getConceptClass().getUuid().equals(ConceptClass.DRUG_UUID)) {
            errors.add("There is an existing concept linked to the drug, which does not belong to concept class drug");
        }
        
        if(drugMetaData.getDrugConcept()==null){
            errors.add("There is no concept available with the provided generic name.");
        }

        if(drugMetaData.getDosageForm() == null){
            errors.add("There is no concept available with the provided dosage form.");
        }

        throwExceptionIfExists(errors);
    }
}

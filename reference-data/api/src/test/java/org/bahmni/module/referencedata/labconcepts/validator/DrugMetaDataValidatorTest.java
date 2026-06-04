package org.bahmni.module.referencedata.labconcepts.validator;

import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;
import org.openmrs.api.APIException;

public class DrugMetaDataValidatorTest {

    @Test(expected = APIException.class)
    public void ensureDrugConceptIsNotNull() {
        DrugMetaData drugMetaData = new DrugMetaData(new Drug(), null, new Concept(), new ConceptClass());
        DrugMetaDataValidator validator = new DrugMetaDataValidator();
        validator.validate(drugMetaData);
    }

    @Test(expected = APIException.class)
    public void ensureDosageFormIsNotNull(){
        DrugMetaData drugMetaData = new DrugMetaData(new Drug(),new Concept(), null, new ConceptClass());
        DrugMetaDataValidator validator = new DrugMetaDataValidator();
        validator.validate(drugMetaData);
    }

    @Test
    public void ensureDosageFormAndDrugConceptValid(){
        DrugMetaData drugMetaData = new DrugMetaData(new Drug(),new Concept(), new Concept(), new ConceptClass());
        DrugMetaDataValidator validator = new DrugMetaDataValidator();
        validator.validate(drugMetaData);
        //No error

    }


}

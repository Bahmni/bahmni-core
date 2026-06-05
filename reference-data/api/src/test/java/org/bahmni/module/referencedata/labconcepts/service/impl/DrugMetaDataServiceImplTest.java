package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.bahmni.module.referencedata.labconcepts.contract.Drug;
import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class DrugMetaDataServiceImplTest {

    @Mock
    ConceptService conceptService;

    private static Integer DRUG_CONCEPT = 1;
    private static Integer DOSAGE_FORM_CONCEPT = 2;

    @Before
    public void setup() {
        initMocks(this);
    }

    @Test
    public void ensureDrugMetadataIsProperWithAllValid(){
        Drug drug = new Drug();
        drug.setUuid("uuid");
        drug.setGenericName("genericName");
        drug.setDosageForm("dosageForm");

        org.openmrs.Drug drugInDb1 = new org.openmrs.Drug();
        drugInDb1.setUuid("uuid");

        org.openmrs.Drug drugInDb2 = new org.openmrs.Drug();
        drugInDb2.setUuid("uuid");

        org.openmrs.Drug mrsDrug = new org.openmrs.Drug(1234);

        when(conceptService.getConceptByName("genericName")).thenReturn(new Concept(DRUG_CONCEPT));
        when(conceptService.getConceptByName("dosageForm")).thenReturn(new Concept(DOSAGE_FORM_CONCEPT));
        when(conceptService.getDrugByUuid("uuid")).thenReturn(mrsDrug);
        when(conceptService.getConceptClassByUuid(ConceptClass.DRUG_UUID)).thenReturn(null);
        when(conceptService.getConceptDatatypeByUuid(ConceptDatatype.N_A_UUID)).thenReturn(null);

        DrugMetaDataServiceImpl drugMetaDataService = new DrugMetaDataServiceImpl(conceptService);
        DrugMetaData drugMetaData = drugMetaDataService.getDrugMetaData(drug);

        assertNotNull(drugMetaData);
        assertNotNull(drugMetaData.getExistingDrug());
        assertEquals(DOSAGE_FORM_CONCEPT, drugMetaData.getDosageForm().getId());
        assertEquals(DRUG_CONCEPT,drugMetaData.getDrugConcept().getId());

    }

    @Test
    public void existingDrugIsNullWhenUuidIsInvalid(){
        Drug drug = new Drug();
        drug.setUuid("uuid");
        drug.setGenericName("genericName");
        drug.setDosageForm("dosageForm");

        org.openmrs.Drug drugInDb1 = new org.openmrs.Drug();
        drugInDb1.setUuid("uuid");

        org.openmrs.Drug drugInDb2 = new org.openmrs.Drug();
        drugInDb2.setUuid("uuid");

        new org.openmrs.Drug(1234);

        when(conceptService.getConceptByName("genericName")).thenReturn(new Concept(DRUG_CONCEPT));
        when(conceptService.getConceptByName("dosageForm")).thenReturn(null);
        when(conceptService.getDrugByUuid("uuid")).thenReturn(null);
        when(conceptService.getConceptClassByUuid(ConceptClass.DRUG_UUID)).thenReturn(null);
        when(conceptService.getConceptDatatypeByUuid(ConceptDatatype.N_A_UUID)).thenReturn(null);

        DrugMetaDataServiceImpl drugMetaDataService = new DrugMetaDataServiceImpl(conceptService);
        DrugMetaData drugMetaData = drugMetaDataService.getDrugMetaData(drug);

        assertNotNull(drugMetaData);
        assertNull(drugMetaData.getExistingDrug());
        assertEquals(DRUG_CONCEPT,drugMetaData.getDrugConcept().getId());
    }

    @Test
    public void newDrugWithInvalidDosageForm(){
        Drug drug = new Drug();
        drug.setGenericName("genericName");
        drug.setDosageForm("dosageForm");

        new org.openmrs.Drug(1234);

        when(conceptService.getConceptByName("genericName")).thenReturn(new Concept(DRUG_CONCEPT));
        when(conceptService.getConceptByName("dosageForm")).thenReturn(null);
        when(conceptService.getDrugByUuid("uuid")).thenReturn(null);
        when(conceptService.getConceptClassByUuid(ConceptClass.DRUG_UUID)).thenReturn(null);
        when(conceptService.getConceptDatatypeByUuid(ConceptDatatype.N_A_UUID)).thenReturn(null);

        DrugMetaDataServiceImpl drugMetaDataService = new DrugMetaDataServiceImpl(conceptService);
        DrugMetaData drugMetaData = drugMetaDataService.getDrugMetaData(drug);

        assertNotNull(drugMetaData);
        assertNull(drugMetaData.getExistingDrug());
        assertNull(drugMetaData.getDosageForm());
        assertEquals(DRUG_CONCEPT,drugMetaData.getDrugConcept().getId());
    }


}

package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.bahmni.module.referencedata.labconcepts.contract.Drug;
import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.bahmni.module.referencedata.labconcepts.service.DrugMetaDataService;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ReferenceDataDrugServiceImplTest {

    private ReferenceDataDrugServiceImpl referenceDataDrugService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private DrugMetaDataService drugMetaDataService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(drugMetaDataService.getDrugMetaData((Drug)anyObject())).thenReturn(new DrugMetaData(new org.openmrs.Drug(), new Concept(), new Concept(), new ConceptClass()));
        referenceDataDrugService = new ReferenceDataDrugServiceImpl(conceptService, drugMetaDataService);
    }

    @Test
    public void throwErrorIfDrugNameNotDefined() throws Exception {
        Drug drugData = new Drug();
        exception.expect(APIException.class);
        exception.expectMessage("Drug name is mandatory");
        referenceDataDrugService.saveDrug(drugData);
    }

    @Test
    public void throwErrorIfDrugGenericNameNotDefined() throws Exception {
        Drug drug = new Drug();
        drug.setName("Drug Name");
        exception.expect(APIException.class);
        exception.expectMessage("Drug generic name is mandatory");
        referenceDataDrugService.saveDrug(drug);
    }

    @Test
    public void saveNewDrug() throws Exception {
        Drug drugData = new Drug();
        drugData.setName("Drug Name");
        drugData.setGenericName("Concept name");
        Concept drugConcept = new ConceptBuilder().withName("Concept Name").withClassUUID(ConceptClass.DRUG_UUID).build();
        org.openmrs.Drug drugSave = new org.openmrs.Drug();
        drugSave.setConcept(drugConcept);
        drugSave.setName("Drug Name");
        referenceDataDrugService.saveDrug(drugData);
        verify(conceptService).saveDrug(any(org.openmrs.Drug.class));
    }
}
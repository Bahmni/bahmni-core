package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.Drug;
import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.DrugBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class DrugMapperTest {

    private ConceptClass drugConceptClass;
    private DrugMapper drugMapper;

    @Mock private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        Locale locale = new Locale("en", "GB");
        mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(locale);

        drugMapper = new DrugMapper();
        drugConceptClass = new ConceptClass();
        drugConceptClass.setUuid(ConceptClass.DRUG_UUID);
        drugConceptClass.setName("Drug");
    }

    @Test
    public void createNewDrugWithNameAndGenericNameAndDosageForm() throws Exception {
        Concept tablet = new ConceptBuilder().withName("Tablet").build();
        Concept existingConcept = new ConceptBuilder().withClassUUID(ConceptClass.DRUG_UUID).withName("Existing Concept").build();


        Drug drug = new Drug();
        drug.setName("Drug Name");
        drug.setGenericName("Existing Concept");
        drug.setDosageForm("Tablet");
        DrugMetaData drugMetaData = new DrugMetaData();
        drugMetaData.setDrugConceptClass(drugConceptClass);
        drugMetaData.setDosageForm(tablet);
        drugMetaData.setDrugConcept(existingConcept);

        org.openmrs.Drug mappedDrug = drugMapper.map(drug, drugMetaData);
        assertEquals("Drug Name", mappedDrug.getName());
        assertEquals("Existing Concept", mappedDrug.getConcept().getName(Context.getLocale()).getName());
        assertEquals("Tablet", mappedDrug.getDosageForm().getName(Context.getLocale()).getName());
        assertFalse(mappedDrug.getCombination());
        assertNull(mappedDrug.getMaximumDailyDose());
        assertNull(mappedDrug.getMinimumDailyDose());
    }

    @Test
    public void createNewDrugWithAllFields() throws Exception {
        Concept existingConcept = new ConceptBuilder().withClassUUID(ConceptClass.DRUG_UUID).withName("Existing Concept").build();

        Drug drug = new Drug();
        drug.setName("Drug Name");
        drug.setGenericName("Existing Concept");
        drug.setDosageForm("Tablet");
        drug.setCombination(true);
        drug.setMaximumDose("99.0");
        drug.setMinimumDose("12.0");
        drug.setStrength("Ok");
        DrugMetaData drugMetaData = new DrugMetaData();
        drugMetaData.setDrugConceptClass(drugConceptClass);
        drugMetaData.setDosageForm(new Concept());
        drugMetaData.setDrugConcept(existingConcept);
        org.openmrs.Drug mappedDrug = drugMapper.map(drug, drugMetaData);
        assertEquals("Drug Name", mappedDrug.getName());
        assertEquals("Existing Concept", mappedDrug.getConcept().getName(Context.getLocale()).getName());
        assertTrue(mappedDrug.getCombination());
        assertEquals("Ok", mappedDrug.getStrength());
        assertTrue(mappedDrug.getMaximumDailyDose().equals(99.0));
        assertTrue(mappedDrug.getMinimumDailyDose().equals(12.0));
    }

    @Test
    public void existingDrugOldConceptNewDosageForm() throws Exception {
        Drug drug = new Drug();
        Concept existingConcept = new ConceptBuilder().withClassUUID(ConceptClass.DRUG_UUID).withName("Existing Concept").build();
        Concept capsule = new ConceptBuilder().withName("Capsule").build();
        org.openmrs.Drug existingDrug = new DrugBuilder().withName("Existing Drug").withConcept(existingConcept).withDosageForm("Tablet").withStrength("Very Strong").build();
        drug.setName("Existing Drug");
        drug.setGenericName("Existing Concept");
        drug.setDosageForm("Capsule");
        drug.setCombination(true);
        drug.setMaximumDose("99.0");
        drug.setMinimumDose("12.0");
        drug.setStrength("Ok");
        DrugMetaData drugMetaData = new DrugMetaData();
        drugMetaData.setDrugConceptClass(drugConceptClass);
        drugMetaData.setDrugConcept(existingConcept);
        drugMetaData.setExistingDrug(existingDrug);
        drugMetaData.setDosageForm(capsule);
        assertEquals("Tablet", existingDrug.getDosageForm().getName(Context.getLocale()).getName());
        org.openmrs.Drug mappedDrug = drugMapper.map(drug, drugMetaData);
        assertEquals("Existing Drug", mappedDrug.getName());
        assertEquals("Existing Concept", mappedDrug.getConcept().getName(Context.getLocale()).getName());
        assertEquals("Capsule", mappedDrug.getDosageForm().getName(Context.getLocale()).getName());
        assertTrue(mappedDrug.getCombination());
        assertEquals("Ok", mappedDrug.getStrength());
        assertTrue(mappedDrug.getMaximumDailyDose().equals(99.0));
        assertTrue(mappedDrug.getMinimumDailyDose().equals(12.0));
        assertEquals(mappedDrug.getUuid(), existingDrug.getUuid());
    }

    @Test
    public void existingDrugNewConceptNewDosageForm() throws Exception {
        Drug drug = new Drug();
        Concept existingConcept = new ConceptBuilder().withClassUUID(ConceptClass.DRUG_UUID).withName("Existing Concept").build();
        Concept capsule = new ConceptBuilder().withName("Capsule").build();
        Concept newConcept = new ConceptBuilder().withName("New Drug Concept").withClassUUID(ConceptClass.DRUG_UUID).build();
        org.openmrs.Drug existingDrug = new DrugBuilder().withName("Existing Drug").withConcept(existingConcept).withDosageForm("Tablet").withStrength("Very Strong").build();
        drug.setName("Existing Drug");
        drug.setGenericName("New Drug Concept");
        drug.setDosageForm("Capsule");
        drug.setCombination(true);
        drug.setMaximumDose("99.0");
        drug.setMinimumDose("12.0");
        drug.setStrength("Ok");
        DrugMetaData drugMetaData = new DrugMetaData();
        drugMetaData.setDrugConceptClass(drugConceptClass);
        drugMetaData.setExistingDrug(existingDrug);
        drugMetaData.setDosageForm(capsule);
        drugMetaData.setDrugConcept(newConcept);
        assertEquals("Tablet", existingDrug.getDosageForm().getName(Context.getLocale()).getName());
        assertEquals("Existing Concept", existingDrug.getConcept().getName(Context.getLocale()).getName());
        org.openmrs.Drug mappedDrug = drugMapper.map(drug, drugMetaData);
        assertEquals("Existing Drug", mappedDrug.getName());
        assertEquals("New Drug Concept", mappedDrug.getConcept().getName(Context.getLocale()).getName());
        assertEquals("Capsule", mappedDrug.getDosageForm().getName(Context.getLocale()).getName());
        assertTrue(mappedDrug.getCombination());
        assertEquals("Ok", mappedDrug.getStrength());
        assertTrue(mappedDrug.getMaximumDailyDose().equals(99.0));
        assertTrue(mappedDrug.getMinimumDailyDose().equals(12.0));
        assertEquals(mappedDrug.getUuid(), existingDrug.getUuid());
    }

    @Test
    public void existingDrugNewDrugName() throws Exception {
        Drug drug = new Drug();
        Concept existingConcept = new ConceptBuilder().withClassUUID(ConceptClass.DRUG_UUID).withName("Existing Concept").build();
        Concept capsule = new ConceptBuilder().withName("Capsule").build();
        Concept newConcept = new ConceptBuilder().withName("New Drug Concept").withClassUUID(ConceptClass.DRUG_UUID).build();
        org.openmrs.Drug existingDrug = new DrugBuilder().withName("Existing Drug").withConcept(existingConcept).withDosageForm("Tablet").withStrength("Very Strong").build();
        drug.setName("New Drug Name");
        drug.setGenericName("New Drug Concept");
        drug.setDosageForm("Capsule");
        drug.setCombination(true);
        drug.setMaximumDose("99.0");
        drug.setMinimumDose("12.0");
        drug.setStrength("Ok");
        DrugMetaData drugMetaData = new DrugMetaData();
        drugMetaData.setDrugConceptClass(drugConceptClass);
        drugMetaData.setExistingDrug(existingDrug);
        drugMetaData.setDosageForm(capsule);
        drugMetaData.setDrugConcept(newConcept);
        assertEquals("Tablet", existingDrug.getDosageForm().getName(Context.getLocale()).getName());
        assertEquals("Existing Concept", existingDrug.getConcept().getName(Context.getLocale()).getName());
        org.openmrs.Drug mappedDrug = drugMapper.map(drug, drugMetaData);
        assertEquals("New Drug Name", mappedDrug.getName());
        assertEquals(mappedDrug.getUuid(), existingDrug.getUuid());
        assertEquals("New Drug Concept", mappedDrug.getConcept().getName(Context.getLocale()).getName());
        assertEquals("Capsule", mappedDrug.getDosageForm().getName(Context.getLocale()).getName());
        assertTrue(mappedDrug.getCombination());
        assertEquals("Ok", mappedDrug.getStrength());
        assertTrue(mappedDrug.getMaximumDailyDose().equals(99.0));
        assertTrue(mappedDrug.getMinimumDailyDose().equals(12.0));
    }

    @Test
    public void testOpenmrsDrugToBahmniDrug(){
        Concept existingConcept = new ConceptBuilder().withClassUUID(ConceptClass.DRUG_UUID).withName("Existing Concept").withShortName("short").build();
        org.openmrs.Drug existingDrug = new DrugBuilder().withName("Existing Drug").withConcept(existingConcept).withDosageForm("Tablet").withStrength("Very Strong").build();

        Drug bahmniDrug = drugMapper.map(existingDrug);
        assertEquals("Existing Drug", bahmniDrug.getName());
        assertEquals("Existing Concept", bahmniDrug.getGenericName());
        assertEquals("Tablet", bahmniDrug.getDosageForm());
        assertEquals("short", bahmniDrug.getShortName());
        assertEquals("Very Strong", bahmniDrug.getStrength());
    }
}
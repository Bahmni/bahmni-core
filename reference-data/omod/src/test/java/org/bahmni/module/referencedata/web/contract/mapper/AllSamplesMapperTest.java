package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.contract.Sample;
import org.bahmni.module.referencedata.labconcepts.mapper.AllSamplesMapper;
import org.bahmni.module.referencedata.labconcepts.mapper.SampleMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class AllSamplesMapperTest {

    private AllSamplesMapper allSamplesMapper;
    private SampleMapper sampleMapper;
    private Concept sampleConcept;
    private Concept specimenConcept;
    private Date dateCreated;
    private Date dateChanged;
    private Concept labSampleConceptSet;


    @Mock
    private ConceptService conceptService;

    @Mock
    private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        allSamplesMapper = new AllSamplesMapper();
        sampleMapper = new SampleMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        Concept testConcept = new ConceptBuilder().withUUID("Test UUID").withDateCreated(dateCreated).withClass(LabTest.LAB_TEST_CONCEPT_CLASSES.get(0)).withDescription("SomeDescription")
                .withDateChanged(dateChanged).withShortName("ShortName").withName("Test concept").withDataType(ConceptDatatype.NUMERIC).build();

        sampleConcept = new ConceptBuilder().withUUID("Sample UUID").withDateCreated(dateCreated).withClass(Sample.SAMPLE_CONCEPT_CLASSES.get(0)).
                withDateChanged(dateChanged).withSetMember(testConcept).withShortName("ShortName").withName("SampleName").build();

        specimenConcept = new ConceptBuilder().withUUID("Specimen UUID").withDateCreated(dateCreated).withClass(Sample.SAMPLE_CONCEPT_CLASSES.get(1)).
                withDateChanged(dateChanged).withSetMember(testConcept).withShortName("SpecimenShortName").withName("SpecimenName").build();

        labSampleConceptSet = new ConceptBuilder().withUUID("Lab Samples UUID").withDateCreated(dateCreated).withDateChanged(dateChanged)
                .withName(AllSamples.ALL_SAMPLES).withClassUUID(ConceptClass.LABSET_UUID).withShortName("Lab samples short name").withDescription("Lab samples Description")
                .withSetMember(sampleConcept)
                .withSetMember(specimenConcept)
                .build();

        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void mapAllSampleFieldsFromConcept() throws Exception {

        AllSamples labSamplesData = allSamplesMapper.map(labSampleConceptSet);
        Sample sampleData = sampleMapper.map(sampleConcept);
        assertEquals("Lab Samples UUID", labSamplesData.getId());
        assertEquals("Lab Samples", labSamplesData.getName());
        assertEquals(dateCreated, labSamplesData.getDateCreated());
        assertEquals(dateChanged, labSamplesData.getLastUpdated());
        assertEquals("Lab samples Description", labSamplesData.getDescription());
        List<Sample> samples = labSamplesData.getSamples();
        assertEquals(2, samples.size());
        assertEquals(sampleData.getId(), samples.get(0).getId());
    }

}

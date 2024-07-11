package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.contract.Panel;
import org.bahmni.module.referencedata.labconcepts.contract.Sample;
import org.bahmni.module.referencedata.labconcepts.mapper.PanelMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptSet;
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

import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.createConceptSet;
import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.getConceptSets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

//TODO: Mihir : write a test for empty tests list
@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class PanelMapperTest {
    private PanelMapper panelMapper;
    private Date dateCreated;
    private Date dateChanged;
    @Mock
    private ConceptService conceptService;
    private Concept panelConcept;
    private List<ConceptSet> testConceptSets;
    private Concept testConcept;
    private List<ConceptSet> panelConceptSets;
    @Mock
    private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        panelMapper = new PanelMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        testConcept = new ConceptBuilder().withUUID("Test UUID").withDateCreated(dateCreated).withClass(LabTest.LAB_TEST_CONCEPT_CLASSES.get(0)).withDescription("SomeDescription")
                .withDateChanged(dateChanged).withShortName("ShortName").withName("Panel Name Here").withDataType(ConceptDatatype.NUMERIC).build();
        panelConcept = new ConceptBuilder().withUUID("Panel UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.LABSET_UUID).withDescription("SomeDescription")
                .withSetMember(testConcept).withDateChanged(dateChanged).withShortName("ShortName").withName("Panel Name Here").withDataType(ConceptDatatype.NUMERIC).build();
        Concept testAndPanelsConcept = new ConceptBuilder().withUUID("Test and Panels UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.CONVSET_UUID)
                .withDateChanged(dateChanged).withShortName("ShortName").withName(AllTestsAndPanels.ALL_TESTS_AND_PANELS).withSetMember(panelConcept).build();
        Concept sampleConcept = new ConceptBuilder().withUUID("Sample UUID").withDateCreated(dateCreated).withClass(Sample.SAMPLE_CONCEPT_CLASSES.get(0)).
                withDateChanged(dateChanged).withSetMember(panelConcept).withShortName("ShortName").withName("SampleName").build();
        Concept departmentConcept = new ConceptBuilder().withUUID("Department UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withClass("Department").withClassUUID(ConceptClass.CONVSET_UUID).withSetMember(panelConcept).withDescription("Some Description").withName("Department Name").build();
        ConceptSet panelConceptSet = createConceptSet(testAndPanelsConcept, panelConcept);
        ConceptSet testConceptSet = createConceptSet(testAndPanelsConcept, testConcept);
        ConceptSet testPanelConceptSet = createConceptSet(testConcept, panelConcept);
        ConceptSet panelSampleConceptSet = createConceptSet(sampleConcept, panelConcept);
        ConceptSet testSampleConceptSet = createConceptSet(sampleConcept, testConcept);
        ConceptSet testDepartmentConceptSet = createConceptSet(departmentConcept, testConcept);

        testConceptSets = getConceptSets(testConceptSet);
        testConceptSets.add(testSampleConceptSet);
        testConceptSets.add(testDepartmentConceptSet);

        panelConceptSets = getConceptSets(panelConceptSet);
        panelConceptSets.add(panelSampleConceptSet);
        panelConceptSets.add(testPanelConceptSet);

        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenAnswer(new Answer<List<ConceptSet>>() {
            @Override
            public List<ConceptSet> answer(InvocationOnMock invocation) throws Throwable {
                Object[] arguments = invocation.getArguments();
                Concept concept = (Concept) arguments[0];
                if (concept.getUuid().equals("Test UUID"))
                    return testConceptSets;
                else if (concept.getUuid().equals("Panel UUID"))
                    return panelConceptSets;
                return null;
            }
        });
        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void mapAllPanelFieldsFromConcept() throws Exception {
        Panel panelData = panelMapper.map(panelConcept);
        assertEquals("Panel UUID", panelData.getId());
        assertEquals("Panel Name Here", panelData.getName());
        assertEquals(dateCreated, panelData.getDateCreated());
        assertEquals(dateChanged, panelData.getLastUpdated());
        assertEquals(1, panelData.getTests().size());
        assertEquals("Test UUID", panelData.getTests().get(0).getUuid());
        assertTrue(panelData.getSortOrder().equals(999.0));
    }

    @Test
    public void isActiveTrueByDefault() throws Exception {
        Panel panelData = panelMapper.map(panelConcept);
        assertTrue(panelData.getIsActive());
    }

    @Test
    public void shouldSetNameIfDescriptionIsNull() throws Exception {
        Concept panelConceptWithoutDescription = new ConceptBuilder().withDateCreated(dateCreated).withClassUUID(ConceptClass.LABSET_UUID)
                .withSetMember(testConcept).withDateChanged(dateChanged).withShortName("ShortName").withName("Panel Name Here").withDataType(ConceptDatatype.NUMERIC).build();

        Panel panelData = panelMapper.map(panelConceptWithoutDescription);
        assertEquals("Panel Name Here", panelData.getDescription());
    }
}

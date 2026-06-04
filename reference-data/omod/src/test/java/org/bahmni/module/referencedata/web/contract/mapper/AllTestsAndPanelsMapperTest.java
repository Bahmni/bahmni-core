package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllTestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.contract.Panel;
import org.bahmni.module.referencedata.labconcepts.contract.TestsAndPanels;
import org.bahmni.module.referencedata.labconcepts.mapper.AllTestsAndPanelsMapper;
import org.bahmni.module.referencedata.labconcepts.mapper.LabTestMapper;
import org.bahmni.module.referencedata.labconcepts.mapper.PanelMapper;
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
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class AllTestsAndPanelsMapperTest {
    private AllTestsAndPanelsMapper allTestsAndPanelsMapper;
    private LabTestMapper testMapper;
    private Date dateChanged;
    private Date dateCreated;
    private Concept testConcept;
    private Concept panelConcept;
    private Concept testAndPanelsConcept;
    @Mock
    private ConceptService conceptService;
    private PanelMapper panelMapper;
    @Mock
    private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        allTestsAndPanelsMapper = new AllTestsAndPanelsMapper();
        testMapper = new LabTestMapper();
        panelMapper = new PanelMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);

        testConcept = new ConceptBuilder().withUUID("Test UUID").withDateCreated(dateCreated).withClass(LabTest.LAB_TEST_CONCEPT_CLASSES.get(0)).withDescription("SomeDescription")
                .withDateChanged(dateChanged).withShortName("ShortName").withName("Test concept").withDataType(ConceptDatatype.NUMERIC).build();

        panelConcept = new ConceptBuilder().withUUID("Panel UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.LABSET_UUID).withDescription("SomeDescription")
                .withSetMember(testConcept).withDateChanged(dateChanged).withShortName("ShortName").withName("Panel Name").withDataType(ConceptDatatype.NUMERIC).build();
        testAndPanelsConcept = new ConceptBuilder().withUUID("Test and Panels UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.CONVSET_UUID).withDescription("Test and Panel Description")
                .withDateChanged(dateChanged).withShortName("ShortName").withName(AllTestsAndPanels.ALL_TESTS_AND_PANELS).withSetMember(testConcept).withSetMember(panelConcept).build();

        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void mapAllTestsAndPanelsFieldsFromConcept() throws Exception {

        AllTestsAndPanels allTestsAndPanels = allTestsAndPanelsMapper.map(testAndPanelsConcept);
        LabTest testData = testMapper.map(testConcept);
        Panel panelData = panelMapper.map(panelConcept);

        assertEquals("Test and Panels UUID", allTestsAndPanels.getId());
        assertEquals("All_Tests_and_Panels", allTestsAndPanels.getName());
        assertEquals(dateCreated, allTestsAndPanels.getDateCreated());
        assertEquals(dateChanged, allTestsAndPanels.getLastUpdated());
        assertEquals("Test and Panel Description", allTestsAndPanels.getDescription());

        TestsAndPanels testsAndPanels = allTestsAndPanels.getTestsAndPanels();
        Set<LabTest> tests = testsAndPanels.getTests();
        assertEquals(1, tests.size());
        assertEquals(testData.getId(), tests.iterator().next().getId());

        Set<Panel> panels = testsAndPanels.getPanels();
        assertEquals(1, panels.size());
        assertEquals(panelData.getId(), panels.iterator().next().getId());
    }

    @Test
    public void shouldNotMapTheTestOrPanelWhichIsRetired() throws Exception {
        Concept testConcept = new ConceptBuilder().withUUID("Test UUID").withDateCreated(dateCreated).withClass(LabTest.LAB_TEST_CONCEPT_CLASSES.get(0)).withDescription("SomeDescription")
                .withDateChanged(dateChanged).withShortName("ShortName").withName("Test concept").withDataType(ConceptDatatype.NUMERIC).withRetired(true).build();
        Concept panelConcept = new ConceptBuilder().withUUID("Panel UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.LABSET_UUID).withDescription("SomeDescription")
                .withSetMember(testConcept).withDateChanged(dateChanged).withShortName("ShortName").withName("Panel Name").withDataType(ConceptDatatype.NUMERIC).withRetired(true).build();
        Concept testAndPanelsConcept = new ConceptBuilder().withUUID("Test and Panels UUID").withDateCreated(dateCreated).withClassUUID(ConceptClass.CONVSET_UUID).withDescription("Test and Panel Description")
                .withDateChanged(dateChanged).withShortName("ShortName").withName(AllTestsAndPanels.ALL_TESTS_AND_PANELS).withSetMember(testConcept).withSetMember(panelConcept).build();
        AllTestsAndPanels allTestsAndPanels = allTestsAndPanelsMapper.map(testAndPanelsConcept);

        assertEquals("Test and Panels UUID", allTestsAndPanels.getId());
        assertEquals("All_Tests_and_Panels", allTestsAndPanels.getName());
        assertEquals(dateCreated, allTestsAndPanels.getDateCreated());
        assertEquals(dateChanged, allTestsAndPanels.getLastUpdated());
        assertEquals("Test and Panel Description", allTestsAndPanels.getDescription());

        TestsAndPanels testsAndPanels = allTestsAndPanels.getTestsAndPanels();
        Set<LabTest> tests = testsAndPanels.getTests();
        assertEquals(0, tests.size());

        Set<Panel> panels = testsAndPanels.getPanels();
        assertEquals(0, panels.size());
    }
}

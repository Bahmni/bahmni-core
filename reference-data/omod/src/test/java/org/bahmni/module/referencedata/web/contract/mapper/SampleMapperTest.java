package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.bahmni.module.referencedata.labconcepts.contract.Sample;
import org.bahmni.module.referencedata.labconcepts.mapper.ResourceMapper;
import org.bahmni.module.referencedata.labconcepts.mapper.SampleMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.createConceptSet;
import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.getConceptSets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class SampleMapperTest {

    private SampleMapper sampleMapper;
    private Concept sampleConcept;
    private Concept allSamplesConcept;
    @Mock
    private ConceptService conceptService;
    private Double sortOrder;
    @Mock
    private UserContext userContext;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        sampleMapper = new SampleMapper();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        sampleConcept = new ConceptBuilder().forSample().withShortName("ShortName").build();
        allSamplesConcept = new ConceptBuilder().withUUID("Laboratory UUID")
                .withName(AllSamples.ALL_SAMPLES).withClass(Sample.SAMPLE_CONCEPT_CLASSES.get(0))
                .withSetMember(sampleConcept).build();
        ConceptSet conceptSet = createConceptSet(allSamplesConcept, sampleConcept);
        sortOrder = Double.valueOf(22);
        conceptSet.setSortWeight(sortOrder);
        List<ConceptSet> conceptSets = getConceptSets(conceptSet);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void mapAllSampleFieldsFromConcept() {
        Sample sampleData = sampleMapper.map(sampleConcept);
        assertEquals("Sample UUID", sampleData.getId());
        assertEquals(sortOrder, sampleData.getSortOrder());
        assertEquals(sampleConcept.getDateCreated(), sampleData.getDateCreated());
        assertEquals(sampleConcept.getDateChanged(), sampleData.getLastUpdated());
        assertEquals("ShortName", sampleData.getShortName());
    }

    @Test
    public void sendDefaultForNoShortName() {
        sampleConcept = new ConceptBuilder().forSample().build();
        assertEquals(0, sampleConcept.getShortNames().size());

        Sample sampleData = sampleMapper.map(sampleConcept);
        assertEquals("Sample UUID", sampleData.getId());
        assertEquals("SampleName", sampleData.getShortName());
    }

    @Test
    public void isActiveTrueByDefault() {
        Sample sampleData = sampleMapper.map(sampleConcept);
        assertTrue(sampleData.getIsActive());
    }

    @Test
    public void doubleMaxAsSortOrderWhenSortOrderNotSpecified() {
        ConceptSet conceptSet = createConceptSet(allSamplesConcept, sampleConcept);
        List<ConceptSet> conceptSets = getConceptSets(conceptSet);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        when(Context.getConceptService()).thenReturn(conceptService);
        Sample sampleData = sampleMapper.map(sampleConcept);
        assertTrue(sampleData.getSortOrder().equals(ResourceMapper.DEFAULT_SORT_ORDER));
    }

    @Test
    public void mapTestsFromConceptSetMembers(){
        Concept testConcept = new ConceptBuilder().forTest().withDataType("N/A").build();
        Concept sampleConcept = new ConceptBuilder().forSample().withSetMember(testConcept).build();
        Sample sample = sampleMapper.map(sampleConcept);
        assertNotNull(sample.getTests());
        assertEquals(1, sample.getTests().size());
        assertEquals("TestName", sample.getTests().get(0).getName());
    }

    @Test
    public void mapPanelsFromConceptSetMembers(){
        Concept panelConcept = new ConceptBuilder().forPanel().build();
        Concept sampleConcept = new ConceptBuilder().forSample().withSetMember(panelConcept).build();
        Sample sample = sampleMapper.map(sampleConcept);
        assertNotNull(sample.getPanels());
        assertEquals(1, sample.getPanels().size());
        assertEquals("PanelName", sample.getPanels().get(0).getName());
    }
}

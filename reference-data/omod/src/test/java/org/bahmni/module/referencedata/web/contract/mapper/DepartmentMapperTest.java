package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.Department;
import org.bahmni.module.referencedata.labconcepts.contract.ResourceReference;
import org.bahmni.module.referencedata.labconcepts.mapper.DepartmentMapper;
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

import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.createConceptSet;
import static org.bahmni.module.referencedata.labconcepts.advice.ConceptServiceEventInterceptorTest.getConceptSets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@PowerMockIgnore("javax.management.*")
@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class DepartmentMapperTest {

    private DepartmentMapper departmentMapper;
    private Concept departmentConcept;
    private Date dateCreated;
    private Date dateChanged;

    @Mock
    private ConceptService conceptService;
    @Mock
    private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        departmentMapper = new DepartmentMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        PowerMockito.mockStatic(Context.class);
        when(Context.getLocale()).thenReturn(defaultLocale);
        departmentConcept = new ConceptBuilder().forDepartment().build();
        Concept allDepartmentsConcept = new ConceptBuilder().withUUID("Laboratory UUID")
                .withName(Department.DEPARTMENT_PARENT_CONCEPT_NAME).withClass(Department.DEPARTMENT_CONCEPT_CLASS)
                .withSetMember(departmentConcept).build();
        ConceptSet conceptSet = createConceptSet(allDepartmentsConcept, departmentConcept);
        List<ConceptSet> conceptSets = getConceptSets(conceptSet);
        when(conceptService.getSetsContainingConcept(any(Concept.class))).thenReturn(conceptSets);
        when(Context.getConceptService()).thenReturn(conceptService);
    }

    @Test
    public void mapAllSampleFieldsFromConcept() throws Exception {
        Department departmentData = departmentMapper.map(departmentConcept);
        assertEquals("Department UUID", departmentData.getId());
        assertEquals(departmentData.getDateCreated(), departmentData.getDateCreated());
        assertEquals(departmentData.getLastUpdated(), departmentData.getLastUpdated());
        assertEquals("Some Description", departmentData.getDescription());
    }

    @Test
    public void mapIfNoParentConcept() throws Exception {
        Concept departmentConcept = new ConceptBuilder().withUUID("Department UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withName("DepartmentName").build();
        Department departmentData = departmentMapper.map(departmentConcept);
        assertEquals("Department UUID", departmentData.getId());
    }

    @Test
    public void isActiveTrueByDefault() throws Exception {
        Department departmentData = departmentMapper.map(departmentConcept);
        assertTrue(departmentData.getIsActive());
    }

    @Test
    public void shouldSetNameIfDescriptionIsNull() throws Exception {

        Concept departmentConceptWithOutDescription = new ConceptBuilder().withUUID("Department UUID").withDateCreated(dateCreated).
                withDateChanged(dateChanged).withName("DepartmentName").build();
        Department departmentData = departmentMapper.map(departmentConceptWithOutDescription);
        assertEquals("DepartmentName", departmentData.getDescription());
    }

    @Test
    public void shouldMapTests() throws Exception {
        Concept testConcept = new ConceptBuilder().forTest().build();
        departmentConcept.addSetMember(testConcept);

        Department departmentData = departmentMapper.map(departmentConcept);
        List<ResourceReference> tests = departmentData.getTests();

        assertEquals(1, tests.size());
        assertEquals("TestName", tests.get(0).getName());
    }
}
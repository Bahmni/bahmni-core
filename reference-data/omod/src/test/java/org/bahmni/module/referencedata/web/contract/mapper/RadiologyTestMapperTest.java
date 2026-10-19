package org.bahmni.module.referencedata.web.contract.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.RadiologyTest;
import org.bahmni.module.referencedata.labconcepts.mapper.RadiologyTestMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class RadiologyTestMapperTest {
    private RadiologyTestMapper testMapper;
    private Concept radiologyConcept;
    private Date dateCreated;
    private Date dateChanged;
    @Mock
    private ConceptService conceptService;
    @Mock
    private UserContext userContext;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        testMapper = new RadiologyTestMapper();
        dateCreated = new Date();
        dateChanged = new Date();
        Locale defaultLocale = new Locale("en", "GB");
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getLocale).thenReturn(defaultLocale);

       radiologyConcept = new ConceptBuilder().withUUID("RadiologyUUID").withDateCreated(dateCreated).withClass(RadiologyTest.RADIOLOGY_TEST_CONCEPT_CLASSES.get(0)).
                withDateChanged(dateChanged).withShortName("clavicle - right, 2 views (x-ray)").withName("Clavicle - Right, 2 views (X-ray)").build();

        contextMockedStatic.when(Context::getConceptService).thenReturn(conceptService);
    }

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }

    @Test
    public void mapNameOfRadiologyTestFromConcept() throws Exception {
        RadiologyTest testData = testMapper.map(radiologyConcept);
        assertEquals("RadiologyUUID", testData.getId());
        assertEquals(dateCreated, testData.getDateCreated());
        assertEquals(dateChanged, testData.getLastUpdated());
        assertEquals("Clavicle - Right, 2 views (X-ray)", testData.getName());
    }


}

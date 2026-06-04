package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.EntityDao;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AbstractDaoImplIT extends BaseIntegrationTest {
    @Autowired
    EntityDao entityDao;

    @Before
    public void setUp() throws Exception {
        executeDataSet("apiTestData.xml");
    }

    @Test
    public void getByUuidShouldGetLocationByUuid() throws Exception {
        String locationUuid = "8d6c993e-c2cc-11de-8d13-0010c6dfed0f";
        Location location = entityDao.getByUuid(locationUuid, Location.class);
        assertNotNull(location);
        assertEquals(locationUuid, location.getUuid());
        assertEquals("Semariya subcentre",location.getDescription());
    }

    @Test
    public void getByUuidShouldGetConceptByUuid() throws Exception{
        String conceptUuid = "True_concept_uuid";
        Concept concept = entityDao.getByUuid(conceptUuid, Concept.class);
        assertNotNull(concept);
        assertEquals(conceptUuid, concept.getUuid());
        assertEquals("True",concept.getName().getName());
    }
}
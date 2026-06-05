package org.openmrs.module.bahmnimapping.dao.impl;

import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class LocationEncounterTypeMapDaoImplIT extends BaseModuleContextSensitiveTest {

    @Autowired
    private LocationEncounterTypeMapDaoImpl locationEncounterTypeMapDao;

    @Test
    public void shouldGetEncounterTypesForMappedLocation() throws Exception {
        executeDataSet("locationEncounterTypeMapData.xml");

        List<EncounterType> encounterTypes = locationEncounterTypeMapDao.getEncounterTypes("c36006e5-9fbb-4f20-866b-0ece245615a1");

        assertEquals(1, encounterTypes.size());
        assertEquals("759799ab-c9a5-435e-b671-77773ada74e4", encounterTypes.get(0).getUuid());
    }

    @Test
    public void shouldGetZeroEncounterTypesForUnmappedLocation() throws Exception {
        executeDataSet("locationEncounterTypeMapData.xml");

        List<EncounterType> encounterTypes = locationEncounterTypeMapDao.getEncounterTypes("e36006e5-9fbb-4f20-866b-0ece245615a1");

        assertEquals(0, encounterTypes.size());
    }

    @Test
    public void shouldGetZeroEncounterTypesForNonExistingLocation() throws Exception {
        executeDataSet("locationEncounterTypeMapData.xml");

        List<EncounterType> encounterTypes = locationEncounterTypeMapDao.getEncounterTypes("11111111-9fbb-4f20-866b-0ece245615a1");

        assertEquals(0, encounterTypes.size());
    }
}
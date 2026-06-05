package org.bahmni.module.obsrelationship.dao.impl;

import org.bahmni.module.obsrelationship.dao.ObsRelationshipDao;
import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.junit.Test;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class ObsRelationshipDaoImplIT extends BaseModuleContextSensitiveTest {

    @Autowired
    ObsRelationshipDao obsRelationshipDao;

    @Autowired
    ObsService obsService;

    @org.junit.Before
    public void setUp() throws Exception {
//       obsRelationshipDao = new ObsRelationshipDaoImpl();
       executeDataSet("obsRelationshipDataset.xml");
    }

    @Test
    public void shouldCreateNewObsRelationship(){
        ObsRelationship obsRelationship = new ObsRelationship();
        obsRelationship.setSourceObs(new Obs(7));
        obsRelationship.setTargetObs(new Obs(9));
        obsRelationship.setObsRelationshipType(new ObsRelationshipType(){{setId(1);}});
        assert(obsRelationship.getId() == 0);

        obsRelationshipDao.saveOrUpdate(obsRelationship);
        assert(obsRelationship.getId()> 0);
    }

    @Test
    public void shouldCreateNewObsRelationshipType(){
        ObsRelationshipType obsRelationshipType = new ObsRelationshipType();
        obsRelationshipType.setName("has-member");
        obsRelationshipType.setDescription("has-member");
        assert(obsRelationshipType.getId() == 0);
        obsRelationshipDao.saveOrUpdateRelationshipType(obsRelationshipType);
        assert(obsRelationshipType.getId()> 0);
        assertThat(obsRelationshipType.getName(),is("has-member"));
    }

    @Test
    public void shouldUpdateObsRelationship() throws ParseException {
        String uuid = "2cc6880e-2c46-11e4-9038-a6c5e4d22fb7";
        ObsRelationship obsRelationship = obsRelationshipDao.getRelationByUuid(uuid);
        obsRelationship.setSourceObs(obsService.getObs(11));
        obsRelationshipDao.saveOrUpdate(obsRelationship);
        assertThat (obsRelationship.getId(),is(1));
        assertThat(obsRelationship.getSourceObs().getId(),is(11));
    }

    @Test
    public void shouldGetRelationsByUuid()
    {
        String uuid = "2cc6880e-2c46-11e4-9038-a6c5e4d22fb7";
        ObsRelationship obsRelationship = obsRelationshipDao.getRelationByUuid(uuid);
        assertThat( obsRelationship.getId(),is(1));
        assertThat (obsRelationship.getTargetObs().getId(),is(7));
        assertThat (obsRelationship.getSourceObs().getId(),is(9));
        assertEquals("qualified-by", obsRelationship.getObsRelationshipType().getName());

    }

    @Test
    public void shouldGetRelationsBySourceAndTargetObs(){
        Obs sourceObs = obsService.getObs(9);
        Obs targetObs = obsService.getObs(7);
        List<ObsRelationship> obsRelationships = obsRelationshipDao.getRelationsBy(sourceObs, targetObs);
        assertNotNull(obsRelationships.get(0));
        assertThat( obsRelationships.get(0).getId(),is(1));
    }

    @Test
    public void shouldGetRelationsBySourceOrTargetObs(){
        Obs sourceObs = obsService.getObs(9);
        List<ObsRelationship> obsRelationships = obsRelationshipDao.getRelationsBy(sourceObs, null);
        assertNotNull(obsRelationships);
        assertThat(obsRelationships.size(),is(2));
        assertThat( obsRelationships.get(0).getId(),is(1));
        assertThat( obsRelationships.get(1).getId(),is(2));
    }

    @Test
    public void shouldNotGetRelationsBySourceAndTargetObsWhenThereIsOnlyASingleMatch(){
        Obs sourceObs = obsService.getObs(9);
        Obs targetObs = obsService.getObs(16);
        List<ObsRelationship> obsRelationships = obsRelationshipDao.getRelationsBy(sourceObs, targetObs);
        assertThat(obsRelationships.size(),is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionWhenGetRelationsBySourceOrTargetObsAreBothNullValues(){
        Obs sourceObs = null;
        Obs targetObs = null;
        obsRelationshipDao.getRelationsBy(sourceObs, targetObs);
    }

    @Test
    public void shouldNotGetRelationsBySourceAndTargetObsWhenBothDoNotMatch(){
        Obs sourceObs = obsService.getObs(15);
        Obs targetObs = obsService.getObs(16);
        List<ObsRelationship> obsRelationships = obsRelationshipDao.getRelationsBy(sourceObs, targetObs);
        assertThat(obsRelationships.size(),is(0));
    }

    @Test
    public void shouldGetAllRelationshipTypes(){
        List<ObsRelationshipType> relationshipTypes = obsRelationshipDao.getAllRelationshipTypes();
        assertThat(relationshipTypes.size(), is(2));
    }

    @Test
    public void shouldGetRelationshipTypeByName(){
        String relationshipName = "derived-from";
        ObsRelationshipType relationshipType = obsRelationshipDao.getRelationshipTypeByName(relationshipName);
        assertThat(relationshipType.getName(), is(relationshipName));
    }

    @Test
    public void shouldReturnNullWhenNameInGetRelationshipTypeByNameDoesNotMatch(){
        String relationshipName = "replaces";
        ObsRelationshipType relationshipType = obsRelationshipDao.getRelationshipTypeByName(relationshipName);
        assertNull(relationshipType);
    }

    @Test
    public void shouldReturnObsRelationsInGivenEncounter(){
        String encounterUuid = "6519d653-393b-4118-9c83-a3715b82d4ac";
        List<ObsRelationship> obsRelationships = obsRelationshipDao.getRelationsWhereSourceObsInEncounter(encounterUuid);
        assertEquals(2,obsRelationships.size());
        assertEquals("2cc6880e-2c46-11e4-9038-a6c5e4d22fb7", obsRelationships.get(0).getUuid());
        assertEquals(new Integer(9), obsRelationships.get(0).getSourceObs().getId());
        assertEquals(new Integer(7), obsRelationships.get(0).getTargetObs().getId());

        assertEquals("2cc6880e-2c46-11e4-9038-a6c5e4d22222", obsRelationships.get(1).getUuid());
        assertEquals(new Integer(9), obsRelationships.get(1).getSourceObs().getId());
        assertEquals(new Integer(11), obsRelationships.get(1).getTargetObs().getId());
    }

}
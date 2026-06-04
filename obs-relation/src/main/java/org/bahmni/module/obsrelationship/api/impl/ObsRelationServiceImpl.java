package org.bahmni.module.obsrelationship.api.impl;

import org.bahmni.module.obsrelationship.api.ObsRelationService;
import org.bahmni.module.obsrelationship.dao.ObsRelationshipDao;
import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.openmrs.Obs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ObsRelationServiceImpl implements ObsRelationService {

    @Autowired
    private ObsRelationshipDao obsRelationshipDao;

    public ObsRelationshipDao getObsRelationshipDao() {
        return obsRelationshipDao;
    }

    public void setObsRelationshipDao(ObsRelationshipDao obsRelationshipDao) {
        this.obsRelationshipDao = obsRelationshipDao;
    }

    @Override
    public ObsRelationship saveOrUpdate(ObsRelationship obsRelationship) {
        return obsRelationshipDao.saveOrUpdate(obsRelationship);
    }

    @Override
    public ObsRelationshipType saveOrUpdateRelationshipType(ObsRelationshipType obsRelationshipType) {
        return obsRelationshipDao.saveOrUpdateRelationshipType(obsRelationshipType);
    }

    @Override
    public ObsRelationship getRelationByUuid(String uuid) {
        return obsRelationshipDao.getRelationByUuid(uuid);
    }

    @Override
    public List<ObsRelationship> getRelationsBy(Obs sourceObs, Obs targetObs) {
        return obsRelationshipDao.getRelationsBy(sourceObs,targetObs);
    }

    @Override
    public List<ObsRelationship> getRelationsWhereSourceObsInEncounter(String encounterUuid){
        return obsRelationshipDao.getRelationsWhereSourceObsInEncounter(encounterUuid);
    }

    @Override
    public List<ObsRelationshipType> getAllRelationshipTypes() {
        return obsRelationshipDao.getAllRelationshipTypes();
    }

    @Override
    public ObsRelationshipType getRelationshipTypeByName(String name) {
        return obsRelationshipDao.getRelationshipTypeByName(name);
    }

    @Override
    public List<ObsRelationship> getObsRelationshipsByTargetObsUuid(String targetObsUuid) {
        return obsRelationshipDao.getObsRelationshipsByTargetObsUuid(targetObsUuid);
    }
}

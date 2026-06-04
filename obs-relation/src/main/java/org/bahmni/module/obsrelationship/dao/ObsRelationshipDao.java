package org.bahmni.module.obsrelationship.dao;

import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.openmrs.Obs;

import java.util.List;

public interface ObsRelationshipDao {
    ObsRelationship saveOrUpdate(ObsRelationship obsRelationship);
    ObsRelationshipType saveOrUpdateRelationshipType(ObsRelationshipType obsRelationshipType);
    ObsRelationship getRelationByUuid(String uuid);
    List<ObsRelationship> getRelationsBy(Obs sourceObs, Obs targetObs);
    List<ObsRelationshipType> getAllRelationshipTypes();
    ObsRelationshipType getRelationshipTypeByName(String name);

    List<ObsRelationship> getRelationsWhereSourceObsInEncounter(String encounterUuid);

    List<ObsRelationship> getObsRelationshipsByTargetObsUuid(String targetObsUuid);
}

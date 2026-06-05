package org.bahmni.module.obsrelationship.api;


import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.openmrs.Obs;

import java.util.List;

public interface ObsRelationService {
    ObsRelationship saveOrUpdate(ObsRelationship obsRelationship);
    ObsRelationshipType saveOrUpdateRelationshipType(ObsRelationshipType obsRelationshipType);
    ObsRelationship getRelationByUuid(String uuid);
    List<ObsRelationship> getRelationsBy(Obs sourceObs, Obs targetObs);

    List<ObsRelationship> getRelationsWhereSourceObsInEncounter(String encounterUuid);

    List<ObsRelationshipType> getAllRelationshipTypes();
    ObsRelationshipType getRelationshipTypeByName(String name);

    List<ObsRelationship> getObsRelationshipsByTargetObsUuid(String targetObsUuid);
}

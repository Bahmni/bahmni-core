/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmnimapping.dao;

import org.openmrs.module.bahmnimapping.model.EntityMapping;
import org.openmrs.module.bahmnimapping.model.EntityMappingType;

import java.util.List;

public interface EntityMappingDao {
    List<EntityMapping> getMappingsOfEntity(String entity1Uuid, String mappingTypeName);
    List<EntityMapping> getAllEntityMappings(String mappingTypeName);
    EntityMappingType getEntityMappingTypeByName(String name);

}

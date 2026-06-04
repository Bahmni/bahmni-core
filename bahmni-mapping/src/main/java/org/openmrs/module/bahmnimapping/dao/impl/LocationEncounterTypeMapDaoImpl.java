/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.bahmnimapping.dao.impl;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.EncounterType;
import org.openmrs.module.bahmnimapping.dao.LocationEncounterTypeMapDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationEncounterTypeMapDaoImpl implements LocationEncounterTypeMapDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<EncounterType> getEncounterTypes(String locationUuid) {
        Session currentSession = sessionFactory.getCurrentSession();
        Query query = currentSession.createQuery(
                "select et from EncounterType et, Location l, LocationEncounterTypeMap map " +
                "where map.encounterType = et.encounterTypeId and map.location = l.locationId and l.uuid = :locationUuid " +
                "and map.voided = false and et.retired = false and l.retired = false");
        query.setParameter("locationUuid", locationUuid);
        return (List<EncounterType>) query.list();
    }
}

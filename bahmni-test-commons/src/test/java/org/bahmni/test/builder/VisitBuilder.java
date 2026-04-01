/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.test.builder;

import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.VisitType;

import java.util.Date;
import java.util.HashSet;

public class VisitBuilder {

    private final Visit visit;

    public VisitBuilder() {
        visit = new Visit();
    }

    public VisitBuilder withPerson(Person person) {
        visit.setPatient(new Patient(person));
        return this;
    }

    public VisitBuilder withUUID(String uuid) {
        visit.setUuid(uuid);
        return this;
    }

    public VisitBuilder withStartDatetime(Date startDatetime) {
        visit.setStartDatetime(startDatetime);
        return this;
    }

    public VisitBuilder withVisitType(VisitType visitType){
        visit.setVisitType(visitType);
        return this;
    }

    public Visit build() {
        return visit;
    }

    public VisitBuilder withEncounter(Encounter encounter) {
        HashSet<Encounter> encounters = new HashSet<>();
        encounters.add(encounter);
        visit.setEncounters(encounters);
        return this;
    }
}

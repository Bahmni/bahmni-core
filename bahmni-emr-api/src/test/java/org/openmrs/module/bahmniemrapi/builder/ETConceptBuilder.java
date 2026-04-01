/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class ETConceptBuilder {
    private EncounterTransaction.Concept concept;

    public ETConceptBuilder() {
        concept = new EncounterTransaction.Concept();
    }

    public EncounterTransaction.Concept build() {
        return concept;
    }

    public ETConceptBuilder withName(String name) {
        concept.setName(name);
        return this;
    }

    public ETConceptBuilder withUuid(String uuid) {
        concept.setUuid(uuid);
        return this;
    }

    public ETConceptBuilder withSet(boolean isSet) {
        concept.setSet(isSet);
        return this;
    }

    public ETConceptBuilder withClass(String className) {
        concept.setConceptClass(className);
        return this;
    }
}

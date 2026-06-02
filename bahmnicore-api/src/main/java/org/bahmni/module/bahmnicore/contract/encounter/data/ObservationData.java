/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.contract.encounter.data;

public class ObservationData {
    private String conceptUUID;
    private String conceptName;
    private Object value;

    public ObservationData(String conceptUUID, String conceptName, Object value) {
        this.conceptUUID = conceptUUID;
        this.conceptName = conceptName;
        this.value = value;
    }

    public ObservationData() {
    }

    public String getConceptUUID() {
        return conceptUUID;
    }

    public Object getValue() {
        return value;
    }

    public void setConceptUUID(String conceptUUID) {
        this.conceptUUID = conceptUUID;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }
}
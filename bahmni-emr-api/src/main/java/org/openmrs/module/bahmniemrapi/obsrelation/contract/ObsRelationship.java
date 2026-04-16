/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.obsrelation.contract;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

public class ObsRelationship {
    private BahmniObservation targetObs;
    private String uuid;
    private String relationshipType;

    public ObsRelationship() {
    }

    public ObsRelationship(BahmniObservation targetObs, String uuid, String relationshipType) {
        this.targetObs = targetObs;
        this.uuid = uuid;
        this.relationshipType = relationshipType;
    }

    public BahmniObservation getTargetObs() {
        return targetObs;
    }

    public void setTargetObs(BahmniObservation targetObs) {
        this.targetObs = targetObs;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }
}

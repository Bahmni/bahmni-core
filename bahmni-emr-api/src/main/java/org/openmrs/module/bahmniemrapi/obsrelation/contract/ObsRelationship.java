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

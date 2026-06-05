package org.bahmni.module.obsrelationship.model;


import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Obs;
import org.openmrs.User;

import java.io.Serializable;
import java.util.Date;

public class ObsRelationship extends BaseOpenmrsObject implements Auditable, Serializable {

    private int id;
    private Obs targetObs;
    private Obs sourceObs;
    private User creator;
    private Date dateCreated;
    private ObsRelationshipType obsRelationshipType;

    @Override
    public User getCreator() {
        return this.creator;
    }


    @Override
    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public Date getDateCreated() {
        return this.dateCreated;
    }

    @Override
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    @Override
    public User getChangedBy() {
        return null;
    }

    @Override
    public void setChangedBy(User changedBy) {

    }

    @Override
    public Date getDateChanged() {
        return null;
    }

    @Override
    public void setDateChanged(Date dateChanged) {

    }

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Obs getTargetObs() {
        return targetObs;
    }

    public void setTargetObs(Obs targetObs) {
        this.targetObs = targetObs;
    }

    public Obs getSourceObs() {
        return sourceObs;
    }

    public void setSourceObs(Obs sourceObs) {
        this.sourceObs = sourceObs;
    }

    public ObsRelationshipType getObsRelationshipType() {
        return obsRelationshipType;
    }

    public void setObsRelationshipType(ObsRelationshipType obsRelationshipType) {
        this.obsRelationshipType = obsRelationshipType;
    }


}

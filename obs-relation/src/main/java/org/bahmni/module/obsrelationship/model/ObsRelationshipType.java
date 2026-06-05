package org.bahmni.module.obsrelationship.model;

import org.openmrs.BaseOpenmrsMetadata;


public class ObsRelationshipType extends BaseOpenmrsMetadata implements java.io.Serializable{
    private int id;

    @Override
    public Integer getId() {
        return this.id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }
}

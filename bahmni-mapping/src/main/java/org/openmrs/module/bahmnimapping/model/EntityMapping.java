package org.openmrs.module.bahmnimapping.model;

public class EntityMapping {
    private Integer id;
    private String uuid;
    private String entity1Uuid;
    private String entity2Uuid;
    private EntityMappingType entityMappingType;

    public EntityMapping() {
    }

    public EntityMapping(Integer id, String uuid, String entity1Uuid, String entity2Uuid, EntityMappingType entityMappingType) {
        this.id = id;
        this.uuid = uuid;
        this.entity1Uuid = entity1Uuid;
        this.entity2Uuid = entity2Uuid;
        this.entityMappingType = entityMappingType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getEntity1Uuid() {
        return entity1Uuid;
    }

    public void setEntity1Uuid(String entity1Uuid) {
        this.entity1Uuid = entity1Uuid;
    }

    public String getEntity2Uuid() {
        return entity2Uuid;
    }

    public void setEntity2Uuid(String entity2Uuid) {
        this.entity2Uuid = entity2Uuid;
    }

    public EntityMappingType getEntityMappingType() {
        return entityMappingType;
    }

    public void setEntityMappingType(EntityMappingType entityMappingType) {
        this.entityMappingType = entityMappingType;
    }
}



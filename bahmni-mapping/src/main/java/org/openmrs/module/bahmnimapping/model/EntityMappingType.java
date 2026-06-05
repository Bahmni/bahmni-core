package org.openmrs.module.bahmnimapping.model;

public class EntityMappingType {
    private Integer id;
    private String uuid;
    private String name;
    private String entity1Type;
    private String entity2Type;

    public EntityMappingType() {
    }

    public EntityMappingType(Integer id, String uuid, String name, String entity1Type, String entity2Type) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.entity1Type = entity1Type;
        this.entity2Type = entity2Type;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEntity1Type() {
        return entity1Type;
    }

    public void setEntity1Type(String entity1Type) {
        this.entity1Type = entity1Type;
    }

    public String getEntity2Type() {
        return entity2Type;
    }

    public void setEntity2Type(String entity2Type) {
        this.entity2Type = entity2Type;
    }
}

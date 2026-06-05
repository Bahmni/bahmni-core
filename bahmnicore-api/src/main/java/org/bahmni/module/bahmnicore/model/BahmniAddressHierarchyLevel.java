package org.bahmni.module.bahmnicore.model;

public class BahmniAddressHierarchyLevel {

    private Integer levelId;

    private String name;

    private Integer parentLevelId;

    private String addressField;

    private Boolean required = false;

    private String uuid;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public Integer getLevelId() {
        return this.levelId;
    }

    public void setAddressField(String addressField) {
        this.addressField = addressField;
    }

    public String getAddressField() {
        return addressField;
    }

    public Integer getId() {
        return this.levelId;
    }

    public void setId(Integer id) {
        this.levelId = id;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Boolean getRequired() {
        return required;
    }

    public Integer getParentLevelId() {
        return parentLevelId;
    }

    public void setParentLevelId(Integer parentLevelId) {
        this.parentLevelId = parentLevelId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}

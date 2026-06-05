package org.bahmni.module.bahmnicore.model;


public class BahmniAddressHierarchyEntry {

    private Integer addressHierarchyEntryId;

    private String name;

    private Integer levelId;

    private BahmniAddressHierarchyLevel addressHierarchyLevel;

    private Integer parentId;

    private String userGeneratedId;

    private String uuid;

    public Integer getAddressHierarchyEntryId() {
        return addressHierarchyEntryId;
    }

    public void setAddressHierarchyEntryId(Integer addressHierarchyEntryId) {
        this.addressHierarchyEntryId = addressHierarchyEntryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getUserGeneratedId() {
        return userGeneratedId;
    }

    public void setUserGeneratedId(String userGeneratedId) {
        this.userGeneratedId = userGeneratedId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public BahmniAddressHierarchyLevel getAddressHierarchyLevel() {
        return addressHierarchyLevel;
    }

    public void setAddressHierarchyLevel(BahmniAddressHierarchyLevel addressHierarchyLevel) {
        this.addressHierarchyLevel = addressHierarchyLevel;
    }
}

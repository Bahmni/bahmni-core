package org.bahmni.module.bahmnicore.contract.encounter.data;

public class ConceptData {
    private String uuid;
    private String name;

    public ConceptData(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public ConceptData() {
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
}
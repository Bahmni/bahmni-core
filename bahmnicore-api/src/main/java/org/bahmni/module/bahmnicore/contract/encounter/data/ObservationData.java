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
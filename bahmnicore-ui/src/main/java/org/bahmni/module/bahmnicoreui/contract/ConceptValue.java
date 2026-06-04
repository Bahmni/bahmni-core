package org.bahmni.module.bahmnicoreui.contract;

public class ConceptValue {
    private String value;

    private Boolean abnormal;

    public Boolean getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(Boolean abnormal) {
        this.abnormal = abnormal;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}

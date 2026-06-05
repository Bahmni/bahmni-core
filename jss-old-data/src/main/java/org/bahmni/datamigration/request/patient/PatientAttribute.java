package org.bahmni.datamigration.request.patient;

import static org.bahmni.datamigration.DataScrub.scrubData;

public class PatientAttribute {
    private String attributeType;
    private String name;
    private String value;

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = scrubData(name);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = scrubData(value);
    }
}
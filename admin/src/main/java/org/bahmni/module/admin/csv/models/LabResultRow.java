package org.bahmni.module.admin.csv.models;

import org.apache.commons.lang3.StringUtils;

public class LabResultRow {
    private String test;
    private String result;

    public LabResultRow() {
    }

    public LabResultRow(String test, String result) {
        this.test = test;
        this.result = result;
    }

    public String getTest() {
        return test;
    }

    public LabResultRow setTest(String test) {
        this.test = test;
        return this;
    }

    public String getResult() {
        return result;
    }

    public LabResultRow setResult(String result) {
        this.result = result;
        return this;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(test) && StringUtils.isBlank(result);
    }
}

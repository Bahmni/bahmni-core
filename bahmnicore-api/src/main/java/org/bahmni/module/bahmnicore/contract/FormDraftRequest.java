package org.bahmni.module.bahmnicore.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FormDraftRequest {

    @JsonProperty
    private String patientUuid;

    @JsonProperty
    private String formData;

    public FormDraftRequest() {
        // Required for JSON deserialization
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }
}

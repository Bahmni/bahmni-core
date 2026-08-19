package org.bahmni.module.bahmnicore.contract;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FormDraftRequest {

    @JsonProperty
    private String patientUuid;

    @JsonProperty
    private String providerUuid;

    @JsonProperty
    private String formData;

    public FormDraftRequest() {
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }
}

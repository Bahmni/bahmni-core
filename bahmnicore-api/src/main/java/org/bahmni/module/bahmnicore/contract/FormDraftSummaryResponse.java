package org.bahmni.module.bahmnicore.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormDraftSummaryResponse {

    @JsonProperty
    private String draftUuid;

    @JsonProperty
    private String patientUuid;

    @JsonProperty
    private String patientName;

    @JsonProperty
    private String patientIdentifier;

    @JsonProperty
    private String encounterUuid;

    @JsonProperty
    private String formName;

    @JsonProperty
    private Long timestamp;

    public FormDraftSummaryResponse() {
    }

    public String getDraftUuid() {
        return draftUuid;
    }

    public void setDraftUuid(String draftUuid) {
        this.draftUuid = draftUuid;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientIdentifier() {
        return patientIdentifier;
    }

    public void setPatientIdentifier(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

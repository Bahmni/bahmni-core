package org.bahmni.module.bahmnicore.contract;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

public class FormDraftResponse {

    @JsonProperty
    private String uuid;

    @JsonProperty
    private String formData;

    @JsonProperty
    private Boolean markedAsSaved;

    @JsonProperty
    private Long timestamp;

    public FormDraftResponse() {
    }

    public FormDraftResponse(String uuid) {
        this.uuid = uuid;
    }

    public FormDraftResponse(String uuid, String formData) {
        this.uuid = uuid;
        this.formData = formData;
    }

    public FormDraftResponse(String uuid, String formData, Boolean markedAsSaved) {
        this.uuid = uuid;
        this.formData = formData;
        this.markedAsSaved = markedAsSaved;
    }

    public FormDraftResponse(String uuid, String formData, Boolean markedAsSaved, Long timestamp) {
        this.uuid = uuid;
        this.formData = formData;
        this.markedAsSaved = markedAsSaved;
        this.timestamp = timestamp;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    public Boolean getMarkedAsSaved() {
        return markedAsSaved;
    }

    public void setMarkedAsSaved(Boolean markedAsSaved) {
        this.markedAsSaved = markedAsSaved;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

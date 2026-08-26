package org.bahmni.module.bahmnicore.model;

import org.openmrs.BaseChangeableOpenmrsData;
import org.openmrs.Patient;
import org.openmrs.User;

public class FormDraft extends BaseChangeableOpenmrsData {

    private Integer id;

    private String uuid;

    private Patient patient;

    private User user;

    private String formDataPath;  // Path to JSON file on filesystem

    private Boolean markedAsSaved;  // Track if draft has been submitted/saved

    public FormDraft() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFormDataPath() {
        return formDataPath;
    }

    public void setFormDataPath(String formDataPath) {
        this.formDataPath = formDataPath;
    }

    public Boolean getMarkedAsSaved() {
        return markedAsSaved;
    }

    public void setMarkedAsSaved(Boolean markedAsSaved) {
        this.markedAsSaved = markedAsSaved;
    }
}

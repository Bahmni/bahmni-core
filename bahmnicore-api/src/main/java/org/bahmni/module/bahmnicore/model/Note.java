package org.bahmni.module.bahmnicore.model;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.openmrs.Auditable;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;
import org.openmrs.User;

public class Note extends BaseOpenmrsData implements Auditable, Serializable {

    private Integer noteId;

    private String noteText;

    private Date dateChanged;

    private Date dateCreated;

    private NoteType noteType;

    private Date noteDate;

    private Integer locationId;

    private User creator;

    private User changedBy;


    public Note() {
    }

    @Override
    public void setId(Integer id) {
        setNoteId(id);
    }

    @Override
    public Integer getId() {
        return getNoteId();
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    @Override
    public Date getDateCreated() {
        return dateCreated;
    }

    @Override
    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Integer getNoteId() {
        return noteId;
    }

    public void setNoteId(Integer noteId) {
        this.noteId = noteId;
    }

    public Integer getLocationId() {
        return locationId;
    }

    public void setLocationId(Integer locationId) {
        this.locationId = locationId;
    }

    public Date getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(Date noteDate) {
        this.noteDate = noteDate;
    }

    public NoteType getNoteType() {
        return noteType;
    }

    public void setNoteType(NoteType noteType) {
        this.noteType = noteType;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    @Override
    @JsonIgnore
    public User getCreator() {
        return creator;
    }

    @Override
    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    @JsonIgnore
    public User getChangedBy() {
        return changedBy;
    }

    @Override
    public void setChangedBy(User changedBy) {
        this.changedBy = changedBy;
    }
}

package org.bahmni.module.bahmnicore.model;


import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.openmrs.BaseOpenmrsData;
import org.openmrs.Patient;

/**
 * Not currently used.
 */
public class Note extends BaseOpenmrsData implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5392076713513109152L;

    // Data

    /**
     * noteId, an identifier for a patient note.
     */
    private Integer noteId;

    private String text;

    private Date dateChanged;

    private Date dateCreated;

    private Integer priority;

    private Integer weight;

    private Patient patient;

    private NoteType noteType;

    private Date noteDate;

    private Integer locationId;


    public Note() {
    }

    /**
     * @see org.openmrs.OpenmrsObject#setId(java.lang.Integer)
     */
    public void setId(Integer id) {
        setNoteId(noteId);
    }

    /**
     * @see org.openmrs.OpenmrsObject#getId()
     */
    public Integer getId() {
        return getNoteId();
    }

    public Date getDateChanged() {
        return dateChanged;
    }

    public void setDateChanged(Date dateChanged) {
        this.dateChanged = dateChanged;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }

    /**
     * @return Returns the noteId.
     */
    public Integer getNoteId() {
        return noteId;
    }

    /**
     * @param noteId the noteId to set.
     */
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
}

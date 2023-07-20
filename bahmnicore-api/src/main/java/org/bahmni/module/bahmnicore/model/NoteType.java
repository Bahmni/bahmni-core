package org.bahmni.module.bahmnicore.model;

import org.openmrs.BaseOpenmrsData;

import java.io.Serializable;

public class NoteType extends BaseOpenmrsData implements Serializable {

    private Integer noteTypeId;

    private String name;

    private String description;
    public NoteType() {
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getNoteTypeId() {
        return noteTypeId;
    }
    public void setNoteTypeId(Integer noteTypeId) {
        this.noteTypeId = noteTypeId;
    }
    public Integer getId() {
        return getNoteTypeId();
    }

    public void setId(Integer id) {
        setNoteTypeId(id);
    }
}



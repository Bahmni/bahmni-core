package org.bahmni.module.bahmnicore.contract;


import java.util.Date;

public class NoteResponse {

    private String uuid;

    private Integer noteId;

    private String noteText;

    private String noteTypeName;

    private Date noteDate;

    private String LocationName;


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getNoteId() {
        return noteId;
    }

    public void setNoteId(Integer noteId) {
        this.noteId = noteId;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public Date getNoteDate() {
        return noteDate;
    }

    public void setNoteDate(Date noteDate) {
        this.noteDate = noteDate;
    }

    public String getNoteTypeName() {
        return noteTypeName;
    }

    public void setNoteTypeName(String noteTypeName) {
        this.noteTypeName = noteTypeName;
    }

    public String getLocationName() {
        return LocationName;
    }

    public void setLocationName(String locationName) {
        LocationName = locationName;
    }
}

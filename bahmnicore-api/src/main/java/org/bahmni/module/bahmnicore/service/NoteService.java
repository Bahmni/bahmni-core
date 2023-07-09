package org.bahmni.module.bahmnicore.service;


import java.util.Date;
import java.util.List;

import org.bahmni.module.bahmnicore.model.Note;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.util.PrivilegeConstants;


public interface NoteService {


    @Authorized(PrivilegeConstants.GET_NOTE)
    public List<Note> getNotes(Date startDate, Date endDate, String noteType) throws Exception;


    public Note createNote(Note note) throws APIException;


    public Note getNote(Integer noteId) throws Exception;


    public Note updateNote(Note note) throws APIException;


    @Authorized(PrivilegeConstants.DELETE_NOTE)
    public Note voidNote(Note note, String reason) throws APIException;

    public List<Note> createNotes(List<Note> notes) throws APIException;

    @Authorized(PrivilegeConstants.GET_NOTE)
    public Note getNote(Date noteDate, String noteType, Integer locationId) throws Exception;
}

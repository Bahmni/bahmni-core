package org.bahmni.module.bahmnicore.service;


import java.util.Date;
import java.util.List;

import org.bahmni.module.bahmnicore.contract.NoteRequestResponse;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.model.NoteType;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.util.PrivilegeConstants;
import org.springframework.transaction.annotation.Transactional;


public interface NoteService {

    @Transactional
    @Authorized(PrivilegeConstants.GET_NOTE)
    List<Note> getNotes(Date noteStartDate, Date noteEndDate, String noteType) throws Exception;

    @Transactional
    Note createNote(Note note);

    @Transactional
    Note updateNote(Integer id, NoteRequestResponse noteRequestResponse);

    @Transactional
    @Authorized(PrivilegeConstants.DELETE_NOTE)
    Note voidNote(Integer id, String reason);

    @Transactional
    List<Note> createNotes(List<Note> notes);

    @Transactional
    NoteType getNoteType(String name);

    @Transactional
    @Authorized(PrivilegeConstants.GET_NOTE)
    Note getNote(Date noteDate, String noteType);

    @Transactional
    Note getNoteById(Integer id);

    @Transactional
    Note getNoteByUuid(String uuid);
}

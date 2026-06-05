package org.bahmni.module.bahmnicore.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.contract.NoteRequestResponse;
import org.bahmni.module.bahmnicore.dao.NoteDao;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.model.NoteType;
import org.bahmni.module.bahmnicore.service.NoteService;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.ProviderService;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Transactional
public class NoteServiceImpl implements NoteService, Serializable {

    private NoteDao noteDao;

    public void setNoteDao(NoteDao noteDao) {
        this.noteDao = noteDao;
    }

    private NoteDao getNoteDAO() {
        return noteDao;
    }

    private Log log = LogFactory.getLog(this.getClass());

    public NoteServiceImpl() {
    }

    public Note createNote(Note note) {
        log.info("Create a note " + note);
        noteDao.createNote(note);
        return note;
    }

    public Note getNote(Integer noteId) {
        log.info("Get note " + noteId);
        return noteDao.getNoteById(noteId);
    }

    public Note updateNote(Integer id, NoteRequestResponse noteRequestResponse) {
        Note note = noteDao.getNoteById(id);
        Provider provider = Context.getService(ProviderService.class).getProviderByUuid(noteRequestResponse.getProviderUuid());
        User user = Context.getService(UserService.class).getUsersByPerson(provider.getPerson(), false).get(0);
        if (note.getCreator().getUserId() == user.getUserId()) {
            note.setNoteText(noteRequestResponse.getNoteText());
            log.info("Update note " + note);
            return noteDao.updateNote(note);
        } else {
            Note newNote = new Note();
            newNote.setNoteDate(note.getNoteDate());
            newNote.setNoteText(noteRequestResponse.getNoteText());
            newNote.setNoteType(note.getNoteType());
            newNote = noteDao.createNote(newNote);
            voidNote(id, "Updated to #" + newNote.getNoteId());
            log.info("Voided old note and created note " + newNote);
            return newNote;
        }
    }

    public List<Note> getNotes(Date noteStartDate, Date noteEndDate, String noteType) {
        return noteDao.getNotes(noteStartDate, noteEndDate, noteType);
    }


    public Note voidNote(Integer id, String reason) {
        Note note = noteDao.getNoteById(id);
        note.setVoided(true);
        note.setVoidReason(reason);
        log.debug("voiding note because " + reason);
        return noteDao.voidNote(note);
    }

    @Override
    public List<Note> createNotes(List<Note> notes) {
        return notes.stream().map(note -> noteDao.createNote(note)).collect(Collectors.toList());
    }

    @Override
    public NoteType getNoteType(String name) {
        return noteDao.getNoteType(name);
    }

    @Override
    public Note getNote(Date noteDate, String noteType)  {
        return noteDao.getNote(noteDate, noteType);
    }

    @Override
    public Note getNoteById(Integer noteId) {
        return noteDao.getNoteById(noteId);
    }

    @Override
    public Note getNoteByUuid(String uuid) {
        return noteDao.getNoteByUuid(uuid);
    }
}

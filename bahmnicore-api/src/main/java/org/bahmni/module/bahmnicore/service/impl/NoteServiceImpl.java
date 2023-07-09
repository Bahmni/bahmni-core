package org.bahmni.module.bahmnicore.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.dao.NoteDao;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.service.NoteService;
import org.openmrs.api.APIException;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class NoteServiceImpl implements NoteService, Serializable {

    private static final long serialVersionUID = 5649635694623650303L;

    private NoteDao noteDao;

    private NoteDao getNoteDAO() {
        return noteDao;
    }

    public void setNoteDAO(NoteDao dao) {
        this.noteDao = dao;
    }

    private Log log = LogFactory.getLog(this.getClass());

    public NoteServiceImpl() {
    }

    public Collection<Note> getNotes() throws Exception {
        log.info("Get all notes");
        return getNoteDAO().getNotes();
    }

    public Note createNote(Note note) throws APIException {
        log.info("Create a note " + note);
        getNoteDAO().createNote(note);
        return note;
    }

    public Note getNote(Integer noteId) throws Exception {
        log.info("Get note " + noteId);
        return getNoteDAO().getNoteById(noteId);
    }

    public Note updateNote(Note note) throws APIException {
        log.info("Update note " + note);
        return getNoteDAO().updateNote(note);
    }

    public List<Note> getNotes(Date startDate, Date endDate, String noteType) throws Exception {
        return noteDao.getNotes(startDate, endDate, noteType);
    }


    public Note voidNote(Note note, String reason) throws APIException {
        log.debug("voiding note because " + reason);
        return noteDao.voidNote(note, reason);
    }

    @Override
    public List<Note> createNotes(List<Note> notes) throws APIException {
        for (Note note: notes) {
            noteDao.createNote(note);
        }
        return notes;
    }

    @Override
    public Note getNote(Date noteDate, String noteType, Integer locationId) throws Exception {
        return noteDao.getNote(noteDate, noteType, locationId);
    }
}

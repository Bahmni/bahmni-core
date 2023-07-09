package org.bahmni.module.bahmnicore.dao.impl;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.dao.NoteDao;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.model.NoteType;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.APIException;
import org.openmrs.api.db.DAOException;

public class NoteDaoImpl implements NoteDao {

    protected final static Log log = LogFactory.getLog(NoteDaoImpl.class);

    private SessionFactory sessionFactory;

    public NoteDaoImpl() {
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

//    @SuppressWarnings("unchecked")
    public List<Note> getNotes() {
        log.info("Getting all notes from the database");
        return sessionFactory.getCurrentSession().createQuery("from Note").list();
    }

    public Note getNoteById(Integer id) {
        log.info("Get note " + id);
        return (Note) sessionFactory.getCurrentSession().get(Note.class, id);
    }

    public void createNote(Note note) {
        log.debug("Creating new note");
        NoteType noteType = getNoteType(note.getNoteType().getName());
        note.setNoteType(noteType);
        sessionFactory.getCurrentSession().save(note);
    }

    private NoteType getNoteType(String name){
        List<NoteType> noteType = new ArrayList<>();
        Session currentSession = sessionFactory.getCurrentSession();
        Query query = currentSession.createQuery("select noteType from NoteType noteType " +
                "where noteType.name = :name");
        query.setParameter("name", name);
        noteType.addAll(query.list());
        return CollectionUtils.isEmpty(noteType) ? null : noteType.get(0);

    }

    public Note updateNote(Note note) {
        log.debug("Updating existing note");
        sessionFactory.getCurrentSession().save(note);
        return note;
    }

    public void deleteNote(Note note) throws DAOException {
        log.debug("Deleting existing note");
        sessionFactory.getCurrentSession().delete(note);
    }

    public Note voidNote(Note note, String reason) throws APIException {
        log.debug("voiding note because " + reason);
        sessionFactory.getCurrentSession().save(note);
        return note;
    }

    @Override
    public Note getNote(Date noteDate, String noteType, Integer locationId) throws DAOException {
        List<Note> notes = new ArrayList<>();
        Session currentSession = sessionFactory.getCurrentSession();
        Query query = currentSession.createQuery(
                "select note from Note note" +
                        "where note.noteDate = :noteDate and note.locationId = :locationId " +
                        "and note.noteType.description = :noteType");
        query.setParameter("noteDate", noteDate);
        query.setParameter("locationId", locationId);
        query.setParameter("noteType", noteType);
        notes.addAll(query.list());
        return CollectionUtils.isEmpty(notes) ? null : notes.get(0);
    }

    @Override
    public List<Note> getNotes(Date startDate, Date endDate, String noteType) throws DAOException {
        List<Note> notes = new ArrayList<>();
        Session currentSession = sessionFactory.getCurrentSession();
        Query query = currentSession.createQuery(
               "select note from Note note" +
               "where note.noteDate between :startDate and :endDate " +
                       "and note.noteType.description = :noteType");
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("noteType", noteType);
        notes.addAll(query.list());
        return notes;

    }
}

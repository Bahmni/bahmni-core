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
import org.hibernate.query.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.openmrs.api.APIException;

public class NoteDaoImpl implements NoteDao {

    protected final static Log log = LogFactory.getLog(NoteDaoImpl.class);

    private SessionFactory sessionFactory;

    public NoteDaoImpl() {
    }

    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Note getNoteById(Integer id) {
        log.info("Get note " + id);
        return (Note) sessionFactory.getCurrentSession().get(Note.class, id);
    }

    public Note createNote(Note note) {
        log.debug("Creating new note");
        sessionFactory.getCurrentSession().save(note);
        return note;
    }

    public NoteType getNoteType(String name){
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

    public void deleteNote(Note note) {
        log.debug("Deleting existing note");
        sessionFactory.getCurrentSession().delete(note);
    }

    public Note voidNote(Note note) throws APIException {
        sessionFactory.getCurrentSession().save(note);
        return note;
    }

    @Override
    public Note getNote(Date noteDate, String noteType) {
        List<Note> notes = new ArrayList<>();
        StringBuilder query = new StringBuilder("select note from Note note " +
                "where note.noteDate = :noteDate " +
                "and note.noteType.name = :noteType " +
                "and note.voided = false");

        Query queryToGetNotes = sessionFactory.getCurrentSession().createQuery(query.toString());
        queryToGetNotes.setParameter("noteDate", noteDate);
        queryToGetNotes.setParameter("noteType", noteType);

        notes.addAll(queryToGetNotes.list());
        return CollectionUtils.isEmpty(notes) ? null : notes.get(0);
    }

    @Override
    public List<Note> getNotes(Date startDate, Date endDate, String noteType) {
        List<Note> notes = new ArrayList<>();
        Session currentSession = sessionFactory.getCurrentSession();
        Query query = currentSession.createQuery(
               "select note from Note note " +
               "where note.noteDate between :startDate and :endDate " +
                       "and note.noteType.name = :noteType" +
                       " and note.voided = false");
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        query.setParameter("noteType", noteType);
        notes.addAll(query.list());
        return notes;

    }
    @Override
    public Note getNoteByUuid(String uuid) {
        return (Note)this.sessionFactory.getCurrentSession().createQuery("from Note note where note.uuid = :uuid").setParameter("uuid", uuid).uniqueResult();
    }
}

package org.bahmni.module.bahmnicore.dao;

import java.util.Date;
import java.util.List;

import org.bahmni.module.bahmnicore.model.Note;
import org.openmrs.api.db.DAOException;

public interface NoteDao {

    public List<Note> getNotes() throws DAOException;

    public void createNote(Note note) throws DAOException;

    public Note getNoteById(Integer noteId) throws DAOException;

    public Note updateNote(Note note) throws DAOException;

    public void deleteNote(Note note) throws DAOException;

    public Note voidNote(Note note, String reason) throws DAOException;

    public Note getNote(Date noteDate, String noteType, Integer locationId) throws DAOException;

    public List<Note> getNotes(Date startDate, Date endDate, String noteType) throws DAOException;
}

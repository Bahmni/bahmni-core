package org.bahmni.module.bahmnicore.dao;

import java.util.Date;
import java.util.List;

import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.model.NoteType;
import org.openmrs.api.db.DAOException;

public interface NoteDao {

    Note createNote(Note note);

    Note getNoteById(Integer noteId);

    Note updateNote(Note note);

    void deleteNote(Note note);

    Note voidNote(Note note);

    Note getNote(Date noteDate, String noteType);

    NoteType getNoteType(String name);

    List<Note> getNotes(Date startDate, Date endDate, String noteType);

    Note getNoteByUuid(String uuid);
}

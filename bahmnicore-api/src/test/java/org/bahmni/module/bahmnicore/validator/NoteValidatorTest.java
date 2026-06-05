package org.bahmni.module.bahmnicore.validator;

import org.bahmni.module.bahmnicore.contract.NoteRequestResponse;
import org.bahmni.module.bahmnicore.dao.NoteDao;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.model.NoteType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.MockitoAnnotations.initMocks;

public class NoteValidatorTest {

    @InjectMocks
    private NoteValidator validator;
    @Mock
    private NoteDao noteDao;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void ensureNoteTypeIsNotNull() {
        initMocks(this);
        NoteRequestResponse noteRequest = new NoteRequestResponse();
        Errors noteRequestErrors = new BeanPropertyBindingResult(noteRequest, "noteRequest");
        noteRequest.setNoteTypeName(null);
        noteRequest.setNoteText("Note Text");
        noteRequest.setNoteDate(new Date());
        Note note = new Note();
        note.setId(1);
        Mockito.when(noteDao.getNote(any(Date.class), any(String.class))).thenReturn(note);
        validator.validate(noteRequest, noteRequestErrors);
        assertEquals(true, "Note.noteType.required".matches(noteRequestErrors.getAllErrors().get(0).getCode().toString()));
    }

    @Test
    public void ensureNoteDateIsNotNull() {
        NoteRequestResponse noteRequest = new NoteRequestResponse();
        Errors noteRequestErrors = new BeanPropertyBindingResult(noteRequest, "noteRequest");
        noteRequest.setNoteTypeName("OT module");
        noteRequest.setNoteText("Note Text");
        noteRequest.setNoteDate(null);
        Note note = new Note();
        note.setId(1);
        Mockito.when(noteDao.getNote(any(Date.class), any(String.class))).thenReturn(note);
        validator.validate(noteRequest, noteRequestErrors);
        assertEquals(true, "Note.noteDate.required".matches(noteRequestErrors.getAllErrors().get(0).getCode().toString()));
    }

    @Test
    public void ensureNoteTextIsNotNull() {
        NoteRequestResponse noteRequest = new NoteRequestResponse();
        Errors noteRequestErrors = new BeanPropertyBindingResult(noteRequest, "noteRequest");
        noteRequest.setNoteTypeName("OT module");
        noteRequest.setNoteDate(new Date());
        noteRequest.setNoteText(null);
        Note note = new Note();
        note.setId(1);
        Mockito.when(noteDao.getNote(any(Date.class), any(String.class))).thenReturn(note);
        validator.validate(noteRequest, noteRequestErrors);
        assertEquals(true, "Note.noteText.required".matches(noteRequestErrors.getAllErrors().get(0).getCode().toString()));
    }

    @Test
    public void ensureNoteDoesntExistForSameNoteDateAndNoteType() throws ParseException {
        NoteRequestResponse noteRequest = new NoteRequestResponse();
        Errors noteRequestErrors = new BeanPropertyBindingResult(noteRequest, "noteRequest");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date noteDate1 = format.parse("2023-08-16 00:00:00.0");
        noteRequest.setNoteTypeName("OT module");
        noteRequest.setNoteDate(noteDate1);
        noteRequest.setNoteText("Some text");

        Note note = new Note();
        NoteType noteType = new NoteType();
        Date existingNote = format.parse("2023-08-16 00:00:00.0");
        noteType.setName("OT module");
        note.setId(1);
        note.setNoteDate(existingNote);
        note.setNoteType(noteType);
        Mockito.when(noteDao.getNote(any(Date.class), any(String.class))).thenReturn(note);
        validator.validate(noteRequest, noteRequestErrors);
        assertEquals(true, "Note entry exist for noteType and noteDate".matches(noteRequestErrors.getAllErrors().get(0).getCode().toString()));
    }
}

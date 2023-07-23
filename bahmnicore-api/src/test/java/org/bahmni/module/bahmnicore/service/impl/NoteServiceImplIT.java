package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.drugorder.DrugOrderConfigResponse;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.model.NoteType;
import org.bahmni.module.bahmnicore.service.NoteService;
import org.hibernate.HibernateException;
import org.hibernate.PropertyValueException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.DrugOrder;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.*;

public class NoteServiceImplIT extends BaseIntegrationTest {

    @Before
    public void setUp() throws Exception {
        executeDataSet("notesData.xml");
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldCreateNewNoteOfSpecificNoteType() {
        NoteService noteService = Context.getService(NoteService.class);
        Note note = new Note();
        NoteType noteType  =  noteService.getNoteType("OT module");
        note.setNoteType(noteType);
        note.setNoteText("note one");
        note.setNoteDate(new Date());
        noteService.createNote(note);
        assertNotNull(note.getId());
        assertNotNull(note.getUuid());
        assertEquals(note.getNoteText(), "note one");
        assertEquals(note.getNoteType().getName(), "OT module");
        assertNotNull(note.getNoteDate());
    }

    @Test
    public void shouldCreateNewNotesOfSpecificNoteType() {
        NoteService noteService = Context.getService(NoteService.class);
        NoteType noteType  =  noteService.getNoteType("OT module");
        Note note1 = new Note();
        note1.setNoteType(noteType);
        note1.setNoteText("note one");
        note1.setNoteDate(new Date());

        Note note2 = new Note();
        note2.setNoteType(noteType);
        note2.setNoteText("Hello World Two");
        note2.setNoteDate(new Date());

        Note noteObjectOne = noteService.createNote(note1);
        Note noteObjectTwo = noteService.createNote(note2);

        assertNotNull(noteObjectOne);
        assertEquals(noteObjectOne.getNoteText(), note1.getNoteText());
        assertEquals(noteObjectOne.getNoteType().getName(), note1.getNoteType().getName());
        assertNotNull(noteObjectOne.getNoteDate());

        assertNotNull(noteObjectTwo);
        assertEquals(noteObjectTwo.getNoteText(), note2.getNoteText());
        assertEquals(noteObjectTwo.getNoteType().getName(), note2.getNoteType().getName());
        assertNotNull(noteObjectTwo.getNoteDate());

    }

    @Test
    public void shouldGetNoteOfSpecificTypeAndDate() throws Exception {
        NoteService noteService = Context.getService(NoteService.class);
        NoteType noteType = new NoteType();
        noteType.setName("OT module");
        Note note = new Note();
        note.setNoteType(noteType);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Date date = format.parse("2023-08-16 00:00:00.0");
        note.setNoteDate(date);
        Note noteResponse = noteService.getNote(date,"OT module" );
        assertNotNull(noteResponse);
        assertEquals(noteResponse.getNoteText(), "note one");
        assertEquals(noteResponse.getNoteType().getName(), "OT module");
        assertNotNull(noteResponse.getNoteDate());
    }


    @Test
    public void shouldThrowExceptionWhenNoteTypeIsEmpty() throws Exception {
        NoteService noteService = Context.getService(NoteService.class);
        Note note = new Note();
        note.setNoteText("Hello World");
        note.setNoteDate(new Date());
        expectedException.expect(HibernateException.class);
        noteService.createNote(note);
    }

    @Test
    public void shouldExceptionWhenNoteDateIsEmpty() throws Exception {
        NoteService noteService = Context.getService(NoteService.class);
        Note note = new Note();
        note.setNoteText("Hello World");
        note.setNoteDate(new Date());
        expectedException.expect(HibernateException.class);
        noteService.createNote(note);

    }


}

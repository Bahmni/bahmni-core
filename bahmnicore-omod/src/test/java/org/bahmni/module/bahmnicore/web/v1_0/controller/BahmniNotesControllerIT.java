package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.NoteRequest;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.model.NoteType;
import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.List;

import static org.jcodec.common.Assert.assertNotNull;

public class BahmniNotesControllerIT  extends BaseIntegrationTest {

    @Autowired
    private BahmniNotesController bahmniNotesController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("NoteInfo.xml");
    }

    @Test
    public void createNewNote() throws Exception {
      String content = "{\"noteTypeText\": \"OT module\"},\n" +
              "\"noteDate\": \"2023-07-08\",\n" +
              "\"noteText\": \"sample text\"}";

        NoteType noteType = new NoteType();
        noteType.setName("hello");
        Note note = new Note();
        note.setNoteType(noteType);
        note.setNoteText("Hello World");
        bahmniNotesController.save((List<NoteRequest>) note);
    }
}

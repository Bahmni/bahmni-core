package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.contract.NoteRequest;
import org.bahmni.module.bahmnicore.contract.NoteResponse;
import org.bahmni.module.bahmnicore.dao.NoteDao;
import org.bahmni.module.bahmnicore.dao.impl.NoteDaoImpl;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.service.NoteService;
import org.bahmni.module.bahmnicore.service.impl.NoteServiceImpl;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    @Autowired
    private NoteService noteService;

    public NoteResponse map(Note note){
        NoteResponse noteResponse = new NoteResponse();
        noteResponse.setNoteId(note.getNoteId());
        noteResponse.setNoteDate(note.getNoteDate());
        noteResponse.setNoteText(note.getNoteText());
        noteResponse.setUuid(note.getUuid());
        noteResponse.setNoteTypeName(note.getNoteType().getName());
        return noteResponse;
    }

    public Note mapRequest(NoteRequest noteRequest){
        Note note = new Note();
        note.setNoteId(noteRequest.getNoteId());
        note.setNoteDate(noteRequest.getNoteDate());
        note.setNoteText(noteRequest.getNoteText());
        note.setUuid(noteRequest.getUuid());
        note.setNoteType(noteService.getNoteType(noteRequest.getNoteTypeName()));
        return note;
    }
}

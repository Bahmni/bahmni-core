package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.contract.NoteRequestResponse;
import org.bahmni.module.bahmnicore.model.Note;
import org.bahmni.module.bahmnicore.service.NoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    @Autowired
    private NoteService noteService;

    public NoteRequestResponse mapResponse(Note note){
        NoteRequestResponse noteResponse = new NoteRequestResponse();
        noteResponse.setNoteId(note.getNoteId());
        noteResponse.setNoteDate(note.getNoteDate());
        noteResponse.setNoteText(note.getNoteText());
        noteResponse.setUuid(note.getUuid());
        noteResponse.setNoteTypeName(note.getNoteType().getName());
        return noteResponse;
    }

    public Note mapRequest(NoteRequestResponse noteRequest){
        Note note = new Note();
        note.setNoteId(noteRequest.getNoteId());
        note.setNoteDate(noteRequest.getNoteDate());
        note.setNoteText(noteRequest.getNoteText());
        note.setUuid(noteRequest.getUuid());
        note.setNoteType(noteService.getNoteType(noteRequest.getNoteTypeName()));
        return note;
    }
}

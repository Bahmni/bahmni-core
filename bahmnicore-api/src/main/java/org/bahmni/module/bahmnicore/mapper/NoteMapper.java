/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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

package org.bahmni.module.bahmnicore.validator;


import org.bahmni.module.bahmnicore.contract.NoteRequest;
import org.bahmni.module.bahmnicore.dao.NoteDao;
import org.bahmni.module.bahmnicore.model.Note;
import org.openmrs.annotation.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Component
public class NoteValidator implements Validator {

    @Autowired
    private NoteDao noteDao;

    @Override
    public boolean supports(Class c) {
        return NoteRequest.class.isAssignableFrom(c);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        NoteRequest noteRequest = (NoteRequest) obj;
        if (noteRequest == null) {
            errors.reject("error.general");
        } else {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noteTypeName", "Note.noteType.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noteText", "Note.noteText.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noteDate", "Note.noteDate.required");
        }

        Note note = noteDao.getNote(noteRequest.getNoteDate(), noteRequest.getNoteTypeName());
        if(nonNull(note)) {
            errors.reject("Note entry exist for notetype and noteDate");
        }
    }


}

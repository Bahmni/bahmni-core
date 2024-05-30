package org.bahmni.module.bahmnicore.validator;


import org.bahmni.module.bahmnicore.contract.NoteRequestResponse;
import org.bahmni.module.bahmnicore.dao.NoteDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import static java.util.Objects.nonNull;

@Component
public class NoteValidator implements Validator {

    @Autowired
    private NoteDao noteDao;

    @Override
    public boolean supports(Class c) {
        return NoteRequestResponse.class.isAssignableFrom(c);
    }

    @Override
    public void validate(Object obj, Errors errors) {
        NoteRequestResponse noteRequest = (NoteRequestResponse) obj;
        if (noteRequest == null) {
            errors.reject("error.general");
        } else {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noteTypeName", "Note.noteType.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noteText", "Note.noteText.required");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noteDate", "Note.noteDate.required");
        }

        if(nonNull(noteDao.getNote(noteRequest.getNoteDate(), noteRequest.getNoteTypeName()))) {
            errors.reject("Note entry exist for noteType and noteDate");
        }
    }


}

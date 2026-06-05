package org.bahmni.module.bahmnicore.web.v1_0;

import org.openmrs.api.APIException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN, reason = "Visit for this patient is closed. You cannot do an 'Undo Discharge' for the patient.")
public class VisitClosedException extends APIException {
    public VisitClosedException(String message){
        super(message);
    }
}

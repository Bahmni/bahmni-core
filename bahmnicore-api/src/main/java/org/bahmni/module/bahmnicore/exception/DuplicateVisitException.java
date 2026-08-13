package org.bahmni.module.bahmnicore.exception;

import org.openmrs.api.APIException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DuplicateVisitException extends APIException {

    public DuplicateVisitException(String message) {
        super(message);
    }
}

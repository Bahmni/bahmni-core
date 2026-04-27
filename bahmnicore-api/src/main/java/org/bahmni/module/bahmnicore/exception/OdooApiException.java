package org.bahmni.module.bahmnicore.exception;

import org.apache.http.HttpStatus;

public class OdooApiException extends RuntimeException {

    public OdooApiException(String message) {
        super(message);
    }

    public OdooApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

package org.bahmni.module.bahmnicore.exception;

public class OdooApiException extends RuntimeException {

    public OdooApiException(String message) {
        super(message);
    }

    public OdooApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

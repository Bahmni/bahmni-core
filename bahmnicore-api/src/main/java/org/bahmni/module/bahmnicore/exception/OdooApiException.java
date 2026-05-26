package org.bahmni.module.bahmnicore.exception;

public class OdooApiException extends RuntimeException {

    private final int httpStatusCode;

    public OdooApiException(String message) {
        super(message);
        this.httpStatusCode = 0;

    }

    public OdooApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = 0;

    }

    public OdooApiException(String message, int httpStatusCode, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;

    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

}

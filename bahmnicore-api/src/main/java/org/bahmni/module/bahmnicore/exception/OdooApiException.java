package org.bahmni.module.bahmnicore.exception;

public class OdooApiException extends RuntimeException {

    private final int httpStatusCode;
    private final String responseBody;

    public OdooApiException(String message) {
        super(message);
        this.httpStatusCode = 0;
        this.responseBody = null;
    }

    public OdooApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = 0;
        this.responseBody = null;
    }

    public OdooApiException(String message, int httpStatusCode, Throwable cause) {
        super(message, cause);
        this.httpStatusCode = httpStatusCode;
        this.responseBody = null;
    }

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

}

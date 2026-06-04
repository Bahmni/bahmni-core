package org.bahmni.module.bahmnicore;

public class ApplicationError extends RuntimeException {
    private int errorCode;

    public ApplicationError(String message, Throwable throwable) {
        super(message, throwable);
    }

    public ApplicationError(String message) {
        super(message);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}

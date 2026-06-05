package org.bahmni.module.bahmnicore;


public class BahmniCoreException extends ApplicationError {
    public BahmniCoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public BahmniCoreException(String message) {
        super(message);
    }
}
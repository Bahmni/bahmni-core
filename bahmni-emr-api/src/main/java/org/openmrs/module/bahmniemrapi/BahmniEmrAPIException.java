package org.openmrs.module.bahmniemrapi;


public class BahmniEmrAPIException extends RuntimeException {
    public BahmniEmrAPIException(String message, Throwable cause) {
        super(message, cause);
    }

    public BahmniEmrAPIException(String message) {
        super(message);
    }
}
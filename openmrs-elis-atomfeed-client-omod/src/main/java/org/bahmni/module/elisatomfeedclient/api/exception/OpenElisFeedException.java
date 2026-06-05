package org.bahmni.module.elisatomfeedclient.api.exception;

public class OpenElisFeedException extends RuntimeException {
    public OpenElisFeedException(String message, Exception e) {
        super(message, e);
    }

    public OpenElisFeedException(String message) {
        super(message);
    }
}

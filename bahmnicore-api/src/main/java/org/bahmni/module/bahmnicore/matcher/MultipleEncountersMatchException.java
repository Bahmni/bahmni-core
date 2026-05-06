package org.bahmni.module.bahmnicore.matcher;

public class MultipleEncountersMatchException extends RuntimeException {

    public MultipleEncountersMatchException(String message) {
        super(message);
    }

    public MultipleEncountersMatchException(String message, Throwable cause) {
        super(message, cause);
    }
}

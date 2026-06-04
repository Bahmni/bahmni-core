package org.bahmni.module.bahmnicore.bahmniexceptions;

import org.openmrs.api.APIException;

public class FileTypeNotSupportedException extends APIException {
    public FileTypeNotSupportedException(String message) {
        super(message);
    }
}

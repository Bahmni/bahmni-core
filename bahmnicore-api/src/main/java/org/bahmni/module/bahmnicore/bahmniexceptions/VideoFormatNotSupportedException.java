package org.bahmni.module.bahmnicore.bahmniexceptions;

import org.openmrs.api.APIException;

public class VideoFormatNotSupportedException extends APIException{
    public VideoFormatNotSupportedException(String message) {
        super(message);
    }
}

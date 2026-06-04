package org.bahmni.module.referencedata.labconcepts.validator;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;

import java.util.List;

public class Validator {
    public void throwExceptionIfExists(List<String> errors) {
        String message = StringUtils.join(errors, "\n");
        if (!StringUtils.isBlank(message)) {
            throw new APIException(message);
        }
    }
}

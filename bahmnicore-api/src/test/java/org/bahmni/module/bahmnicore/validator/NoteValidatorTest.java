package org.bahmni.module.bahmnicore.validator;

import org.bahmni.module.bahmnicore.model.Note;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.RelationshipType;
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.validator.RelationshipTypeValidator;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;

import static org.junit.jupiter.api.Assertions.*;

public class NoteValidatorTest extends BaseContextSensitiveTest {

    public void validate_shouldFailValidationIfaIsToBIsNullOrEmptyOrWhitespace() throws Exception {
        Note type = new Note();

        Errors errors = new BindException(type, "type");
        new NoteValidator().validate(type, errors);
        Assert.assertTrue(errors.hasFieldErrors("aIsToB"));
    }

}

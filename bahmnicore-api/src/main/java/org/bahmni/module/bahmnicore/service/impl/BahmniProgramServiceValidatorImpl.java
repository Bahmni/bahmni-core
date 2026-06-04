package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.service.BahmniProgramServiceValidator;
import org.openmrs.PatientProgram;
import org.openmrs.api.APIException;
import org.springframework.stereotype.Component;

@Component
public class BahmniProgramServiceValidatorImpl implements BahmniProgramServiceValidator {
    public void validate(PatientProgram patientProgram) throws APIException {}
}

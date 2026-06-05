package org.bahmni.module.bahmnicore.service;

import org.openmrs.PatientProgram;
import org.openmrs.api.APIException;

public interface BahmniProgramServiceValidator {
    void validate(PatientProgram patientProgram) throws APIException;
}

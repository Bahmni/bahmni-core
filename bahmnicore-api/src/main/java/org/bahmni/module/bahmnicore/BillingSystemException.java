package org.bahmni.module.bahmnicore;

import org.openmrs.Patient;

public class BillingSystemException extends ApplicationError {
    private Patient patient;

    public BillingSystemException(String message, Throwable throwable, Patient patient) {
        super(message, throwable);
        this.patient = patient;
    }

    public Patient getPatient() {
        return patient;
    }
}

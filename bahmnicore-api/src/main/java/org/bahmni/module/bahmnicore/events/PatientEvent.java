package org.bahmni.module.bahmnicore.events;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

public class PatientEvent extends BahmniEvent {

    private Patient patient;

    public PatientEvent(BahmniEventType bahmniEventType, Patient patient) {
        super(bahmniEventType);
        this.patient = patient;
        this.payloadId=patient.getUuid();
    }

    public Patient getPatient() {
        return patient;
    }
}


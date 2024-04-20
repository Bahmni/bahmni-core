package org.bahmni.module.bahmnicore.service;

import org.openmrs.Location;
import org.openmrs.Patient;

import java.util.Date;
import java.util.List;
import java.util.Locale;

public interface SMSService {

    String getPrescriptionMessage(Locale locale, Date visitDate, Patient patient, Location location, List<String> providerList, String prescriptionDetail);

    Object sendSMS(String phoneNumber, String message);
}

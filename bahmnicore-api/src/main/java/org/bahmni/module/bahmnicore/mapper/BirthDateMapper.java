package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.Age;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.openmrs.Patient;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class BirthDateMapper {
	
	public Patient map(Patient patient, BahmniPatient bahmniPatient) {
		Date birthDate = bahmniPatient.getBirthdate();
		Age age = bahmniPatient.getAge();
		if (birthDate != null) {
			patient.setBirthdate(birthDate);
			patient.setBirthdateEstimated(false);
			
		} else if (age != null) {
            patient.setBirthdate(age.getDateOfBirth());
			patient.setBirthdateEstimated(true);
		}
		return patient;
	}

    public BahmniPatient mapFromPatient(BahmniPatient bahmniPatient, Patient patient) {
        if(bahmniPatient == null){
            bahmniPatient = new BahmniPatient();
        }

        if(patient.getBirthdateEstimated()){
            bahmniPatient.setAge(Age.fromBirthDate(patient.getBirthdate()));
            return bahmniPatient;
        }
        bahmniPatient.setBirthDate(patient.getBirthdate());
        bahmniPatient.setAge(Age.fromBirthDate(patient.getBirthdate()));
        return bahmniPatient;
    }
}

package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniName;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.openmrs.Patient;
import org.openmrs.PersonName;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PersonNameMapper {
	
	public Patient map(Patient patient, List<BahmniName> names) {
		
		int oldNumberOfNames = patient.getNames().size();
		addName(patient, names);
		int newNumberOfNames = patient.getNames().size();
		
		voidEarlierNames(patient, oldNumberOfNames, newNumberOfNames);
		
		return patient;
	}

    public BahmniPatient mapFromPatient(BahmniPatient bahmniPatient, Patient patient) {
        if (bahmniPatient == null){
            bahmniPatient = new BahmniPatient();
        }
        bahmniPatient.addName(new BahmniName(patient.getGivenName(), patient.getFamilyName()));

		return bahmniPatient;
	}
	
	private void voidEarlierNames(Patient patient, int oldNumberOfNames, int newNumberOfNames) {
		if (newNumberOfNames > oldNumberOfNames) {
			for (PersonName name : patient.getNames()) {
				if (name.getId() != null) {
					name.setVoided(true);
				}
			}
		}
	}
	
	private void addName(Patient patient, List<BahmniName> names) {
		for (BahmniName name : names) {
            PersonName personName = new PersonName(name.getGivenName(), null, name.getFamilyName());
            personName.setPreferred(true);
            patient.addName(personName);
		}
	}
}

package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.bahmnicore.model.BahmniPersonAttribute;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PersonAttributeMapper {
	
	private PersonService personService;

    @Autowired
    public PersonAttributeMapper(PersonService personService) {
        this.personService = personService;
    }

    public Patient map(Patient patient, List<BahmniPersonAttribute> attributes) {
		for (BahmniPersonAttribute attribute : attributes) {
			if (attribute.getPersonAttributeUuid() == null || attribute.getValue() == null)
				continue;
			
			PersonAttribute personAttribute = new PersonAttribute();
			personAttribute.setAttributeType(personService.getPersonAttributeTypeByUuid(
			    attribute.getPersonAttributeUuid().toString()));
			personAttribute.setValue(attribute.getValue().toString());
			patient.addAttribute(personAttribute);
		}
		return patient;
	}

    public BahmniPatient mapFromPatient(BahmniPatient bahmniPatient, Patient patient) {
        if(bahmniPatient == null){
            bahmniPatient = new BahmniPatient();
        }
        Set<PersonAttribute> attributes = patient.getAttributes();
        for (PersonAttribute attribute : attributes) {
            bahmniPatient.addAttribute(new BahmniPersonAttribute(attribute.getAttributeType().getUuid(), attribute.getValue()));
        }
        return bahmniPatient;
    }

}

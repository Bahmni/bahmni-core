package org.bahmni.module.admin.csv.service;

import org.apache.commons.lang.StringUtils;
import org.bahmni.common.config.registration.service.RegistrationPageService;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.PatientRow;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateStringInSupportedFormat;
import static org.bahmni.module.admin.csv.utils.CSVUtils.getDateFromString;

public class CSVPatientService {

    private static final String BAHMNI_PRIMARY_IDENTIFIER_TYPE = "bahmni.primaryIdentifierType";
    private static final String REGISTRATION_PAGE_VALIDATION_MANDATORY_FIELD = "bahmni.registration.validation.mandatoryField";
    private boolean DEFAULT_MANDATORY_FIELD_VALIDATION = true;

    private PatientService patientService;
    private PersonService personService;
    private ConceptService conceptService;
    private AdministrationService administrationService;
    private CSVAddressService csvAddressService;
    private RegistrationPageService registrationPageService;

    public CSVPatientService(PatientService patientService, PersonService personService, ConceptService conceptService, AdministrationService administrationService, CSVAddressService csvAddressService, RegistrationPageService registrationPageService) {
        this.patientService = patientService;
        this.personService = personService;
        this.conceptService = conceptService;
        this.administrationService = administrationService;
        this.csvAddressService = csvAddressService;
        this.registrationPageService = registrationPageService;
    }

    public Patient save(PatientRow patientRow) throws ParseException {
        Patient patient = new Patient();
        PersonName personName = new PersonName(patientRow.firstName, patientRow.middleName, patientRow.lastName);
        personName.setPreferred(true);
        patient.addName(personName);

        addPersonAttributes(patient, patientRow);

        if (!StringUtils.isBlank(patientRow.birthdate)) {
            patient.setBirthdate(getDateFromString(patientRow.birthdate));
        } else if (!StringUtils.isBlank(patientRow.age)) {
            patient.setBirthdateFromAge(Integer.parseInt(patientRow.age), new Date());
        }
        patient.setGender(patientRow.gender);
        patient.addIdentifier(new PatientIdentifier(patientRow.registrationNumber, getPatientIdentifierType(), null));

        List<KeyValue> addressParts = patientRow.addressParts;
        PersonAddress personAddress = csvAddressService.getPersonAddress(addressParts);
        if (personAddress != null) {
            patient.addAddress(personAddress);
        }

        patient.setPersonDateCreated(patientRow.getRegistrationDate());

        patientService.savePatient(patient);

        return patient;
    }

    private void addPersonAttributes(Patient patient, PatientRow patientRow) throws ParseException {
        final List<String> mandatoryAttributes = registrationPageService.getMandatoryAttributes();
    	for (KeyValue attribute : patientRow.attributes) {
            if (validateForMandatoryAttribute(mandatoryAttributes, attribute))
                continue;
            PersonAttributeType personAttributeType = findAttributeType(attribute.getKey());
    		if (personAttributeType.getFormat().equalsIgnoreCase("org.openmrs.Concept")) {
    		    Concept concept = getConceptByName(attribute.getValue());
    		    if (concept != null) {
    		    	patient.addAttribute(new PersonAttribute(personAttributeType, concept.getId().toString()));
    		    } else {
    		    	throw new RuntimeException("Invalid value for Attribute." + attribute.getKey());
    		    }
    		} else if (personAttributeType.getFormat().startsWith("java.lang.")) {
    		    patient.addAttribute(new PersonAttribute(findAttributeType(attribute.getKey()), attribute.getValue()));
    		} else if (personAttributeType.getFormat().startsWith("org.openmrs.util.AttributableDate")) {

    		    String dateString = attribute.getValue();
                //Validating the Date format
                getDateFromString(dateString);
                String supportedDateString = getDateStringInSupportedFormat(dateString);
    		    patient.addAttribute(new PersonAttribute(findAttributeType(attribute.getKey()),supportedDateString));
    		}
    	}
    }

    private boolean validateForMandatoryAttribute(List<String> mandatoryAttributes, KeyValue attribute) {
        boolean skipCurrentAttribute = false;
        final boolean mandatoryFieldValidationRequired = isMandatoryFieldValidationRequired();
        if(StringUtils.isBlank(attribute.getValue())) {
            if(mandatoryFieldValidationRequired) {
                if(mandatoryAttributes == null)
                    throw new RuntimeException("Error in reading patient registration config");
                else if(mandatoryAttributes.contains(attribute.getKey()))
                    throw new RuntimeException(String.format("Missing value for mandatory attribute \"%s\"", attribute.getKey()));
                else
                    skipCurrentAttribute = true;
            } else
                skipCurrentAttribute = true;
        }
        return skipCurrentAttribute;
    }

    private Concept getConceptByName(String name) {
        List<Concept> concepts = conceptService.getConceptsByName(name);
        if (concepts != null) {
            for (Concept concept : concepts) {
                Collection<ConceptName> nameCollection = concept.getNames();
                for (ConceptName conceptName : nameCollection) {
                    if (conceptName.getName().equalsIgnoreCase(name) && (conceptName.isPreferred() || conceptName.isFullySpecifiedName() || conceptName.isShort())) {
                        return concept;
                    }
                }
            }
        }
        return null;
    }

    private PersonAttributeType findAttributeType(String key) {
        for (PersonAttributeType personAttributeType : personService.getAllPersonAttributeTypes(false)) {
            if (key.equalsIgnoreCase(personAttributeType.getName())) {
                return personAttributeType;
            }
        }

        throw new RuntimeException(String.format("Person Attribute %s not found", key));
    }

    private PatientIdentifierType getPatientIdentifierType() {
        String globalProperty = administrationService.getGlobalProperty(BAHMNI_PRIMARY_IDENTIFIER_TYPE);
        return patientService.getPatientIdentifierTypeByUuid(globalProperty);
    }

    private boolean isMandatoryFieldValidationRequired() {
        String mandatoryFieldValidationGlobalProperty = administrationService.getGlobalProperty(REGISTRATION_PAGE_VALIDATION_MANDATORY_FIELD);
        return StringUtils.isNotBlank(mandatoryFieldValidationGlobalProperty) ? Boolean.valueOf(mandatoryFieldValidationGlobalProperty) : DEFAULT_MANDATORY_FIELD_VALIDATION;
    }

}

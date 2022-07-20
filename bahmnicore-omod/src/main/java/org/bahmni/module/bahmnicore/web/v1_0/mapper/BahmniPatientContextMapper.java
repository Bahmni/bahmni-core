package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.PatientProgramAttribute;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PatientProgram;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.patient.PatientContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BahmniPatientContextMapper {
    @Autowired
    private ConceptService conceptService;

    public PatientContext map(Patient patient, PatientProgram patientProgram, List<String> configuredPersonAttributes, List<String> configuredProgramAttributes, List<String> configuredPatientIdentifiers, PatientIdentifierType primaryIdentifierType) {
        PatientContext patientContext = new PatientContext();

        patientContext.setBirthdate(patient.getBirthdate());
        patientContext.setFamilyName(patient.getFamilyName());
        patientContext.setGivenName(patient.getGivenName());
        patientContext.setMiddleName(patient.getMiddleName());
        patientContext.setGender(patient.getGender());
        patientContext.setIdentifier(patient.getPatientIdentifier(primaryIdentifierType).getIdentifier());
        patientContext.setUuid(patient.getUuid());

        mapConfiguredPersonAttributes(patient, configuredPersonAttributes, patientContext);
        mapConfiguredProgramAttributes(patientProgram, configuredProgramAttributes, patientContext);
        mapConfiguredPatientIdentifier(patient, configuredPatientIdentifiers, patientContext,primaryIdentifierType);
        return patientContext;
    }

    private void mapConfiguredPatientIdentifier(Patient patient, List<String> configuredPatientIdentifiers, PatientContext patientContext, PatientIdentifierType primaryIdentifierType) {
        if (CollectionUtils.isEmpty(configuredPatientIdentifiers)) {
            return;
        }
        for (String configuredPatientIdentifier : configuredPatientIdentifiers) {
            PatientIdentifier patientIdentifier = patient.getPatientIdentifier(configuredPatientIdentifier);
            if (patientIdentifier != null && !configuredPatientIdentifier.equals(primaryIdentifierType.getName())) {
                patientContext.addAdditionalPatientIdentifier(configuredPatientIdentifier, patientIdentifier.getIdentifier());
            }
        }
    }

    private void mapConfiguredProgramAttributes(PatientProgram patientProgram, List<String> configuredProgramAttributes, PatientContext patientContext) {
        if (patientProgram == null || configuredProgramAttributes == null) {
            return;
        }
        for (String configuredProgramAttribute : configuredProgramAttributes) {
            for (PatientProgramAttribute patientProgramAttribute : patientProgram.getActiveAttributes()) {
                if (patientProgramAttribute.getAttributeType().getName().equals(configuredProgramAttribute)) {
                    if (patientProgramAttribute.getAttributeType().getDatatypeClassname().equals("org.bahmni.module.bahmnicore.customdatatype.datatype.CodedConceptDatatype")) {
                        Concept concept = conceptService.getConcept(patientProgramAttribute.getValueReference());
                        patientContext.addProgramAttribute(configuredProgramAttribute, patientProgramAttribute.getAttributeType().getDescription(), concept.getName().getName());
                    } else {
                        patientContext.addProgramAttribute(configuredProgramAttribute, patientProgramAttribute.getAttributeType().getDescription(), patientProgramAttribute.getValueReference());
                    }
                }
            }
        }
    }

    private void mapConfiguredPersonAttributes(Patient patient, List<String> configuredPersonAttributes, PatientContext patientContext) {
        if (configuredPersonAttributes == null) {
            return;
        }
        for (String configuredPersonAttribute : configuredPersonAttributes) {
            PersonAttribute personAttribute = patient.getAttribute(configuredPersonAttribute);
            mapPersonAttribute(patientContext, configuredPersonAttribute, personAttribute);
        }
    }

    private void mapPersonAttribute(PatientContext patientContext, String configuredPersonAttribute, PersonAttribute personAttribute) {
        if (personAttribute != null) {
            if (personAttribute.getAttributeType().getFormat().equals("org.openmrs.Concept")) {
                Concept concept = conceptService.getConcept(personAttribute.getValue());
                patientContext.addPersonAttribute(configuredPersonAttribute, personAttribute.getAttributeType().getDescription(), concept.getName().getName());
            } else {
                patientContext.addPersonAttribute(configuredPersonAttribute, personAttribute.getAttributeType().getDescription(), personAttribute.getValue());
            }
        }
    }
}

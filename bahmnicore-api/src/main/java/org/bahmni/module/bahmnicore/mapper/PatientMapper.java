package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.openmrs.Patient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    private PersonNameMapper personNameMapper;

    private BirthDateMapper birthDateMapper;

    private PersonAttributeMapper personAttributeMapper;

    private AddressMapper addressMapper;

    private final PatientIdentifierMapper patientIdentifierMapper;

    @Autowired
    public PatientMapper(PersonNameMapper personNameMapper, BirthDateMapper birthDateMapper,
                         PersonAttributeMapper personAttributeMapper, AddressMapper addressMapper,
                         PatientIdentifierMapper patientIdentifierMapper) {
        this.personNameMapper = personNameMapper;
        this.birthDateMapper = birthDateMapper;
        this.personAttributeMapper = personAttributeMapper;
        this.addressMapper = addressMapper;
        this.patientIdentifierMapper = patientIdentifierMapper;
    }

    public Patient map(Patient patient, BahmniPatient bahmniPatient) {
        if (patient == null) {
            patient = new Patient();
            patient.setPersonDateCreated(bahmniPatient.getPersonDateCreated());
            patient.setUuid(bahmniPatient.getUuid());
        }
        patient.setGender(bahmniPatient.getGender());
        patient = personNameMapper.map(patient, bahmniPatient.getNames());
        patient = birthDateMapper.map(patient, bahmniPatient);
        patient = personAttributeMapper.map(patient, bahmniPatient.getAttributes());
        patient = addressMapper.map(patient, bahmniPatient.getAddresses());
        patient = patientIdentifierMapper.map(bahmniPatient, patient);
        return patient;
    }

}

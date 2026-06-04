package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.PatientProgram;
import org.openmrs.PatientProgramAttribute;
import org.openmrs.ProgramAttributeType;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.patient.PatientContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BahmniPatientContextMapperTest {

    private BahmniPatientContextMapper bahmniPatientContextMapper = new BahmniPatientContextMapper();
    private PatientIdentifierType primaryIdentifierType;

    @Before
    public void setUp() throws Exception {
        primaryIdentifierType = new PatientIdentifierType();
        primaryIdentifierType.setName("Primary Identifier");
    }

    @Test
    public void shouldMapPatientInformationToPatientContext() {
        Patient patient = new Patient();
        patient.setBirthdate(new Date());
        patient.setGender("Male");
        patient.setNames(getPersonNames("GivenName", "MiddleName", "FamilyName"));
        patient.addIdentifier(createPrimaryIdentifier("GAN20000"));
        Set<PersonAttribute> attributes = new LinkedHashSet<>();
        attributes.add(getPersonAttribute("Caste", "Caste", "Caste Value", "java.lang.String"));
        attributes.add(getPersonAttribute("Education", "Education", "Education Value", "java.lang.String"));
        patient.setAttributes(attributes);

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, new PatientProgram(), Collections.singletonList("Caste"), Collections.singletonList("IRDB Number"), null, primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals(patient.getBirthdate(), patientContext.getBirthdate());
        assertEquals(patient.getIdentifiers().iterator().next().getIdentifier(), patientContext.getIdentifier());
        assertEquals(patient.getGender(), patientContext.getGender());
        assertEquals(patient.getFamilyName(), patientContext.getFamilyName());
        assertEquals(patient.getMiddleName(), patientContext.getMiddleName());
        assertEquals(patient.getGivenName(), patientContext.getGivenName());
        assertEquals(1, patientContext.getPersonAttributes().size());
        assertEquals("Caste Value", patientContext.getPersonAttributes().get("Caste").get("value"));
        assertEquals("Caste", patientContext.getPersonAttributes().get("Caste").get("description"));
    }

    @Test
    public void shouldNotReturnPersonAttributesIfTheConfiguredAttributesAreNotExists() {
        Patient patient = setUpPatient();

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, new PatientProgram(), Collections.singletonList("Caste"), Arrays.asList("IRDB Number"), null, primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals(0, patientContext.getPersonAttributes().size());
    }

    private Patient setUpPatient() {
        Patient patient = new Patient();
        Set<PersonName> names = getPersonNames("GivenName", "MiddleName", "FamilyName");
        patient.setNames(names);
        PatientIdentifier primaryIdentifier = createPrimaryIdentifier("GAN20000");
        Set<PatientIdentifier> identifiers = new HashSet<>();
        identifiers.addAll(Collections.singletonList(primaryIdentifier));
        patient.setIdentifiers(identifiers);
        return patient;
    }

    private PatientIdentifier createPrimaryIdentifier(String value) {
        return new PatientIdentifier(value, primaryIdentifierType, null);
    }

    @Test
    public void shouldMapProgramAttributesToPatientContext() {
        Patient patient = setUpPatient();

        PatientProgram patientProgram = new PatientProgram();
        HashSet<PatientProgramAttribute> patientProgramAttributes = new HashSet<>();
        patientProgramAttributes.add(getPatientProgramAttribute("IRDB Number", "IRDB Number Description", "1234", "String"));
        patientProgramAttributes.add(getPatientProgramAttribute("TSRT Number", "TSRT Number", "9876", "String"));
        patientProgram.setAttributes(patientProgramAttributes);
        PatientContext patientContext = bahmniPatientContextMapper.map(patient, patientProgram, Collections.singletonList("Caste"), Collections.singletonList("IRDB Number"), null, primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals(1, patientContext.getProgramAttributes().size());
        assertEquals("1234", patientContext.getProgramAttributes().get("IRDB Number").get("value"));
    }

    @Test
    public void shouldNotReturnProgramAttributesIfTheConfiguredAttributesAreNotExists() {
        Patient patient = setUpPatient();

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, new PatientProgram(), Collections.singletonList("Caste"), Collections.singletonList("IRDB Number"), null, primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals(0, patientContext.getProgramAttributes().size());
    }

    @Test
    public void shouldNotReturnProgramAttributesIfTheProgramDoesNotExists() {
        Patient patient = setUpPatient();

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, null, Collections.singletonList("Caste"), Collections.singletonList("IRDB Number"), null, primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals(0, patientContext.getProgramAttributes().size());
    }

    @Test
    public void shouldNotReturnProgramAttributesIfNotConfigured() {
        Patient patient = setUpPatient();

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, new PatientProgram(), Collections.singletonList("Caste"), null, null, primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals(0, patientContext.getProgramAttributes().size());
    }

    @Test
    public void shouldNotReturnPersonAttributesIfNotConfigured() {
        Patient patient = setUpPatient();

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, new PatientProgram(), null, Collections.singletonList("IRDTB Number"), null, primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals(0, patientContext.getProgramAttributes().size());
    }

    @Test
    public void shouldReturnConfiguredExtraIdentifier() throws Exception {
        Patient patient = setUpPatient();
        PatientIdentifier nationalIdentifier = createIdentifier("National Identifier", "NAT10020");
        patient.addIdentifier(nationalIdentifier);

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, new PatientProgram(), null, null, Collections.singletonList("National Identifier"), primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals("GAN20000", patientContext.getIdentifier());
        assertEquals(1, patientContext.getAdditionalPatientIdentifiers().size());
        assertEquals("NAT10020", patientContext.getAdditionalPatientIdentifiers().get("National Identifier"));
    }

    @Test
    public void shouldNotReturnConfiguredExtraIdentifierIfDataIsNotCaptured() throws Exception {
        Patient patient = setUpPatient();

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, new PatientProgram(), null, null, Collections.singletonList("National Identifier"), primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals("GAN20000", patientContext.getIdentifier());
        assertEquals(0, patientContext.getAdditionalPatientIdentifiers().size());
    }

    @Test
    public void shouldNotReturnPrimaryIdentifierInExtraIdentifiersListIfConfigured() throws Exception {
        Patient patient = setUpPatient();

        PatientContext patientContext = bahmniPatientContextMapper.map(patient, new PatientProgram(), null, null, Collections.singletonList("Primary Identifier"), primaryIdentifierType);

        assertNotNull(patientContext);
        assertEquals("GAN20000", patientContext.getIdentifier());
        assertEquals(0, patientContext.getAdditionalPatientIdentifiers().size());
    }




    private PatientIdentifier createIdentifier(String type, String value) {
        PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
        patientIdentifierType.setName(type);
        return new PatientIdentifier(value, patientIdentifierType, null);
    }

    private Set<PersonName> getPersonNames(String givenName, String middleName, String familyName) {
        Set<PersonName> names = new LinkedHashSet<>();
        names.add(new PersonName(givenName, middleName, familyName));
        return names;
    }

    private PatientProgramAttribute getPatientProgramAttribute(String typeName, String typeDescription, String value, String dataTypeClassName) {
        PatientProgramAttribute patientProgramAttribute = new PatientProgramAttribute();
        ProgramAttributeType attributeType = new ProgramAttributeType();
        attributeType.setName(typeName);
        attributeType.setDescription(typeDescription);
        attributeType.setDatatypeClassname(dataTypeClassName);
        patientProgramAttribute.setAttributeType(attributeType);
        patientProgramAttribute.setValueReferenceInternal(value);
        return patientProgramAttribute;
    }

    private PersonAttribute getPersonAttribute(String typeName, String typeDescription, String value, String format) {
        PersonAttributeType attributeType = new PersonAttributeType();
        attributeType.setName(typeName);
        attributeType.setDescription(typeDescription);
        attributeType.setFormat(format);
        return new PersonAttribute(attributeType, value);
    }
}

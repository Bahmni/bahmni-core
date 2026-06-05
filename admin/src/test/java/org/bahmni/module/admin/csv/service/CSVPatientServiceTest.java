package org.bahmni.module.admin.csv.service;

import org.bahmni.common.config.registration.service.RegistrationPageService;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.PatientRow;
import org.bahmni.module.admin.csv.utils.CSVUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class CSVPatientServiceTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private PatientService mockPatientService;
    @Mock
    private PersonService mockPersonService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private AdministrationService mockAdminService;
    @Mock
    private AddressHierarchyService addressHierarchyService;
    @Mock
    private CSVAddressService csvAddressService;
    @Mock
    private RegistrationPageService registrationPageService;
    @Mock
    private UserContext userContext;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(mockAdminService);
        when(mockAdminService.getGlobalProperty(eq("bahmni.admin.csv.upload.dateFormat"))).thenReturn("yyyy-M-d");
    }

    @Test
    public void savePatientName() throws ParseException {
        PatientRow patientRow = new PatientRow();
        patientRow.firstName = "Romesh";
        patientRow.middleName = "Sharad";
        patientRow.lastName = "Powar";

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);

        csvPatientService.save(patientRow);

        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient capturedPatient = patientArgumentCaptor.getValue();
        assertEquals("Romesh", capturedPatient.getGivenName());
        assertEquals("Sharad", capturedPatient.getMiddleName());
        assertEquals("Powar", capturedPatient.getFamilyName());
    }

    @Test
    public void saveRegistrationNumberBirthdateGender() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN);

        PatientRow patientRow = new PatientRow();
        patientRow.age = "34";
        patientRow.gender = "Male";
        patientRow.registrationNumber = "reg-no";
        patientRow.birthdate = "1998-07-07";

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);

        csvPatientService.save(patientRow);

        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient capturedPatient = patientArgumentCaptor.getValue();
        assertEquals("Male", capturedPatient.getGender());
        assertEquals("reg-no", capturedPatient.getPatientIdentifier().getIdentifier());
        assertEquals(simpleDateFormat.parse("1998-07-07"), capturedPatient.getBirthdate());

    }

    @Test
    public void saveRegistrationNumberAgeGender() throws ParseException {
        PatientRow patientRow = new PatientRow();
        patientRow.age = "34";
        patientRow.gender = "Male";
        patientRow.registrationNumber = "reg-no";


        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);

        csvPatientService.save(patientRow);

        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient capturedPatient = patientArgumentCaptor.getValue();
        assertEquals("Male", capturedPatient.getGender());
        assertEquals("reg-no", capturedPatient.getPatientIdentifier().getIdentifier());
        assertEquals(new Integer(34), capturedPatient.getAge());

    }

    @Test
    public void saveAddressparts() throws ParseException {
        PatientRow patientRow = new PatientRow();

        List<KeyValue> addressParts = new ArrayList<KeyValue>() {{
            add(new KeyValue("Cities", "zhumri tallayya"));
            add(new KeyValue("States", "Timbaktu"));
            add(new KeyValue("Countries", "Bharat"));
            add(new KeyValue("ZipCode", "555555"));
        }};
        patientRow.addressParts = addressParts;

        AddressHierarchyLevel cityLevel = new AddressHierarchyLevel();
        cityLevel.setName("Cities");
        cityLevel.setAddressField(AddressField.CITY_VILLAGE);

        AddressHierarchyLevel stateLevel = new AddressHierarchyLevel();
        stateLevel.setName("States");
        stateLevel.setAddressField(AddressField.STATE_PROVINCE);

        AddressHierarchyLevel countryLevel = new AddressHierarchyLevel();
        countryLevel.setName("Countries");
        countryLevel.setAddressField(AddressField.COUNTRY);

        AddressHierarchyLevel postalCodeLevel = new AddressHierarchyLevel();
        postalCodeLevel.setName("ZipCode");
        postalCodeLevel.setAddressField(AddressField.POSTAL_CODE);

        ArrayList<AddressHierarchyLevel> addressHierarchyLevels = new ArrayList<>();
        addressHierarchyLevels.add(cityLevel);
        addressHierarchyLevels.add(stateLevel);
        addressHierarchyLevels.add(countryLevel);
        addressHierarchyLevels.add(postalCodeLevel);

        when(addressHierarchyService.getAddressHierarchyLevels()).thenReturn(addressHierarchyLevels);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, new CSVAddressService(addressHierarchyService), registrationPageService);
        csvPatientService.save(patientRow);

        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient patient = patientArgumentCaptor.getValue();
        Set<PersonAddress> addresses = patient.getAddresses();
        PersonAddress capturedAddress = addresses.iterator().next();
        assertEquals("zhumri tallayya", capturedAddress.getCityVillage());
        assertEquals("Timbaktu", capturedAddress.getStateProvince());
        assertEquals("Bharat", capturedAddress.getCountry());
        assertEquals("555555", capturedAddress.getPostalCode());
    }

    @Test
    public void savePersonAttributes() throws ParseException {
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Arrays.asList(
                createPersonAttributeType("familyNameLocal", "java.lang.String"),
                createPersonAttributeType("caste", "java.lang.String")
        ));
        PatientRow patientRow = new PatientRow();
        patientRow.attributes = new ArrayList<KeyValue>() {{
            add(new KeyValue("familyNameLocal", "ram"));
            add(new KeyValue("caste", "gond"));
        }};

        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        csvPatientService.save(patientRow);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient patient = patientArgumentCaptor.getValue();
        assertEquals(patient.getAttribute("familyNameLocal").getValue(), "ram");
        assertEquals(patient.getAttribute("caste").getValue(), "gond");
    }

    @Test
    public void shouldOnlyAddPersonAttributesOfFormatOpenMrsConceptAndJavaDataTypes() throws ParseException {
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Arrays.asList(
                createPersonAttributeType("education", "org.openmrs.Concept"),
                createPersonAttributeType("isUrban", "java.lang.Boolean"),
                createPersonAttributeType("visitDate", "org.openmrs.util.AttributableDate"),
                createPersonAttributeType("landHolding", "java.lang.Integer")
        ));

        Concept concept = new Concept();
        ConceptName conceptNameFullySpecified = new ConceptName();
        conceptNameFullySpecified.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        conceptNameFullySpecified.setName("123");
        concept.setNames(Collections.singleton(conceptNameFullySpecified));
        concept.setId(123);
        when(conceptService.getConceptsByName("123")).thenReturn(Collections.singletonList(concept));
        PatientRow patientRow = new PatientRow();
        patientRow.attributes = new ArrayList<KeyValue>() {{
            add(new KeyValue("education", "123"));
            add(new KeyValue("isUrban", "true"));
            add(new KeyValue("visitDate", "2016-11-22"));
            add(new KeyValue("landHolding", "222"));
        }};

        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        csvPatientService.save(patientRow);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient patient = patientArgumentCaptor.getValue();
        assertThat(patient.getAttributes().size(), is(4));
        assertThat(patient.getAttribute("education").getValue(), is("123"));
        assertThat(patient.getAttribute("isUrban").getValue(), is("true"));
        assertThat(patient.getAttribute("landHolding").getValue(), is("222"));
    }

    @Test
    public void shouldOnlyUseTheConceptIfItsFullySpecifiedOrShortNameMatchesTheCodedAnswer() throws ParseException {
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Collections.singletonList(
                createPersonAttributeType("confirmedByChw", "org.openmrs.Concept")
        ));

        Concept concept = new Concept();
        ConceptName conceptNameFullySpecified = new ConceptName();
        conceptNameFullySpecified.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        conceptNameFullySpecified.setName("Yes");
        ConceptName conceptNameShort = new ConceptName();
        conceptNameShort.setConceptNameType(ConceptNameType.SHORT);
        conceptNameShort.setName("yes");
        concept.setId(123);
        concept.setNames(Arrays.asList(conceptNameFullySpecified, conceptNameShort));

        Concept secondConcept = new Concept();
        ConceptName secondConceptNameFullySpecified = new ConceptName();
        secondConceptNameFullySpecified.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        secondConceptNameFullySpecified.setName("True");
        ConceptName secondConceptName = new ConceptName();
        secondConceptName.setName("Yes");
        secondConcept.setNames(Arrays.asList(secondConceptNameFullySpecified, secondConceptName));
        secondConcept.setId(321);

        when(conceptService.getConceptsByName("Yes")).thenReturn(Arrays.asList(concept, secondConcept));
        PatientRow patientRow = new PatientRow();
        patientRow.attributes = new ArrayList<KeyValue>() {{
            add(new KeyValue("confirmedByChw", "Yes"));
        }};

        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        csvPatientService.save(patientRow);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient patient = patientArgumentCaptor.getValue();
        assertThat(patient.getAttributes().size(), is(1));
        assertThat(patient.getAttribute("confirmedByChw").getValue(), is("123"));
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionIfTheCodedAttributeValueGivenIsInvalid() throws ParseException {
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Collections.singletonList(
                createPersonAttributeType("confirmedByChw", "org.openmrs.Concept")
        ));

        when(conceptService.getConceptsByName("Yes")).thenReturn(null);
        PatientRow patientRow = new PatientRow();
        patientRow.attributes = new ArrayList<KeyValue>() {{
            add(new KeyValue("confirmedByChw", "Yes"));
        }};

        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        csvPatientService.save(patientRow);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowAnExceptionIfNoFullySpecifiedNameMatches() throws ParseException {
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Collections.singletonList(
                createPersonAttributeType("confirmedByChw", "org.openmrs.Concept")
        ));

        Concept concept = new Concept();
        ConceptName conceptNameFullySpecified = new ConceptName();
        conceptNameFullySpecified.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        conceptNameFullySpecified.setName("Nope");
        ConceptName conceptNameShort = new ConceptName();
        conceptNameShort.setConceptNameType(ConceptNameType.SHORT);
        conceptNameShort.setName("nope");
        concept.setId(123);
        concept.setNames(Arrays.asList(conceptNameFullySpecified, conceptNameShort));

        Concept secondConcept = new Concept();
        ConceptName secondConceptNameFullySpecified = new ConceptName();
        secondConceptNameFullySpecified.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        secondConceptNameFullySpecified.setName("True");
        ConceptName secondConceptName = new ConceptName();
        secondConceptName.setName("Yes");
        secondConcept.setNames(Arrays.asList(secondConceptNameFullySpecified, secondConceptName));
        secondConcept.setId(321);

        when(conceptService.getConceptsByName("Yes")).thenReturn(Arrays.asList(concept, secondConcept));
        PatientRow patientRow = new PatientRow();
        patientRow.attributes = new ArrayList<KeyValue>() {{
            add(new KeyValue("confirmedByChw", "Yes"));
        }};

        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        csvPatientService.save(patientRow);
    }

    @Test
    public void failsWhenNonExistingAttributeIsImported() throws ParseException {
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Arrays.asList(createPersonAttributeType("familyNameLocal", "java.lang.String")));

        PatientRow patientRow = new PatientRow();
        patientRow.attributes = Arrays.asList(new KeyValue("nonExisting", "someValue"));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Person Attribute nonExisting not found");
        csvPatientService.save(patientRow);
    }

    @Test
    public void shouldThrowExceptionWhenMandatoryAttributeIsMissing() throws ParseException {
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Arrays.asList(createPersonAttributeType("familyNameLocal", "java.lang.String")));
        when(mockAdminService.getGlobalProperty(anyString())).thenReturn("true");
        when(registrationPageService.getMandatoryAttributes()).thenReturn(Arrays.asList("Admission Date"));

        PatientRow patientRow = new PatientRow();
        patientRow.attributes = Arrays.asList(new KeyValue("Admission Date", ""));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Missing value for mandatory attribute \"Admission Date\"");
        csvPatientService.save(patientRow);
    }

    @Test
    public void shouldThrowExceptionWhenPatientConfigIsNotAvailable() throws ParseException {
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Arrays.asList(createPersonAttributeType("familyNameLocal", "java.lang.String")));
        when(mockAdminService.getGlobalProperty(anyString())).thenReturn("true");
        when(registrationPageService.getMandatoryAttributes()).thenReturn(null);

        PatientRow patientRow = new PatientRow();
        patientRow.attributes = Arrays.asList(new KeyValue("Admission Date", ""));

        exception.expect(RuntimeException.class);
        exception.expectMessage("Error in reading patient registration config");
        csvPatientService.save(patientRow);
    }

    @Test
    public void shouldSkipAttributeCreationWhenEmptyAndNotMandatory() throws ParseException {
        CSVPatientService csvPatientService = new CSVPatientService(mockPatientService, mockPersonService, conceptService, mockAdminService, csvAddressService, registrationPageService);
        when(mockPersonService.getAllPersonAttributeTypes(false)).thenReturn(Arrays.asList(createPersonAttributeType("familyNameLocal", "java.lang.String")));
        when(mockAdminService.getGlobalProperty(anyString())).thenReturn("true");
        when(registrationPageService.getMandatoryAttributes()).thenReturn(Arrays.asList());

        PatientRow patientRow = new PatientRow();
        patientRow.attributes = Arrays.asList(new KeyValue("Admission Date", ""));

        csvPatientService.save(patientRow);

        ArgumentCaptor<Patient> patientArgumentCaptor = ArgumentCaptor.forClass(Patient.class);
        verify(mockPatientService).savePatient(patientArgumentCaptor.capture());

        Patient patient = patientArgumentCaptor.getValue();
        assertThat(patient.getAttributes().size(), is(0));
        assertNull(patient.getAttribute("Admission Date"));
    }

    private PersonAttributeType createPersonAttributeType(String name, String format) {
        PersonAttributeType personAttributeType = new PersonAttributeType();
        personAttributeType.setName(name);
        personAttributeType.setFormat(format);
        return personAttributeType;
    }
}

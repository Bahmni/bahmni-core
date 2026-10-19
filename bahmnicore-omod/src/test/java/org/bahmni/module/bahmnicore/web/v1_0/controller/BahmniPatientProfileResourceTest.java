package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Person;
import org.openmrs.Relationship;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.emrapi.patient.EmrPatientProfileService;
import org.openmrs.module.emrapi.patient.PatientProfile;
import org.openmrs.module.idgen.webservices.services.IdentifierSourceServiceWrapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.doThrow;

// TODO: 13/09/16 This is wrong way of writing test. We should mock the external dependency in resource but we ended up mocking all internal dependencies. For eg: MessageSourceService
// NOTE: Context static mocking has been migrated to native Mockito MockedStatic (see contextMockedStatic field below).
// @RunWith(PowerMockRunner.class)/@PrepareForTest(BahmniPatientProfileResource.class) and the org.powermock.api.mockito.PowerMockito
// spy()/doReturn()/doThrow() calls below are INTENTIONALLY KEPT: they stub the PRIVATE methods mapForCreatePatient/mapForUpdatePatient
// on a spy of the class under test via PowerMockito's reflection-based "doReturn(x).when(spy, "methodName", args)" API, which has
// no vanilla Mockito equivalent (vanilla Mockito.doReturn/when require calling the real method on the spy, which is impossible for
// a private method from a different class). See final migration report for details/options (e.g. widening method visibility).
@PrepareForTest({BahmniPatientProfileResource.class})
@RunWith(PowerMockRunner.class)
public class BahmniPatientProfileResourceTest {

    @Mock
    private EmrPatientProfileService emrPatientProfileService;

    @Mock
    private RestService restService;

    @Mock
    PatientResource1_8 patientResource1_8;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private PatientService patientService;

    @Mock
    private PersonService personService;
    @Mock
    private MessageSourceService messageSourceService;

    @Mock
    private IdentifierSourceServiceWrapper identifierSourceServiceWrapper;

    private BahmniPatientProfileResource bahmniPatientProfileResource;
    private SimpleObject propertiesToCreate;

    private MockedStatic<Context> contextMockedStatic;

    @Before
    public void setUp() throws IOException, java.net.URISyntaxException {
        MockitoAnnotations.initMocks(this);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        ClassLoader classLoader = getClass().getClassLoader();
        // NOTE: use toURI() rather than the classic `new File(url.getFile())` anti-pattern - the latter breaks here because
        // this checkout's path ("OpenMRS 2.8 Upgrade") contains spaces, which getResource() URL-encodes as %20; new File()
        // on the raw getFile() string then fails to resolve. toURI() correctly decodes the URL before building the File.
        File file = new File(classLoader.getResource("patient.json").toURI());
        String jsonString = FileUtils.readFileToString(file);
        propertiesToCreate = new SimpleObject().parseJson(jsonString);
        Patient patient = new Patient();
        patient.setGender("M");
        contextMockedStatic.when(() -> Context.getService(RestService.class)).thenReturn(restService);
        contextMockedStatic.when(Context::getPersonService).thenReturn(personService);
        contextMockedStatic.when(Context::getMessageSourceService).thenReturn(messageSourceService);
        contextMockedStatic.when(Context::getAdministrationService).thenReturn(administrationService);
        when(restService.getResourceBySupportedClass(Patient.class)).thenReturn(patientResource1_8);
        when(patientResource1_8.getPatient(any(SimpleObject.class))).thenReturn(patient);
        when(patientResource1_8.getPatientForUpdate(anyString(), any(SimpleObject.class))).thenReturn(patient);
    }

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }

    @Test
    public void createPatient() throws Exception {
        bahmniPatientProfileResource = new BahmniPatientProfileResource(emrPatientProfileService, identifierSourceServiceWrapper);
        BahmniPatientProfileResource bahmniPatientProfileResourceSpy = spy(this.bahmniPatientProfileResource);
        PatientProfile delegate = mock(PatientProfile.class);
        when(identifierSourceServiceWrapper.generateIdentifierUsingIdentifierSourceUuid("dead-cafe", "")).thenReturn("BAH300010");
        doReturn(delegate).when(bahmniPatientProfileResourceSpy, "mapForCreatePatient", propertiesToCreate);
        when(emrPatientProfileService.save(delegate)).thenReturn(delegate);
        contextMockedStatic.when(Context::getPatientService).thenReturn(patientService);
        Patient patient = mock(Patient.class);
        when(patient.getUuid()).thenReturn("patientUuid");
        when(delegate.getPatient()).thenReturn(patient);
        PatientIdentifier patientIdentifier = mock(PatientIdentifier.class);
        Set<PatientIdentifier> patientIdentifiers = new HashSet<>();
        patientIdentifiers.add(patientIdentifier);
        when(patient.getIdentifiers()).thenReturn(patientIdentifiers);
        doNothing().when(bahmniPatientProfileResourceSpy).setConvertedProperties(any(PatientProfile.class), any(SimpleObject.class), any(DelegatingResourceDescription.class), any(Boolean.class));
        Person person = new Person();
        person.setUuid("personUuid");
        when(personService.getPersonByUuid("patientUuid")).thenReturn(person);
        List<Relationship> relationships = Arrays.asList();
        when(personService.getRelationshipsByPerson(person)).thenReturn(relationships);

        ResponseEntity<Object> response = bahmniPatientProfileResourceSpy.create(false, propertiesToCreate);

        Assert.assertEquals(200, response.getStatusCode().value());
        verify(identifierSourceServiceWrapper, times(1)).generateIdentifierUsingIdentifierSourceUuid("dead-cafe", "");
        verify(personService, times(1)).getPersonByUuid("patientUuid");
        verify(delegate, times(1)).setRelationships(relationships);
    }

    @Test
    public void updatePatient() throws Exception {
        bahmniPatientProfileResource = new BahmniPatientProfileResource(emrPatientProfileService, identifierSourceServiceWrapper);
        BahmniPatientProfileResource spy = spy(bahmniPatientProfileResource);
        PatientProfile delegate = mock(PatientProfile.class);
        doReturn(delegate).when(spy, "mapForUpdatePatient", anyString(), any(SimpleObject.class));
        when(emrPatientProfileService.save(delegate)).thenReturn(delegate);
        doNothing().when(spy).setConvertedProperties(any(PatientProfile.class), any(SimpleObject.class), any(DelegatingResourceDescription.class), any(Boolean.class));
        Person person = new Person();
        person.setUuid("personUuid");
        when(personService.getPersonByUuid("patientUuid")).thenReturn(person);
        List<Relationship> relationships = Collections.emptyList();
        when(personService.getRelationshipsByPerson(person)).thenReturn(relationships);
        Patient patient = mock(Patient.class);
        when(patient.getUuid()).thenReturn("patientUuid");
        when(delegate.getPatient()).thenReturn(patient);

        ResponseEntity<Object> response = spy.update("someUuid", propertiesToCreate);

        Assert.assertEquals(200, response.getStatusCode().value());
        verify(personService, times(1)).getPersonByUuid("patientUuid");
        verify(delegate, times(2)).setRelationships(relationships);
    }

    @Test
    public void shouldThrowExceptionWhenPatientIsNotHavingProperPrivilege() throws Exception {
        bahmniPatientProfileResource = new BahmniPatientProfileResource(emrPatientProfileService, identifierSourceServiceWrapper);
        BahmniPatientProfileResource spy = spy(bahmniPatientProfileResource);
        doThrow(new APIAuthenticationException()).when(spy, "mapForUpdatePatient", anyString(), any(SimpleObject.class));

        ResponseEntity<Object> response = spy.update("someUuid", propertiesToCreate);
        Assert.assertEquals(403,response.getStatusCode().value());
    }
}
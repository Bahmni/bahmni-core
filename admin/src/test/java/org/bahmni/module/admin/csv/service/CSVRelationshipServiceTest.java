package org.bahmni.module.admin.csv.service;

import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class CSVRelationshipServiceTest {
    @Mock
    private BahmniPatientService patientService;

    @Mock
    private PersonService personService;

    @Mock
    private ProviderService providerService;

    @Mock
    @Qualifier("adminService")
    private AdministrationService administrationService;

    @Mock
    private UserContext userContext;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private CSVRelationshipService csvRelationshipService;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(eq("bahmni.admin.csv.upload.dateFormat"))).thenReturn("yyyy-M-d");
        csvRelationshipService = new CSVRelationshipService(patientService, personService, providerService, administrationService);
    }

    @Test
    public void shouldFailIfPersonADoesNotExist() throws Exception {
        when(patientService.get("", true)).thenReturn(null);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("No matching patients found with ID:'null'");

        csvRelationshipService.save(new RelationshipRow());
    }

    @Test
    public void shouldFailIfAisToBRelationshipDoesNotExist() throws Exception {
        when(patientService.get(null, true)).thenReturn(getPatients());
        when(patientService.getByAIsToB("")).thenReturn(null);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("No matching relationship type found with relationship type name:'null'");

        csvRelationshipService.save(new RelationshipRow());
    }

    @Test
    public void shouldFailIfBisToARelationshipDoesNotExist() throws Exception {
        when(patientService.get("GAN200012", true)).thenReturn(getPatients());
        ArrayList<RelationshipType> relationshipTypes = getRelationshipTypes();
        when(patientService.getByAIsToB("Parent")).thenReturn(relationshipTypes);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("No matching relationship type found with relationship type name:'something'");

        csvRelationshipService.save(new RelationshipRow("GAN200012", "","", "something", null, null));
    }

    @Test
    public void shouldFailIfPersonBDoesNotExist() throws Exception {
        when(patientService.get("GAN200012", true)).thenReturn(getPatients());
        ArrayList<RelationshipType> relationshipTypes = getRelationshipTypes();
        when(patientService.getByAIsToB("Parent")).thenReturn(relationshipTypes);
        when(patientService.get("GAN200013", true)).thenReturn(null);

        when(administrationService.getGlobalProperty(anyString())).thenReturn("{patient: [\"Parent\"]}");

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("No matching patients found with ID:'GAN200013'");

        csvRelationshipService.save(new RelationshipRow("GAN200012", "GAN200013", "", "Parent", null, null));
    }

    @Test
    public void shouldFailIfPersonBAsProviderDoesNotExist() throws Exception {
        when(patientService.get("GAN200012", true)).thenReturn(getPatients());
        ArrayList<RelationshipType> relationshipTypes = getRelationshipTypes();
        when(patientService.getByAIsToB("Parent")).thenReturn(relationshipTypes);
        when(providerService.getProviders("Super User", null, null, null)).thenReturn(null);

        when(administrationService.getGlobalProperty(anyString())).thenReturn("{provider: [\"Parent\"]}");

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("No matching provider found with ID:'Super User'");

        csvRelationshipService.save(new RelationshipRow("GAN200012", "", "Super User", "Parent", null, null));
    }

    @Test
    public void shouldFailIfRelationshipMapDoesNotExist() throws Exception {
        when(patientService.get("GAN200012", true)).thenReturn(getPatients());
        when(patientService.get("GAN200013", true)).thenReturn(null);
        ArrayList<RelationshipType> relationshipTypes = getRelationshipTypes();
        when(patientService.getByAIsToB("Parent")).thenReturn(relationshipTypes);
        when(providerService.getProviders("Super User", null, null, null)).thenReturn(null);

        when(administrationService.getGlobalProperty(anyString())).thenReturn(null);

        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Relationship not found ProviderName");

        csvRelationshipService.save(new RelationshipRow("GAN200012", "GAN200013", "ProviderName", "Parent", null, null));
    }

    @Test
    public void shouldSaveRelationship() throws Exception {
        when(patientService.get("GAN200012", true)).thenReturn(getPatients());
        ArrayList<RelationshipType> relationshipTypes = getRelationshipTypes();
        when(patientService.getByAIsToB("Doctor")).thenReturn(relationshipTypes);
        when(providerService.getProviders("Super User", null, null, null)).thenReturn(getProviders());
        when(administrationService.getGlobalProperty(anyString())).thenReturn("{provider: [\"Doctor\"]}");
        when(administrationService.getGlobalProperty(eq("bahmni.admin.csv.upload.dateFormat"))).thenReturn("yyyy-M-d");

        Relationship expectedRelationship = new Relationship();
        expectedRelationship.setPersonA(getPatients().get(0));
        expectedRelationship.setPersonB(getProviders().get(0).getPerson());
        when(personService.saveRelationship(any(Relationship.class))).thenReturn(expectedRelationship);

        Relationship relationship = csvRelationshipService.save(new RelationshipRow("GAN200012", "", "Super User", "Doctor", null, null));
        assertNotNull("Relationship should not be null", relationship);
        assertEquals(expectedRelationship.getPersonA(), relationship.getPersonA());
        assertEquals(expectedRelationship.getPersonB(), relationship.getPersonB());

    }

    private List<Patient> getPatients() {
        List<Patient> patients = new ArrayList<>();
        Patient patient = new Patient();
        patient.setId(1);
        patients.add(patient);
        return patients;
    }

    private ArrayList<Provider> getProviders() {
        ArrayList<Provider> providers = new ArrayList<Provider>();
        Provider provider = new Provider();
        provider.setName("Super User");
        providers.add(provider);
        return providers;
    }

    private ArrayList<RelationshipType> getRelationshipTypes() {
        ArrayList<RelationshipType> relationshipTypes = new ArrayList<RelationshipType>();
        RelationshipType relationshipType = new RelationshipType();
        relationshipType.setaIsToB("Parent");
        relationshipType.setbIsToA("Child");
        relationshipTypes.add(relationshipType);
        return relationshipTypes;
    }
}
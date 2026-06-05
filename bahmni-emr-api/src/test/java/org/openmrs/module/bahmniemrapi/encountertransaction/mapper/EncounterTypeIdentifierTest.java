package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.openmrs.EncounterType;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmnimapping.services.BahmniLocationService;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class EncounterTypeIdentifierTest {

    @Mock
    private BahmniLocationService bahmniLocationService;
    @Mock
    private EncounterService encounterService;
    @Mock
    private AdministrationService administrationService;
    private EncounterTypeIdentifier identifier;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        initMocks(this);
        identifier = new EncounterTypeIdentifier(bahmniLocationService, encounterService, administrationService);
    }

    @Test
    public void shouldGetEncounterTypeBasedOnLocationWhenEncounterTypeNameIsNotSet() throws Exception {
        BahmniEncounterTransaction encounterTransaction = new BahmniEncounterTransaction();
        String locationUuid = UUID.randomUUID().toString();
        encounterTransaction.setEncounterTypeUuid(null);
        encounterTransaction.setEncounterType(null);
        encounterTransaction.setLocationUuid(locationUuid);
        EncounterType encounterTypeMappedToLocation = new EncounterType();
        encounterTypeMappedToLocation.setUuid(UUID.randomUUID().toString());
        when(bahmniLocationService.getEncounterType(locationUuid)).thenReturn(encounterTypeMappedToLocation);

        EncounterType actualEncounterType = identifier.getEncounterTypeFor(encounterTransaction.getEncounterType(), encounterTransaction.getLocationUuid());

        assertEquals(encounterTypeMappedToLocation, actualEncounterType);
    }

    @Test
    public void shouldGetEncounterTypeWhenEncounterTypeNameIsSet() throws Exception {
        BahmniEncounterTransaction encounterTransaction = new BahmniEncounterTransaction();
        String locationUuid = UUID.randomUUID().toString();
        encounterTransaction.setEncounterTypeUuid(null);
        encounterTransaction.setEncounterType("Consultation");
        encounterTransaction.setLocationUuid(locationUuid);

        identifier.getEncounterTypeFor(encounterTransaction.getEncounterType(), encounterTransaction.getLocationUuid());

        assertEquals(null, encounterTransaction.getEncounterTypeUuid());
        verify(encounterService).getEncounterType("Consultation");
        verifyZeroInteractions(bahmniLocationService);
    }

    @Test
    public void shouldGetDefaultEncounterTypeWhenNoEncounterTypeFoundForLocationAndEncounterTypeNameIsNotSet() throws Exception {
        String locationUuid = "location-uuid";
        BahmniEncounterTransaction encounterTransaction = new BahmniEncounterTransaction();
        EncounterType defaultEncounterType = new EncounterType();
        encounterTransaction.setEncounterTypeUuid(null);
        encounterTransaction.setEncounterType(null);
        encounterTransaction.setLocationUuid(locationUuid);
        when(bahmniLocationService.getEncounterType(locationUuid)).thenReturn(null);
        when(administrationService.getGlobalProperty("bahmni.encounterType.default")).thenReturn("Field Consultation");
        when(encounterService.getEncounterType("Field Consultation")).thenReturn(defaultEncounterType);

        EncounterType actualEncounterType = identifier.getEncounterTypeFor(encounterTransaction.getEncounterType(), encounterTransaction.getLocationUuid());

        assertEquals(defaultEncounterType, actualEncounterType);
        verify(bahmniLocationService).getEncounterType(locationUuid);
        verify(administrationService).getGlobalProperty("bahmni.encounterType.default");
        verify(encounterService).getEncounterType("Field Consultation");
    }

    @Test
    public void shouldReturnNullWhenNoEncounterTypeFoundForDefaultEncounterTypeGlobalProperty() throws Exception {
        String locationUuid = "location-uuid";
        BahmniEncounterTransaction encounterTransaction = new BahmniEncounterTransaction();
        encounterTransaction.setEncounterTypeUuid(null);
        encounterTransaction.setEncounterType(null);
        encounterTransaction.setLocationUuid(locationUuid);
        when(bahmniLocationService.getEncounterType(locationUuid)).thenReturn(null);
        when(administrationService.getGlobalProperty("bahmni.encounterType.default")).thenReturn("Field Consultation");
        when(encounterService.getEncounterType("Field Consultation")).thenReturn(null);

        EncounterType actualEncounterType = identifier.getEncounterTypeFor(encounterTransaction.getEncounterType(), encounterTransaction.getLocationUuid());

        assertNull(actualEncounterType);
        verify(bahmniLocationService).getEncounterType(locationUuid);
        verify(administrationService).getGlobalProperty("bahmni.encounterType.default");
    }

}
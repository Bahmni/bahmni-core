package org.bahmni.datamigration;

import org.junit.Test;
import org.mockito.Mock;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class AddressServiceTest {
    @Mock
    private MasterTehsils masterTehsils;
    @Mock
    private AmbiguousTehsils ambiguousTehsils;
    @Mock
    private CorrectedTehsils correctedTehsils;

    @Test
    public void getTehsilFor() {
        initMocks(this);
        FullyQualifiedTehsil rightTehsil = new FullyQualifiedTehsil("Kota", "Dota", "Stota");
        when(masterTehsils.getFullyQualifiedTehsil("Kota")).thenReturn(rightTehsil);
        when(ambiguousTehsils.contains("Kota")).thenReturn(false);
        when(correctedTehsils.correctedTehsil("Kota")).thenReturn("Kota");

        AddressService addressService = new AddressService(masterTehsils, ambiguousTehsils, correctedTehsils);
        FullyQualifiedTehsil tehsilFor = addressService.getTehsilFor(new FullyQualifiedTehsil("Kota", "WrDota", "WrStota"));
        assertEquals(rightTehsil, tehsilFor);
    }

    @Test
    public void getTehsilFor2() {
        initMocks(this);
        FullyQualifiedTehsil rightTehsil = new FullyQualifiedTehsil("Kota", "Dota", "Stota");
        when(masterTehsils.getFullyQualifiedTehsil("Kota")).thenReturn(rightTehsil);
        when(ambiguousTehsils.contains("Kota")).thenReturn(true);
        when(correctedTehsils.correctedTehsil("Kota")).thenReturn("Kota");

        AddressService addressService = new AddressService(masterTehsils, ambiguousTehsils, correctedTehsils);
        FullyQualifiedTehsil tehsilFromPatientRecord = new FullyQualifiedTehsil("Kota", "WrDota", "WrStota");
        FullyQualifiedTehsil tehsilFor = addressService.getTehsilFor(tehsilFromPatientRecord);
        assertEquals(tehsilFromPatientRecord, tehsilFor);
    }
}
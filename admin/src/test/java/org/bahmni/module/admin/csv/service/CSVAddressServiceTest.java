package org.bahmni.module.admin.csv.service;

import org.bahmni.csv.KeyValue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.PersonAddress;
import org.openmrs.module.addresshierarchy.AddressField;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CSVAddressServiceTest {
    private AddressHierarchyService mockAddressHierarchyService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        mockAddressHierarchyService = mock(AddressHierarchyService.class);
    }

    @Test
    public void mapThroughAddressHierarchyLevels() {
        List<KeyValue> addressParts = new ArrayList<KeyValue>() {{
            add(new KeyValue("Cities", "zhumri tallayya"));
            add(new KeyValue("States", "Timbaktu"));
            add(new KeyValue("Countries", "Bharat"));
            add(new KeyValue("ZipCode", "555555"));
        }};

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
        when(mockAddressHierarchyService.getAddressHierarchyLevels()).thenReturn(addressHierarchyLevels);

        CSVAddressService csvAddressService = new CSVAddressService(mockAddressHierarchyService);
        PersonAddress personAddress = csvAddressService.getPersonAddress(addressParts);

        assertEquals("zhumri tallayya", personAddress.getCityVillage());
        assertEquals("Timbaktu", personAddress.getStateProvince());
        assertEquals("Bharat", personAddress.getCountry());
        assertEquals("555555", personAddress.getPostalCode());
    }

    @Test
    public void throwErrorWhenAddressLevelNotFound() {
        List<KeyValue> addressParts = new ArrayList<KeyValue>() {{
            add(new KeyValue("Cities", "zhumri tallayya"));
        }};

        AddressHierarchyLevel cityLevel = new AddressHierarchyLevel();
        cityLevel.setName("City");
        cityLevel.setAddressField(AddressField.CITY_VILLAGE);

        ArrayList<AddressHierarchyLevel> addressHierarchyLevels = new ArrayList<>();
        addressHierarchyLevels.add(cityLevel);

        when(mockAddressHierarchyService.getAddressHierarchyLevels()).thenReturn(addressHierarchyLevels);

        exception.expect(RuntimeException.class);
        exception.expectMessage(String.format("Address Hierarchy level {0} does not exist.", "Cities"));

        CSVAddressService csvAddressService = new CSVAddressService(mockAddressHierarchyService);
        csvAddressService.getPersonAddress(addressParts);
    }

    @Test
    public void mapOtherAddressHierarchyLevels() {
        List<KeyValue> addressParts = new ArrayList<KeyValue>() {{
            add(new KeyValue("tehsil", "zhumri tallayya"));
            add(new KeyValue("gram panchayat", "Timbaktu"));
        }};

        AddressHierarchyLevel tehsilLevel = new AddressHierarchyLevel();
        tehsilLevel.setName("tehsil");
        tehsilLevel.setAddressField(AddressField.ADDRESS_1);

        AddressHierarchyLevel panchayatLevel = new AddressHierarchyLevel();
        panchayatLevel.setName("gram panchayat");
        panchayatLevel.setAddressField(AddressField.ADDRESS_2);


        ArrayList<AddressHierarchyLevel> addressHierarchyLevels = new ArrayList<>();
        addressHierarchyLevels.add(tehsilLevel);
        addressHierarchyLevels.add(panchayatLevel);
        when(mockAddressHierarchyService.getAddressHierarchyLevels()).thenReturn(addressHierarchyLevels);

        CSVAddressService csvAddressService = new CSVAddressService(mockAddressHierarchyService);
        PersonAddress personAddress = csvAddressService.getPersonAddress(addressParts);

        assertEquals("zhumri tallayya", personAddress.getAddress1());
        assertEquals("Timbaktu", personAddress.getAddress2());
    }

}
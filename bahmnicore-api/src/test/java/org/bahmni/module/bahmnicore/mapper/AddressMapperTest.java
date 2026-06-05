package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniAddress;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.bahmnicore.util.AddressMother;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class AddressMapperTest {

    @Test
    public void shouldAddASingleAddressIfNonePresent() {
        Patient patient = new Patient();
        SimpleObject simpleObjectForAddress = new AddressMother().getSimpleObjectForAddress();
        AddressMapper addressMapper = new AddressMapper();

        BahmniAddress bahmniAddress = new BahmniAddress(simpleObjectForAddress);
        addressMapper.map(patient, Arrays.asList(bahmniAddress));

        Set<PersonAddress> addresses = patient.getAddresses();
        assertEquals(addresses.size(), 1);
        PersonAddress personAddress = addresses.iterator().next();
        assertAllFieldsAreMapped("Added address should map to passed in Bahmni address", bahmniAddress, personAddress);
    }

    @Test
    public void shouldUpdateExistingAddressIfANonVoidedAddressPresent() {
        Patient patient = new Patient();
        PersonAddress address = createPersonAddress("old", 1);
        patient.addAddress(address);


        SimpleObject simpleObjectForAddress = new AddressMother().getSimpleObjectForAddress();
        AddressMapper addressMapper = new AddressMapper();


        BahmniAddress bahmniAddress = new BahmniAddress(simpleObjectForAddress);
        addressMapper.map(patient, Arrays.asList(bahmniAddress));

        Set<PersonAddress> addresses = patient.getAddresses();
        assertEquals(addresses.size(), 1);
        assertAllFieldsAreMapped("Existing address should map to passed in Bahmni address", bahmniAddress, address);
    }

    @Test
    public void shouldUpdateExistingNonVoidedAddressOnly() {
        Patient patient = new Patient();
        PersonAddress nonVoidedAddress = createPersonAddress("nonVoided", 1);
        PersonAddress voidedAddress = createPersonAddress("voided", 2);
        voidedAddress.setVoided(true);
        patient.addAddress(nonVoidedAddress);
        patient.addAddress(voidedAddress);

        SimpleObject simpleObjectForAddress = new AddressMother().getSimpleObjectForAddress();
        AddressMapper addressMapper = new AddressMapper();


        BahmniAddress bahmniAddress = new BahmniAddress(simpleObjectForAddress);
        addressMapper.map(patient, Arrays.asList(bahmniAddress));

        Set<PersonAddress> addresses = patient.getAddresses();
        assertEquals("Size of address should not change", addresses.size(), 2);
        assertAllFieldsAreMapped("Existing nonVoided address should map to passed in Bahmni address", bahmniAddress, nonVoidedAddress);
        assertTrue("Details of voided address should not change", voidedAddress.equalsContent(createPersonAddress("voided", 2)));
    }

    @Test
    public void shouldMapPatientToBahmniPatient() {
        Patient patient = new Patient();
        PersonAddress address = createPersonAddress("foo", 123);
        patient.setAddresses(new HashSet<PersonAddress>(Arrays.asList(address)));

        BahmniPatient bahmniPatient = new AddressMapper().mapFromPatient(null, patient);

        PersonAddress personAddress = patient.getPersonAddress();
        BahmniAddress bahmniAddress = bahmniPatient.getAddresses().get(0);
        assertAllFieldsAreMapped("Address should be mapped from Patient", bahmniAddress,  personAddress);
    }

    private PersonAddress createPersonAddress(String randomPrefix, Integer id) {
        PersonAddress address = new PersonAddress();
        address.setAddress1(randomPrefix + "address1");
        address.setAddress2(randomPrefix + "address2");
        address.setAddress3(randomPrefix + "address3");
        address.setCityVillage(randomPrefix + "cityVillage");
        address.setCountyDistrict(randomPrefix + "countyDistrict");
        address.setStateProvince(randomPrefix + "stateProvince");
        address.setId(id);
        return address;
    }

    private void assertAllFieldsAreMapped(String message, BahmniAddress bahmniAddress, PersonAddress personAddress) {
        assertEquals(message + "address1", bahmniAddress.getAddress1(), personAddress.getAddress1());
        assertEquals(bahmniAddress.getAddress2(), personAddress.getAddress2());
        assertEquals(bahmniAddress.getAddress3(), personAddress.getAddress3());
        assertEquals(bahmniAddress.getCityVillage(), personAddress.getCityVillage());
        assertEquals(bahmniAddress.getCountyDistrict(), personAddress.getCountyDistrict());
        assertEquals(bahmniAddress.getStateProvince(), personAddress.getStateProvince());
    }
}

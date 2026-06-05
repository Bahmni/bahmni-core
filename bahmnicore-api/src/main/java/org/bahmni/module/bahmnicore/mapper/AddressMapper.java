package org.bahmni.module.bahmnicore.mapper;

import org.bahmni.module.bahmnicore.model.BahmniAddress;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class AddressMapper {

    public Patient map(Patient patient, List<BahmniAddress> addresses) {
        PersonAddress personAddress = getNonVoidedAddress(patient);

        if (personAddress == null) {
            personAddress = new PersonAddress();
            populateAddress(personAddress, addresses);
            patient.addAddress(personAddress);
        } else {
            populateAddress(personAddress, addresses);
        }

        return patient;
    }

    private PersonAddress getNonVoidedAddress(Patient patient) {
        PersonAddress personAddress = null;
        Set<PersonAddress> patientAddresses = patient.getAddresses();
        for (PersonAddress address : patientAddresses) {
            if (!address.isVoided())  personAddress = address;
        }
        return personAddress;
    }

    private void populateAddress(PersonAddress personAddress1, List<BahmniAddress> addresses) {
        PersonAddress personAddress = personAddress1;

        for (BahmniAddress address : addresses) {
            personAddress.setAddress1(address.getAddress1());
            personAddress.setAddress2(address.getAddress2());
            personAddress.setAddress3(address.getAddress3());
            personAddress.setAddress4(address.getAddress4());
            personAddress.setAddress5(address.getAddress5());
            personAddress.setAddress6(address.getAddress6());
            personAddress.setCityVillage(address.getCityVillage());
            personAddress.setCountyDistrict(address.getCountyDistrict());
            personAddress.setStateProvince(address.getStateProvince());
            personAddress.setPostalCode(address.getPostalCode());
            personAddress.setCountry(address.getCountry());
            personAddress.setLatitude(address.getLatitude());
            personAddress.setLongitude(address.getLongitude());
            personAddress.setPreferred(true);
        }
    }

    public BahmniPatient mapFromPatient(BahmniPatient bahmniPatient, Patient patient) {
        if(bahmniPatient == null){
            bahmniPatient = new BahmniPatient();
        }
        PersonAddress personAddress = patient.getPersonAddress();
        if(personAddress != null){
            BahmniAddress bahmniAddress = new BahmniAddress();
            bahmniAddress.setAddress1(personAddress.getAddress1());
            bahmniAddress.setAddress2(personAddress.getAddress2());
            bahmniAddress.setAddress3(personAddress.getAddress3());
            bahmniAddress.setAddress4(personAddress.getAddress4());
            bahmniAddress.setAddress5(personAddress.getAddress5());
            bahmniAddress.setAddress6(personAddress.getAddress6());
            bahmniAddress.setCityVillage(personAddress.getCityVillage());
            bahmniAddress.setCountyDistrict(personAddress.getCountyDistrict());
            bahmniAddress.setStateProvince(personAddress.getStateProvince());
            bahmniAddress.setPostalCode(personAddress.getPostalCode());
            bahmniAddress.setCountry(personAddress.getCountry());
            bahmniAddress.setLatitude(personAddress.getLatitude());
            bahmniAddress.setLongitude(personAddress.getLongitude());
            bahmniPatient.addAddress(bahmniAddress);
        }
        return bahmniPatient;
    }
}
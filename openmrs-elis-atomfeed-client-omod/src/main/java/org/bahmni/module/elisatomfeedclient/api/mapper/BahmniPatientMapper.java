package org.bahmni.module.elisatomfeedclient.api.mapper;

import org.bahmni.module.bahmnicore.model.BahmniAddress;
import org.bahmni.module.bahmnicore.model.BahmniName;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.bahmnicore.model.BahmniPersonAttribute;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisPatient;
import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisPatientAttribute;
import org.joda.time.DateTime;
import org.openmrs.PersonAttributeType;

import java.util.List;


public class BahmniPatientMapper {

    private List<PersonAttributeType> allPersonAttributeTypes;

    public BahmniPatientMapper(List<PersonAttributeType> allPersonAttributeTypes) {
        this.allPersonAttributeTypes = allPersonAttributeTypes;
    }

    public BahmniPatient map(OpenElisPatient openElisPatient) {
        BahmniPatient bahmniPatient = new BahmniPatient();
        bahmniPatient.setGender(openElisPatient.getGender());
        bahmniPatient.setPersonDateCreated(DateTime.now().toDate());
        bahmniPatient.addName(new BahmniName(openElisPatient.getFirstName(), openElisPatient.getLastName()));
        bahmniPatient.setBirthDate(openElisPatient.getDateOfBirthAsDate());

        BahmniAddress bahmniAddress = new BahmniAddress();
        bahmniAddress.setAddress1(openElisPatient.getAddress1());
        bahmniAddress.setAddress2(openElisPatient.getAddress2());
        bahmniAddress.setAddress3(openElisPatient.getAddress3());
        bahmniAddress.setCityVillage(openElisPatient.getCityVillage());
        bahmniAddress.setCountyDistrict(openElisPatient.getCountyDistrict());
        bahmniAddress.setStateProvince(openElisPatient.getStateProvince());
        bahmniPatient.addAddress(bahmniAddress);
        bahmniPatient.setUuid(openElisPatient.getPatientUUID());
        bahmniPatient.setIdentifier(openElisPatient.getPatientIdentifier());
        bahmniPatient.setCenter(openElisPatient.getHealthCenter());

        mapCutomAttributes(openElisPatient, bahmniPatient);

        return bahmniPatient;
    }

    private void mapCutomAttributes(OpenElisPatient openElisPatient, BahmniPatient bahmniPatient) {
        for (OpenElisPatientAttribute openElisPatientAttribute : openElisPatient.getAttributes()) {
            String name = openElisPatientAttribute.getName();
            for (PersonAttributeType attributeType : allPersonAttributeTypes) {
                if (attributeType.getName().toUpperCase().equals(name) && attributeType.getFormat().equals("java.lang.String")) {
                    bahmniPatient.addAttribute(new BahmniPersonAttribute(attributeType.getUuid(), openElisPatientAttribute.getValue()));
                    break;
                }
            }
        }
    }

}

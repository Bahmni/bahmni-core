package org.bahmni.module.admin.csv.service;

import org.bahmni.csv.KeyValue;
import org.openmrs.PersonAddress;
import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.addresshierarchy.util.AddressHierarchyUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CSVAddressService {

    private AddressHierarchyService addressHierarchyService;
    private List<AddressHierarchyLevel> addressHierarchyLevels;

    public CSVAddressService() {
    }

    public CSVAddressService(AddressHierarchyService addressHierarchyService) {
        this.addressHierarchyService = addressHierarchyService;
    }

    public PersonAddress getPersonAddress(List<KeyValue> addressParts) {
        if (addressHierarchyLevels == null) {
            addressHierarchyLevels = addressHierarchyService.getAddressHierarchyLevels();
        }

        return mapPersonAddressFields(addressParts, addressHierarchyLevels);
    }

    private PersonAddress mapPersonAddressFields(List<KeyValue> addressParts, List<AddressHierarchyLevel> addressHierarchyLevels) {
        Map<String, String> addressFieldToValueMap = new HashMap<>();
        for (KeyValue addressPart : addressParts) {
            AddressHierarchyLevel addressHierarchyLevel = findAddressHierarchyLevel(addressPart.getKey(), addressHierarchyLevels);
            addressFieldToValueMap.put(addressHierarchyLevel.getAddressField().getName(), addressPart.getValue());
        }
        return AddressHierarchyUtil.convertAddressMapToPersonAddress(addressFieldToValueMap);
    }

    private AddressHierarchyLevel findAddressHierarchyLevel(String key, List<AddressHierarchyLevel> addressHierarchyLevels) {
        for (AddressHierarchyLevel addressHierarchyLevel : addressHierarchyLevels) {
            if (addressHierarchyLevel.getName().equals(key)) {
                return addressHierarchyLevel;
            }
        }
        throw new RuntimeException(String.format("Address Hierarchy level {0} does not exist.", key));
    }


}


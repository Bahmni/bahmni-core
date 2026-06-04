package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;


import org.openmrs.Concept;
import org.openmrs.Provider;
import org.openmrs.obs.ComplexData;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;

@Component
public class ProviderComplexDataMapper implements BahmniComplexDataMapper {
    @Override
    public Serializable map(ComplexData complexData) {
        HashMap<String, Object> locationData = new HashMap<>();

        Provider provider = (Provider) complexData.getData();
        locationData.put("dataType", "Provider");
        locationData.put("display", complexData.getTitle());

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", provider.getId());
        data.put("uuid", provider.getUuid());
        data.put("name", provider.getName());
        locationData.put("data", data);

        return locationData;
    }

    @Override
    public boolean canHandle(final Concept concept, ComplexData complexData) {
        if (complexData.getData() != null) {
            return complexData.getData() instanceof Provider;
        }
        return false;
    }
}

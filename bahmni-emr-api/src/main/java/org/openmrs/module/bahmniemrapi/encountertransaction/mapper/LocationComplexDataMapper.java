package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;


import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.obs.ComplexData;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;

@Component
public class LocationComplexDataMapper implements BahmniComplexDataMapper {
    @Override
    public Serializable map(ComplexData complexData) {
        HashMap<String, Object> locationData = new HashMap<>();

        Location location = (Location) complexData.getData();
        locationData.put("dataType", "Location");
        locationData.put("display", complexData.getTitle());

        HashMap<String, Object> data = new HashMap<>();
        data.put("id", location.getId());
        data.put("uuid", location.getUuid());
        data.put("name", location.getName());
        locationData.put("data", data);

        return locationData;
    }

    @Override
    public boolean canHandle(final Concept concept, ComplexData complexData) {
        if (complexData.getData() != null) {
            return complexData.getData() instanceof Location;
        }
        return false;
    }
}

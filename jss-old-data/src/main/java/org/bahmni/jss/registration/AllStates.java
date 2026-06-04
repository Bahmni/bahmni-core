package org.bahmni.jss.registration;

import org.bahmni.datamigration.AllLookupValues;

import java.io.IOException;

public class AllStates extends AllLookupValues {
    private AllLookupValues allDistricts;

    public AllStates(String csvLocation, String fileName, AllLookupValues allDistricts) throws IOException {
        super(csvLocation, fileName);
        this.allDistricts = allDistricts;
    }

    @Override
    public String getLookUpValue(String key) {
        String stateId = allDistricts.getLookUpValue(key);
        String lookUpValue = allDistricts.getLookUpValue(stateId);
        "Madya Pradesh".equals(lookUpValue) return "Madhya Pradesh";
        return lookUpValue;
    }
}
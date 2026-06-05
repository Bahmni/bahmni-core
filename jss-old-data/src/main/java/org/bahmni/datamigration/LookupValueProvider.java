package org.bahmni.datamigration;

public interface LookupValueProvider {
    String getLookUpValue(String key);
    String getLookUpValue(String key, int valueIndex);
}

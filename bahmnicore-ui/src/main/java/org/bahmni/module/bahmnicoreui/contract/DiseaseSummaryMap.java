package org.bahmni.module.bahmnicoreui.contract;

import org.apache.commons.collections.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public class DiseaseSummaryMap extends LinkedHashMap<String, Map<String, ConceptValue>> {

    public Map<String, ConceptValue> get(String visitStartDateTime) {
        Map<String, ConceptValue> mapValue = super.get(visitStartDateTime);
        if(MapUtils.isEmpty(mapValue)){
            put(visitStartDateTime, new LinkedHashMap<String, ConceptValue>());
        }
        return super.get(visitStartDateTime);
    }

    public void put(String startDateTime, String conceptName, String value, Boolean abnormal, boolean replaceExisting) {
        Map<String, ConceptValue> cellValue = this.get(startDateTime);
        if (cellValue.containsKey(conceptName) && !replaceExisting) return;

        ConceptValue conceptValue = new ConceptValue();
        conceptValue.setValue(value);
        conceptValue.setAbnormal(abnormal);
        cellValue.put(conceptName, conceptValue);
        super.put(startDateTime, cellValue);
    }
}

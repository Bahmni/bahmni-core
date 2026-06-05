package org.openmrs.module.bahmniemrapi.pivottable.contract;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PivotRow {
    private Map<String, ArrayList<BahmniObservation>> columns = new HashMap<>();

    public void addColumn(String name, BahmniObservation bahmniObservation) {
        ArrayList<BahmniObservation> bahmniObs;
        bahmniObs = columns.get(name) != null ? columns.get(name) : new ArrayList<BahmniObservation>();
        bahmniObs.add(bahmniObservation);
        columns.put(name, bahmniObs);
    }

    public ArrayList<BahmniObservation> getValue(String key) {
        return columns.get(key);
    }

    public Map<String, ArrayList<BahmniObservation>> getColumns() {
        return columns;
    }

}

package org.openmrs.module.bahmniemrapi.pivottable.contract;

import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class PivotTable {
    private Set<EncounterTransaction.Concept> headers = new LinkedHashSet<>();
    private List<PivotRow> rows = new ArrayList<>();

    public Set<EncounterTransaction.Concept> getHeaders() {
        return headers;
    }

    public void setHeaders(Set<EncounterTransaction.Concept> headers) {
        this.headers = headers;
    }

    public List<PivotRow> getRows() {
        return rows;
    }

    public void setRows(List<PivotRow> rows) {
        this.rows = rows;
    }

    public void addRow(int index, PivotRow row){
        this.rows.add(index,row);
    }
}
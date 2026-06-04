package org.bahmni.module.bahmnicoreui.contract;

import org.bahmni.module.referencedata.contract.ConceptDetails;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DiseaseSummaryData {

    private DiseaseSummaryMap tabularData = new DiseaseSummaryMap();
    private Set<ConceptDetails> conceptDetails = new LinkedHashSet<>();

    public DiseaseSummaryMap getTabularData() {
        return tabularData;
    }

    public void setTabularData(DiseaseSummaryMap tabularData) {
        this.tabularData = tabularData;
    }

    public void addTabularData(Map<String, Map<String, ConceptValue>> newTable){
        for (String visitDate : newTable.keySet()) {
            Map<String, ConceptValue> valuesForVisit = getValuesForVisit(visitDate);//tabularData.toString(visitDate);
            valuesForVisit.putAll(newTable.get(visitDate));
        }
    }

    private Map<String, ConceptValue> getValuesForVisit(String visitDate) {
        Map<String, ConceptValue> valuesForVisit = tabularData.get(visitDate);
        if( valuesForVisit == null){
            valuesForVisit = new LinkedHashMap<>();
            tabularData.put(visitDate,valuesForVisit);
        }
        return valuesForVisit;
    }

    public Set<ConceptDetails> getConceptDetails() {
        return conceptDetails;
    }

    public void addConceptDetails(Set<ConceptDetails> conceptDetails) {
        this.conceptDetails.addAll(conceptDetails);
    }

    public void concat(DiseaseSummaryData diseaseSummaryData){
        addTabularData(diseaseSummaryData.getTabularData());
        addConceptDetails(diseaseSummaryData.getConceptDetails());
    }
}

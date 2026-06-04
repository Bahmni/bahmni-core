package org.openmrs.module.bahmniemrapi.laborder.contract;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class LabOrderResults {
    private List<LabOrderResult> results = new ArrayList<>();
    private TabularLabOrderResults tabularResult;

    @JsonCreator
    public LabOrderResults(@JsonProperty("results")List<LabOrderResult> results) {
        this.results = results;
        this.tabularResult = this.tabulate();
    }

    private TabularLabOrderResults tabulate() {
        Map<LocalDate, TabularLabOrderResults.DateLabel> dateMap = new TreeMap<>();
        Map<String, TabularLabOrderResults.TestOrderLabel> orderMap = new TreeMap<>();
        List<TabularLabOrderResults.CoordinateValue> coordinateValues = new ArrayList<>();

        Integer dateLabelIndexCounter = 0;
        Integer testOrderLabelCounter = 0;

        for (LabOrderResult result : results) {
            LocalDate orderDate = new LocalDate(result.getAccessionDateTime());
            if(dateMap.get(orderDate) == null) {
                dateMap.put(orderDate, new TabularLabOrderResults.DateLabel(dateLabelIndexCounter++, orderDate.toString("dd-MMM-yyyy")));
            }
            if(orderMap.get(result.getTestName()) == null) {
                orderMap.put(result.getTestName(), new TabularLabOrderResults.TestOrderLabel(testOrderLabelCounter++, result.getTestName(), result.getMinNormal(), result.getMaxNormal(), result.getTestUnitOfMeasurement(), result.getPanelName()));
            }

            if(result.getResult() != null || result.getReferredOut() || result.getUploadedFileName() != null) {
                TabularLabOrderResults.CoordinateValue coordinateValue = new TabularLabOrderResults.CoordinateValue();
                coordinateValue.setDateIndex(dateMap.get(orderDate).getIndex());
                coordinateValue.setTestOrderIndex(orderMap.get(result.getTestName()).getIndex());
                coordinateValue.setResult(result.getResult());
                coordinateValue.setAbnormal(result.getAbnormal());
                coordinateValue.setReferredOut(result.getReferredOut());
                coordinateValue.setUploadedFileName(result.getUploadedFileName());
                coordinateValue.setAccessionDateTime(result.getAccessionDateTime());
                coordinateValues.add(coordinateValue);
            }
        }

        return new TabularLabOrderResults(new ArrayList<>(dateMap.values()), new ArrayList<>(orderMap.values()), coordinateValues);
    }

    public void setTabularResult(TabularLabOrderResults tabularResult) {
        this.tabularResult = tabularResult;
    }

    public List<LabOrderResult> getResults() {
        return results;
    }

    public void setResults(List<LabOrderResult> results) {
        this.results = results;
    }

    public TabularLabOrderResults getTabularResult() {
        return tabularResult;
    }
}

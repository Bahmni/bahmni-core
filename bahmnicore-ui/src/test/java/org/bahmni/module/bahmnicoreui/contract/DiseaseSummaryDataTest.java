package org.bahmni.module.bahmnicoreui.contract;

import org.bahmni.module.referencedata.contract.ConceptDetails;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.AbstractMap.SimpleEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DiseaseSummaryDataTest {

    @Test
    public void shouldAddTabularDataToExistingTabularData(){
        DiseaseSummaryData diseaseSummaryData = new DiseaseSummaryData();
        Map<String,Map<String, ConceptValue>> existingTabularData = new LinkedHashMap<>();
        existingTabularData.put("12-12-2012", createConceptValueMap(new SimpleEntry<>("Blood Pressure", "120/80"), new SimpleEntry<>("Temperature", "101")));
        existingTabularData.put("13-12-2012", createConceptValueMap(new SimpleEntry<>("pulse", "100"), new SimpleEntry<>("Temperature", "104")));
        diseaseSummaryData.addTabularData(existingTabularData);

        Map<String,Map<String, ConceptValue>> newTabularData = new LinkedHashMap<>();
        newTabularData.put("11-12-2012", createConceptValueMap(new SimpleEntry<>("Paracetamol", "500mg"), new SimpleEntry<>("cetrizine", "200mg")));
        newTabularData.put("13-12-2012", createConceptValueMap(new SimpleEntry<>("White blood cells", "100000"), new SimpleEntry<>("serum creatinine", "5")));

        diseaseSummaryData.addTabularData(newTabularData);

        Map<String, Map<String, ConceptValue>> tabularData = diseaseSummaryData.getTabularData();
        assertEquals(3, tabularData.size());
        assertEquals(4, tabularData.get("13-12-2012").size());

        assertEquals("500mg", tabularData.get("11-12-2012").get("Paracetamol").getValue());
        assertEquals("200mg", tabularData.get("11-12-2012").get("cetrizine").getValue());

        assertEquals("100000", tabularData.get("13-12-2012").get("White blood cells").getValue());
        assertEquals("5", tabularData.get("13-12-2012").get("serum creatinine").getValue());


    }

    @Test
    public void shouldAddConceptNamesToExistingSetOfConceptNames(){
        DiseaseSummaryData diseaseSummaryData = new DiseaseSummaryData();
        Set<ConceptDetails> existingConceptNames = new LinkedHashSet<>();
        ConceptDetails bloodConceptDetails = new ConceptDetails();
        bloodConceptDetails.setName("blood");
        ConceptDetails fluidConceptDetails = new ConceptDetails();
        fluidConceptDetails.setName("fluid");
        existingConceptNames.add(bloodConceptDetails);
        existingConceptNames.add(fluidConceptDetails);

        Set<ConceptDetails> newConceptNames = new LinkedHashSet<>();
        ConceptDetails temperatureConceptDetails = new ConceptDetails();
        temperatureConceptDetails.setName("temperature");
        newConceptNames.add(temperatureConceptDetails);
        diseaseSummaryData.addConceptDetails(existingConceptNames);
        diseaseSummaryData.addConceptDetails(newConceptNames);

        Set<ConceptDetails> conceptDetails = diseaseSummaryData.getConceptDetails();
        assertEquals(conceptDetails.size(), 3);
        assertTrue(conceptDetails.contains(temperatureConceptDetails));
    }

    private Map<String,ConceptValue> createConceptValueMap(Map.Entry<String,String>... values){
        Map<String,ConceptValue> conceptValuesForDate = new LinkedHashMap<>();
        for (Map.Entry<String,String> concept : values) {
            ConceptValue value = new ConceptValue();
            value.setValue(concept.getValue());
            conceptValuesForDate.put(concept.getKey(),value);
        }
        return conceptValuesForDate;
    }
}
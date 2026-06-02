/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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

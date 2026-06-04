package org.bahmni.module.bahmnicoreui.service;

import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;

public interface BahmniDiseaseSummaryService {

    DiseaseSummaryData getDiseaseSummary(String patientUuid, DiseaseDataParams queryParams);
}



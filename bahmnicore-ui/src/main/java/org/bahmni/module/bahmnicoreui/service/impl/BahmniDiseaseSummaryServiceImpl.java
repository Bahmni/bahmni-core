package org.bahmni.module.bahmnicoreui.service.impl;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryMap;
import org.bahmni.module.bahmnicoreui.helper.DrugOrderDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.helper.LabDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.helper.ObsDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.service.BahmniDiseaseSummaryService;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Service
public class BahmniDiseaseSummaryServiceImpl implements BahmniDiseaseSummaryService {

    private PatientService patientService;
    private LabDiseaseSummaryAggregator labDiseaseSummaryAggregator;
    private DrugOrderDiseaseSummaryAggregator drugOrderDiseaseSummaryAggregator;
    private ObsDiseaseSummaryAggregator obsDiseaseSummaryAggregator;

    @Autowired
    public BahmniDiseaseSummaryServiceImpl(PatientService patientService, LabDiseaseSummaryAggregator labDiseaseSummaryAggregator, DrugOrderDiseaseSummaryAggregator drugOrderDiseaseSummaryAggregator, ObsDiseaseSummaryAggregator obsDiseaseSummaryAggregator){
        this.patientService = patientService;
        this.labDiseaseSummaryAggregator = labDiseaseSummaryAggregator;
        this.drugOrderDiseaseSummaryAggregator = drugOrderDiseaseSummaryAggregator;
        this.obsDiseaseSummaryAggregator = obsDiseaseSummaryAggregator;
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseSummaryData getDiseaseSummary(String patientUuid, DiseaseDataParams queryParams) {
        DiseaseSummaryData diseaseSummaryData = new DiseaseSummaryData();

        Patient patient = patientService.getPatientByUuid(patientUuid);

        diseaseSummaryData.concat(obsDiseaseSummaryAggregator.aggregate(patient, queryParams));
        diseaseSummaryData.concat(labDiseaseSummaryAggregator.aggregate(patient, queryParams));
        diseaseSummaryData.concat(drugOrderDiseaseSummaryAggregator.aggregate(patient, queryParams));
        diseaseSummaryData.setTabularData(filterDataByCount(diseaseSummaryData.getTabularData(), queryParams.getInitialCount(), queryParams.getLatestCount()));
        return diseaseSummaryData;
    }

    private DiseaseSummaryMap filterDataByCount(DiseaseSummaryMap diseaseSummaryMap, Integer initialCount, Integer latestCount) {
        if(initialCount == null && latestCount == null) return filter(diseaseSummaryMap, 0, diseaseSummaryMap.size());
        DiseaseSummaryMap summaryMap = new DiseaseSummaryMap();
        summaryMap.putAll(filter(diseaseSummaryMap, 0, getIntegerValue(latestCount)));
        summaryMap.putAll(filter(diseaseSummaryMap, diseaseSummaryMap.size() - getIntegerValue(initialCount), diseaseSummaryMap.size()));

        return summaryMap;
    }

    private DiseaseSummaryMap filter(DiseaseSummaryMap diseaseSummaryMap, int fromIndex, int toIndex) {
        DiseaseSummaryMap summaryMap = new DiseaseSummaryMap();
        fromIndex = (fromIndex > diseaseSummaryMap.size() || fromIndex < 0) ? 0 : fromIndex;
        toIndex = (toIndex > diseaseSummaryMap.size()) ? diseaseSummaryMap.size() : toIndex;

        List<String> summaryMapKeys = sortByDate(diseaseSummaryMap.keySet());
        for(int index=fromIndex; index<toIndex; index++) {
            String visitStartDateTime = summaryMapKeys.get(index);
            summaryMap.put(visitStartDateTime, diseaseSummaryMap.get(visitStartDateTime));
        }
        return summaryMap;
    }

    private List<String> sortByDate(Set<String> dataSet) {
        List<String> sortedList = new ArrayList<>(dataSet);
        Collections.sort(sortedList, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return convertToDate(o2).compareTo(convertToDate(o1));
            }
        });
        return sortedList;
    }

    private Date convertToDate(String dateString) {
        Date startDate = null;
        try {
            startDate = DateUtils.parseDate(dateString, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return startDate;
    }

    private int getIntegerValue(Integer value) {
        if(value == null) return 0;
        return value;
    }
}

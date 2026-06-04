package org.bahmni.module.bahmnicoreui.service.impl;

import org.bahmni.module.bahmnicoreui.contract.ConceptValue;
import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryMap;
import org.bahmni.module.bahmnicoreui.helper.DrugOrderDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.helper.LabDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.helper.ObsDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.service.BahmniDiseaseSummaryService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniDiseaseSummaryServiceImplTest {
  BahmniDiseaseSummaryService bahmniDiseaseSummaryService;

  @Mock
  private PatientService patientServiceMock;
  @Mock
  private LabDiseaseSummaryAggregator labDiseaseSummaryAggregatorMock;
  @Mock
  private DrugOrderDiseaseSummaryAggregator drugOrderDiseaseSummaryAggregatorMock;
  @Mock
  private ObsDiseaseSummaryAggregator obsDiseaseSummaryAggregatorMock;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    bahmniDiseaseSummaryService = new BahmniDiseaseSummaryServiceImpl(patientServiceMock,
        labDiseaseSummaryAggregatorMock, drugOrderDiseaseSummaryAggregatorMock, obsDiseaseSummaryAggregatorMock);
  }

  @Test
  public void shouldSortDiseaseSummaryDataByVisitStartDate() throws Exception {
    String patientUuid = "somePatientUuid";
    Patient patient = new Patient();
    DiseaseDataParams diseaseDataParams = new DiseaseDataParams();

    DiseaseSummaryData obsDiseaseSummaryData = setupDiseaseSummaryData(new DiseaseSummaryData(),
        Arrays.asList("2016-07-05T13:13:25+05:30", "2016-07-04T13:13:25+05:30"));

    DiseaseSummaryData drugDiseaseSummaryData = setupDiseaseSummaryData(new DiseaseSummaryData(),
        Arrays.asList("2016-07-05T13:13:25+05:30", "2016-07-05T12:13:25+05:30"));

    DiseaseSummaryData labDiseaseSummaryData = setupDiseaseSummaryData(new DiseaseSummaryData(),
        Arrays.asList("2016-07-05T13:13:25+05:30", "2016-06-05T13:13:25+05:30", "2016-08-05T13:13:25+05:30"));

    when(patientServiceMock.getPatientByUuid(patientUuid)).thenReturn(patient);
    when(obsDiseaseSummaryAggregatorMock.aggregate(patient, diseaseDataParams)).thenReturn(obsDiseaseSummaryData);
    when(drugOrderDiseaseSummaryAggregatorMock.aggregate(patient, diseaseDataParams)).thenReturn(drugDiseaseSummaryData);
    when(labDiseaseSummaryAggregatorMock.aggregate(patient, diseaseDataParams)).thenReturn(labDiseaseSummaryData);

    DiseaseSummaryData actualDiseaseSummary = bahmniDiseaseSummaryService.getDiseaseSummary(patientUuid, diseaseDataParams);
    assertEquals(5, actualDiseaseSummary.getTabularData().size());
    List<String> actualOrderedVisitDates = getOrderedKeysFor(actualDiseaseSummary.getTabularData());
    List<String> expectedOrderedVisitDates = Arrays.asList("2016-08-05T13:13:25+05:30", "2016-07-05T13:13:25+05:30",
        "2016-07-05T12:13:25+05:30", "2016-07-04T13:13:25+05:30", "2016-06-05T13:13:25+05:30");
    assertEquals(expectedOrderedVisitDates, actualOrderedVisitDates);
  }

  private static List<String> getOrderedKeysFor(DiseaseSummaryMap diseaseSummaryMap) {
    List<String> keys = new ArrayList<>();
    for (Map.Entry<String, Map<String, ConceptValue>> t : diseaseSummaryMap.entrySet()) {
      keys.add(t.getKey());
    }
    return keys;
  }

  private DiseaseSummaryData setupDiseaseSummaryData(DiseaseSummaryData diseaseSummaryData, List<String> visitDates) {
    ConceptValue conceptValue = new ConceptValue();
    conceptValue.setValue("someConceptValue");

    LinkedHashMap<String, ConceptValue> conceptMap = new LinkedHashMap<>();
    conceptMap.put("someConceptKey", conceptValue);

    for (String visitDateString : visitDates) {
      LinkedHashMap<String, Map<String, ConceptValue>> visitDateToConceptMap = new LinkedHashMap<>();
      visitDateToConceptMap.put(visitDateString, conceptMap);

      diseaseSummaryData.addTabularData(visitDateToConceptMap);
    }
    return diseaseSummaryData;
  }
}
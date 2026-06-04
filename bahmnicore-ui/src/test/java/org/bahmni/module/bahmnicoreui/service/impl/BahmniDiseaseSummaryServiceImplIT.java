package org.bahmni.module.bahmnicoreui.service.impl;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.bahmni.module.bahmnicoreui.constant.DiseaseSummaryConstants;
import org.bahmni.module.bahmnicoreui.contract.ConceptValue;
import org.bahmni.module.bahmnicoreui.contract.DiseaseDataParams;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryData;
import org.bahmni.module.bahmnicoreui.helper.DrugOrderDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.helper.LabDiseaseSummaryAggregator;
import org.bahmni.module.bahmnicoreui.helper.ObsDiseaseSummaryAggregator;
import org.bahmni.module.referencedata.contract.ConceptDetails;
import org.junit.Test;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BahmniDiseaseSummaryServiceImplIT extends BaseModuleContextSensitiveTest {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DiseaseSummaryConstants.DATE_FORMAT);

    private BahmniDiseaseSummaryServiceImpl bahmniDiseaseSummaryData;
    @Autowired
    private PatientService patientService;

    @Autowired
    private ObsDiseaseSummaryAggregator obsDiseaseSummaryAggregator;
    @Autowired
    private LabDiseaseSummaryAggregator labDiseaseSummaryAggregator;
    @Autowired
    private DrugOrderDiseaseSummaryAggregator drugOrderDiseaseSummaryAggregator;

    @org.junit.Before
    public void setUp() throws Exception {
        bahmniDiseaseSummaryData = new BahmniDiseaseSummaryServiceImpl(patientService, labDiseaseSummaryAggregator, drugOrderDiseaseSummaryAggregator, obsDiseaseSummaryAggregator);
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
    }

    private void setUpObservationTestData() throws Exception {
        executeDataSet("observationsTestData.xml");
        updateSearchIndex();
    }

    private void setUpLabOrderTestData() throws Exception {
        executeDataSet("labOrderTestData.xml");
        updateSearchIndex();
    }

    private void setUpDrugOrderTestData() throws Exception {
        executeDataSet("drugOrderTestData.xml");
        updateSearchIndex();
    }

    @Test
    public void shouldReturnObsForGivenConceptsAndNoOfVisits() throws Exception {
        setUpObservationTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(1);
        ArrayList<String> obsConcepts = new ArrayList<String>() {{
            add("Weight");
            add("Blood Pressure");
        }};

        diseaseDataParams.setObsConcepts(obsConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("86526ed5-3c11-11de-a0ba-001e378eb67a", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummary.getTabularData();

        assertNotNull(obsTable);
        assertEquals(1, obsTable.size());

        Map<String, ConceptValue> obsInVisit = obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2008-09-18")));
        assertEquals(1, obsInVisit.size());
        assertEquals("120.0", obsInVisit.get("Weight").getValue());

    }

    @Test
    public void shouldReturnObsForGivenConceptsForAllVisitsWhenNoOfVisitsNotSpecifed() throws Exception {
        setUpObservationTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        ArrayList<String> obsConcepts = new ArrayList<String>() {{
            add("Blood Pressure");
            add("Weight");
        }};

        diseaseDataParams.setObsConcepts(obsConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("86526ed5-3c11-11de-a0ba-001e378eb67a", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummary.getTabularData();

        assertNotNull(obsTable);
        assertEquals(2, obsTable.size());

        Map<String, ConceptValue> obsForVisit = obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2008-09-18")));
        assertEquals(1, obsForVisit.size());
        assertEquals("120.0", obsForVisit.get("Weight").getValue());

        obsForVisit = obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2008-08-18")));
        assertEquals(2, obsForVisit.size());
        assertEquals("120.0", obsForVisit.get("Systolic Data").getValue());
        assertTrue(obsForVisit.get("Systolic Data").getAbnormal());
        assertEquals("40.0", obsForVisit.get("Diastolic Data").getValue());
        assertTrue(obsForVisit.get("Diastolic Data").getAbnormal());
    }

    @Test
    public void shouldReturnLabResultsForGivenConceptsAndNoOfVisits() throws Exception {
        setUpLabOrderTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(1);
        ArrayList<String> labConcepts = new ArrayList<String>() {{
            add("PS for Malaria");
        }};

        diseaseDataParams.setLabConcepts(labConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-0800271c1b75", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> labTable = diseaseSummary.getTabularData();

        assertNotNull(labTable);
        assertEquals(1, labTable.size());

        Map<String, ConceptValue> labResultsInVisit = labTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2013-09-26")));
        assertNotNull(labResultsInVisit);
        assertEquals(1, labResultsInVisit.size());
        assertEquals("new Result for PS Malaria", labResultsInVisit.get("PS for Malaria").getValue());

    }

    @Test
    public void shouldReturnLabResultsForGivenConceptsForAllVisits() throws Exception {
        setUpLabOrderTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        ArrayList<String> labConcepts = new ArrayList<String>() {{
            add("PS for Malaria");
        }};

        diseaseDataParams.setLabConcepts(labConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-0800271c1b75", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> labTable = diseaseSummary.getTabularData();

        assertNotNull(labTable);
        assertEquals(2, labTable.size());

        Map<String, ConceptValue> labResultsInVisit = labTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2013-09-26")));
        assertNotNull(labResultsInVisit);
        assertEquals(1, labResultsInVisit.size());
        assertEquals("new Result for PS Malaria", labResultsInVisit.get("PS for Malaria").getValue());

        labResultsInVisit = labTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2005-09-26")));
        assertNotNull(labResultsInVisit);
        assertEquals(1, labResultsInVisit.size());
        assertEquals("almost dead of PS Malaria", labResultsInVisit.get("PS for Malaria").getValue());
    }

    @Test
    public void shouldReturnDrugOrdersForGivenConceptsAndNoOfVisits() throws Exception {
        setUpDrugOrderTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(2);
        ArrayList<String> drugConcepts = new ArrayList<String>() {{
            add("Calpol 250mg");
        }};

        diseaseDataParams.setDrugConcepts(drugConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-080027175c1b", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> drugTable = diseaseSummary.getTabularData();

        assertNotNull(drugTable);
        assertEquals(1, drugTable.size());

        Map<String, ConceptValue> durgOrdersInVisit = drugTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2012-12-12")));
        assertNotNull(durgOrdersInVisit);
        assertEquals(1, durgOrdersInVisit.size());
        assertEquals("250mg,325.0,1/day x 7 days/week", durgOrdersInVisit.get("Calpol 250mg").getValue());
    }

    @Test
    public void shouldNotReturnVisitIfNoDrugsAreOrdered() throws Exception {
        setUpDrugOrderTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(1);
        ArrayList<String> drugConcepts = new ArrayList<String>() {{
            add("Calpol 250mg");
        }};

        diseaseDataParams.setDrugConcepts(drugConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-080027175c1b", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> drugTable = diseaseSummary.getTabularData();

        assertNotNull(drugTable);
        assertEquals(0, drugTable.size());

    }


    @Test
    public void shouldReturnDrugOrdersForGivenConceptsForAllVisits() throws Exception {
        setUpDrugOrderTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        ArrayList<String> drugConcepts = new ArrayList<String>() {{
            add("cetirizine 100mg");
            add("Calpol 250mg");
        }};

        diseaseDataParams.setDrugConcepts(drugConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-080027175c1b", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> drugTable = diseaseSummary.getTabularData();

        assertNotNull(drugTable);
        assertEquals(2, drugTable.size());

        Map<String, ConceptValue> durgOrdersInVisit = drugTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2001-09-22")));
        assertNotNull(durgOrdersInVisit);
        assertEquals(1, durgOrdersInVisit.size());
        assertEquals("250mg,125.0,1/day x 7 days/week", durgOrdersInVisit.get("Calpol 250mg").getValue());

    }

    @Test
    public void shouldReturnDrugOrdersForGivenConceptsForAllVisitsDependingOnTheInitialOrFinalCount() throws Exception {
        setUpDrugOrderTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setGroupBy("visits");
        diseaseDataParams.setInitialCount(1);
        ArrayList<String> drugConcepts = new ArrayList<String>() {{
            add("cetirizine 100mg");
            add("Calpol 250mg");
        }};

        diseaseDataParams.setDrugConcepts(drugConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-080027175c1b", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> drugTable = diseaseSummary.getTabularData();

        assertNotNull(drugTable);
        assertEquals(1, drugTable.size());

        Map<String, ConceptValue> durgOrdersInVisit = drugTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2001-09-22")));
        assertNotNull(durgOrdersInVisit);
        assertEquals(1, durgOrdersInVisit.size());
        assertEquals("250mg,125.0,1/day x 7 days/week", durgOrdersInVisit.get("Calpol 250mg").getValue());

    }

    @Test
    public void shouldReturnObsForGivenConceptsAndVisitUuid() throws Exception {
        setUpObservationTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        ArrayList<String> obsConcepts = new ArrayList<String>() {{
            add("Weight");
            add("Blood Pressure");
        }};

        diseaseDataParams.setObsConcepts(obsConcepts);
        diseaseDataParams.setVisitUuid("e10186d8-1c8e-11e4-bb80-f18addb6f9bb");
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("86526ed5-3c11-11de-a0ba-001e378eb67a", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummary.getTabularData();

        assertNotNull(obsTable);
        assertEquals(1, obsTable.size());

        Map<String, ConceptValue> obsForVisit = obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2008-09-18")));
        assertEquals(1, obsForVisit.size());
        assertEquals("120.0", obsForVisit.get("Weight").getValue());

    }

    @Test
    public void shouldReturnLabResultsForGivenConceptsAndVisitUuid() throws Exception {
        setUpLabOrderTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        ArrayList<String> labConcepts = new ArrayList<String>() {{
            add("PS for Malaria");
        }};

        diseaseDataParams.setLabConcepts(labConcepts);
        diseaseDataParams.setVisitUuid("9d705396-0c0c-11e4-bb80-f18addb6f9bb");
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-0800271c1b75", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> labTable = diseaseSummary.getTabularData();

        assertNotNull(labTable);
        assertEquals(1, labTable.size());

        Map<String, ConceptValue> labResultsInVisit = labTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2013-09-26")));
        assertNotNull(labResultsInVisit);
        assertEquals(1, labResultsInVisit.size());
        assertEquals("new Result for PS Malaria", labResultsInVisit.get("PS for Malaria").getValue());
    }

    @Test
    public void shouldReturnDrugOrdersForGivenConceptsAndVisitUuid() throws Exception {
        setUpDrugOrderTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        ArrayList<String> drugConcepts = new ArrayList<String>() {{
            add("cetirizine 100mg");
            add("Calpol 250mg");
        }};

        diseaseDataParams.setDrugConcepts(drugConcepts);
        diseaseDataParams.setVisitUuid("8244fcd2-f20f-11e3-b47b-c6959a4485cd");
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("75e04d42-3ca8-11e3-bf2b-080027175c1b", diseaseDataParams);
        Map<String, Map<String, ConceptValue>> drugTable = diseaseSummary.getTabularData();

        assertNotNull(drugTable);
        assertEquals(1, drugTable.size());

        Map<String, ConceptValue> drugOrdersInVisit = drugTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2001-09-22")));
        assertNotNull(drugOrdersInVisit);
        assertEquals(1, drugOrdersInVisit.size());
        assertEquals("250mg,125.0,1/day x 7 days/week", drugOrdersInVisit.get("Calpol 250mg").getValue());
    }

    @Test
    public void shouldReturnLeafConceptsNames() throws Exception {
        setUpObservationTestData();
        Context.getAuthenticatedUser().setUserProperty("defaultLocale","en");
        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(3);
        List<String> obsConcepts = new ArrayList<String>() {{
            add("Blood Pressure");
            add("Weight");
        }};
        diseaseDataParams.setObsConcepts(obsConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("86526ed5-3c11-11de-a0ba-001e378eb67a", diseaseDataParams);
        Set<ConceptDetails> conceptNames = diseaseSummary.getConceptDetails();


        assertNotNull(conceptNames);
        assertEquals(3, conceptNames.size());
        Iterator<ConceptDetails> conceptDetailsIterator = conceptNames.iterator();
        assertEquals("Systolic", conceptDetailsIterator.next().getName());
        assertEquals("Diastolic", conceptDetailsIterator.next().getName());
        assertEquals("Weight", conceptDetailsIterator.next().getName());
    }

    @Test
    public void shouldReturnShortNamesForCodedConceptObservations() throws Exception {
        setUpObservationTestData();

        DiseaseDataParams diseaseDataParams = new DiseaseDataParams();
        diseaseDataParams.setNumberOfVisits(3);
        ArrayList<String> obsConcepts = new ArrayList<String>() {{
            add("CodedConcept");
        }};
        Context.getAuthenticatedUser().setUserProperty("defaultLocale", "en");
        diseaseDataParams.setObsConcepts(obsConcepts);
        DiseaseSummaryData diseaseSummary = bahmniDiseaseSummaryData.getDiseaseSummary("86526ed5-3c11-11de-a0ba-001e378eb67a", diseaseDataParams);
        assertEquals("CCAnswer1", diseaseSummary.getTabularData().get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2008-09-18"))).get("CodedConcept").getValue());
    }

    private String frameDiseaseSummaryMapKey(Date date) {
        return DateFormatUtils.format(date, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
    }
}
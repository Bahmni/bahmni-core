package org.bahmni.module.bahmnicoreui.mapper;

import junit.framework.Assert;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bahmni.module.bahmnicoreui.constant.DiseaseSummaryConstants;
import org.bahmni.module.bahmnicoreui.contract.ConceptValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Drug;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.OrderFrequency;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions.FlexibleDosingInstructions;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class DiseaseSummaryMapperTest {

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DiseaseSummaryConstants.DATE_FORMAT);
    private SimpleDateFormat simpleDateTimeFormat = new SimpleDateFormat(DiseaseSummaryConstants.DATE_TIME_FORMAT);
    private String date1;
    private String date2;
    private String date3;
    private String visit1Encounter1Date;
    private String visit1Encounter2Date;
    private String visit1Encounter3Date;

    @Before
    public void setUp() throws Exception {
        date1 = "2014-09-12";
        date2 = "2014-09-13";
        date3 = "2014-09-14";
        visit1Encounter1Date = date1 +" 12:30";
        visit1Encounter2Date = date1 + " 05:30";
        visit1Encounter3Date = date1 +" 07:30";
        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
    }

    @Test
    public void shouldMapObservationsToResponseFormat() throws ParseException {

        DiseaseSummaryObsMapper diseaseSummaryObsMapper = new DiseaseSummaryObsMapper();
        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummaryObsMapper.map(createBahmniObsList(), DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_VISITS);
        assertNotNull(obsTable);
        assertEquals(3, obsTable.size());
        Map<String, ConceptValue> firstDayValue = obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse(date1)));
        assertEquals(2, firstDayValue.size());
        assertEquals("101", firstDayValue.get("temperature").getValue());
        assertEquals("90", firstDayValue.get("pulse").getValue());

        Map<String, ConceptValue> secondDayValue = obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse(date2)));
        assertEquals(1, secondDayValue.size());
        assertEquals("100", secondDayValue.get("pulse").getValue());

        Map<String, ConceptValue> thirdDayValue = obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse(date3)));
        assertEquals(1, thirdDayValue.size());
        assertEquals("120", thirdDayValue.get("bp").getValue());
    }

    @Test
    public void shouldMapObservationsAndGroupByEncounters() throws ParseException {
        DiseaseSummaryObsMapper diseaseSummaryObsMapper = new DiseaseSummaryObsMapper();
        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummaryObsMapper.map(createBahmniObsList(), DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_ENCOUNTER);
        assertNotNull(obsTable);
        assertEquals(5,obsTable.size());
        assertTrue(obsTable.containsKey(frameDiseaseSummaryMapKey(simpleDateTimeFormat.parse(visit1Encounter1Date))));

        Map<String, ConceptValue> visit1Encounter1Map = obsTable.get(frameDiseaseSummaryMapKey(simpleDateTimeFormat.parse(visit1Encounter1Date)));
        assertEquals(2, visit1Encounter1Map.size());
        assertEquals("101",visit1Encounter1Map.get("temperature").getValue());
        assertEquals("90",visit1Encounter1Map.get("pulse").getValue());

        Map<String, ConceptValue> visit1Encounter2Map = obsTable.get(frameDiseaseSummaryMapKey(simpleDateTimeFormat.parse(visit1Encounter2Date)));
        assertEquals(1, visit1Encounter2Map.size());
        assertEquals("102",visit1Encounter2Map.get("temperature").getValue());

        Map<String, ConceptValue> visit1Encounter3Map = obsTable.get(frameDiseaseSummaryMapKey(simpleDateTimeFormat.parse(visit1Encounter3Date)));
        assertEquals(1, visit1Encounter3Map.size());
        assertEquals("103",visit1Encounter3Map.get("temperature").getValue());

    }

    @Test
    public void shouldMapMultiselectObservations() throws ParseException {
        DiseaseSummaryObsMapper diseaseSummaryObsMapper = new DiseaseSummaryObsMapper();
        Collection<BahmniObservation> bahmniObsListWithMultiselectObs = createBahmniObsListWithMultiselectObs();
        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummaryObsMapper.map(bahmniObsListWithMultiselectObs, null);
        Assert.assertEquals("2-3days,5-6days", obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-09-12"))).get("m/c days").getValue());
        Assert.assertEquals("102", obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-09-12"))).get("temperature").getValue());
        Assert.assertEquals("90", obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-09-12"))).get("pulse").getValue());
        Assert.assertEquals("100", obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-09-13"))).get("pulse").getValue());

        Assert.assertEquals("Child_value2,Child_value1", obsTable.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-09-12"))).get("childobservation").getValue());

    }

    private Collection<BahmniObservation> createBahmniObsListWithMultiselectObs() throws ParseException {
        List<BahmniObservation> bahmniObservations =  new ArrayList<>();
        Date visit1 = simpleDateFormat.parse(date1);
        Date visit2 = simpleDateFormat.parse(date2);

        Date encounter1 = simpleDateTimeFormat.parse(visit1Encounter1Date);
        Date encounter2 = simpleDateTimeFormat.parse(visit1Encounter2Date);
        Date encounter3 = simpleDateTimeFormat.parse(visit1Encounter3Date);

        bahmniObservations.add(createBahmniObservation(visit1,encounter1, "M/C days","2-3days"));
        bahmniObservations.add(createBahmniObservation(visit1,encounter1, "M/C days","5-6days"));
        bahmniObservations.add(createBahmniObservation(visit1,encounter1, "Temperature","102"));
        bahmniObservations.add(createBahmniObservation(visit1,encounter1, "Pulse","90"));

        bahmniObservations.add(createBahmniObservation(visit1,encounter2, "Temperature","102"));
        bahmniObservations.add(createBahmniObservation(visit1,encounter3, "Temperature","103"));

        BahmniObservation bahmniObservationParent = createBahmniObservation(visit1, encounter3, "ParentObservation", "");
        final BahmniObservation bahmniObservationChild1 = createBahmniObservation(visit1,encounter3, "ChildObservation","Child_value1");
        final BahmniObservation bahmniObservationChild2 = createBahmniObservation(visit1,encounter3, "ChildObservation","Child_value2");
        bahmniObservationParent.setGroupMembers(new ArrayList<BahmniObservation>(){{
            add(bahmniObservationChild1);
            add(bahmniObservationChild2);
        }});
        bahmniObservations.add(bahmniObservationParent);

        bahmniObservations.add(createBahmniObservation(visit2,simpleDateTimeFormat.parse(date2 +" 12:30"),"Pulse","100"));
        return bahmniObservations;
    }

    @Test
    public void shouldMapCodedConceptValues() throws ParseException {
        DiseaseSummaryObsMapper diseaseSummaryObsMapper = new DiseaseSummaryObsMapper();
        List<BahmniObservation> bahmniObservations =  new ArrayList<>();

        Date visit1 = simpleDateFormat.parse(date1);
        bahmniObservations.add(createBahmniObservation(visit1,simpleDateTimeFormat.parse(date1 +" 12:30"),"Pulse",new EncounterTransaction.Concept("uuid-pulse","very high pulse")));

        Map<String, Map<String, ConceptValue>> obsTable = diseaseSummaryObsMapper.map(bahmniObservations, DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_VISITS);

        Map<String, ConceptValue> dayValue = obsTable.get(frameDiseaseSummaryMapKey(visit1));
        assertEquals(1, dayValue.size());
        assertEquals("very high pulse", dayValue.get("pulse").getValue());

    }

    @Test
    public void shouldMapDrugOrders() throws ParseException, IOException {
        DiseaseSummaryDrugOrderMapper diseaseSummaryDrugOrderMapper = new DiseaseSummaryDrugOrderMapper();
        Map<String, Map<String, ConceptValue>> drugOrderData = diseaseSummaryDrugOrderMapper.map(mockDrugOrders(new String[]{"paracetamol", "2014-08-15","2014-08-15 05:30"}, new String[]{"paracetamol1", "2014-08-15","2014-08-15 06:30"},new String[]{"penicillin", "2014-09-11","2014-09-11 06:30"}), DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_VISITS);

        assertNotNull(drugOrderData);
        assertEquals(2, drugOrderData.size());
        Map<String, ConceptValue> firstDayValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-08-15")));
        assertEquals(2, firstDayValue.size());
        assertEquals("paracetamol-500mg,10.0 mg,daily,SOS", firstDayValue.get("paracetamol").getValue());
        assertEquals("paracetamol1-500mg,10.0 mg,daily,SOS", firstDayValue.get("paracetamol1").getValue());

        Map<String, ConceptValue> secondDayValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-09-11")));
        assertEquals(1, secondDayValue.size());
        assertEquals("penicillin-500mg,10.0 mg,daily,SOS", secondDayValue.get("penicillin").getValue());
    }

    @Test
    public void shouldMapDrugOrdersForEncounters() throws ParseException, IOException {
        DiseaseSummaryDrugOrderMapper diseaseSummaryDrugOrderMapper = new DiseaseSummaryDrugOrderMapper();
        Map<String, Map<String, ConceptValue>> drugOrderData = diseaseSummaryDrugOrderMapper.map(mockDrugOrders(new String[]{"paracetamol", "2014-08-15","2014-08-15 05:30"}, new String[]{"paracetamol1", "2014-08-15","2014-08-15 06:30"},new String[]{"penicillin", "2014-09-11","2014-09-11 06:30"}), DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_ENCOUNTER);
        assertNotNull(drugOrderData);
        assertEquals(3, drugOrderData.size());
        Map<String, ConceptValue> firstEncounterValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateTimeFormat.parse("2014-08-15 05:30")));
        assertEquals(1, firstEncounterValue.size());
        assertEquals("paracetamol-500mg,10.0 mg,daily,SOS", firstEncounterValue.get("paracetamol").getValue());

        Map<String, ConceptValue> secondEncounterValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateTimeFormat.parse("2014-08-15 06:30")));
        assertEquals(1, secondEncounterValue.size());
        assertEquals("paracetamol1-500mg,10.0 mg,daily,SOS", secondEncounterValue.get("paracetamol1").getValue());

        Map<String, ConceptValue> thirdEncounterValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateTimeFormat.parse("2014-09-11 06:30")));
        assertEquals(1, thirdEncounterValue.size());
        assertEquals("penicillin-500mg,10.0 mg,daily,SOS", thirdEncounterValue.get("penicillin").getValue());
    }

    @Test
    public void shouldMapDrugOrdersWithFlexibleDosing() throws ParseException, IOException {
        DiseaseSummaryDrugOrderMapper diseaseSummaryDrugOrderMapper = new DiseaseSummaryDrugOrderMapper();
        Map<String, Map<String, ConceptValue>> drugOrderData = diseaseSummaryDrugOrderMapper.map(mockDrugOrdersWithFlexibleDosing(new String[]{"paracetamol", "2014-08-15", "2014-08-15 05:30"}, new String[]{"penicillin", "2014-09-11", "2014-09-11 05:30"}), DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_VISITS);

        assertNotNull(drugOrderData);
        assertEquals(2, drugOrderData.size());

        Map<String, ConceptValue> firstDayValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-08-15")));
        assertEquals(1, firstDayValue.size());
        assertEquals("paracetamol-500mg,10.0 mg,1-0-1,SOS", firstDayValue.get("paracetamol").getValue());

        Map<String, ConceptValue> secondDayValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-09-11")));
        assertEquals(1, secondDayValue.size());
        assertEquals("penicillin-500mg,10.0 mg,1-0-1,SOS", secondDayValue.get("penicillin").getValue());
    }

    @Test
    public void shouldMapDrugOrdersWithoutAnyExceptionsWhenThereIsNoData() throws ParseException, IOException {
        try{
            DiseaseSummaryDrugOrderMapper diseaseSummaryDrugOrderMapper = new DiseaseSummaryDrugOrderMapper();
            Map<String, Map<String, ConceptValue>> drugOrderData = diseaseSummaryDrugOrderMapper.map(mockDrugOrdersWithoutAnyData(new String[]{"paracetamol", "2014-08-15", "2014-08-15 05:30"}, new String[]{"penicillin", "2014-09-11", "2014-09-11 05:30"}), DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_VISITS);

            assertNotNull(drugOrderData);
            assertEquals(2, drugOrderData.size());

            Map<String, ConceptValue> firstDayValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-08-15")));
            assertEquals(1, firstDayValue.size());
            assertEquals("", firstDayValue.get("paracetamol").getValue());

            Map<String, ConceptValue> secondDayValue = drugOrderData.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-09-11")));
            assertEquals(1, secondDayValue.size());
            assertEquals("", secondDayValue.get("penicillin").getValue());
        }catch (Exception e){
            throw new RuntimeException("Should not throw any exception when drug orders dont have strength,dosage and freequency",e);
        }

    }

    private List<DrugOrder> mockDrugOrdersWithFlexibleDosing(String[]... drugInfoList) throws ParseException {
        List<DrugOrder> drugOrders = new ArrayList<>();
        for (String[] drugInfo : drugInfoList) {
            DrugOrder drugOrder = new DrugOrder();
            drugOrder.setConcept(createMRSConcept(drugInfo[0]));
            drugOrder.setEncounter(createEncounterWithVisitDateInfo(getDateFromString(drugInfo[1]), getDateTimeFromString(drugInfo[2])));
            drugOrder.setDrug(createDrugWithNameAndStrength(drugInfo[0], drugInfo[0] + "-500mg"));
            drugOrder.setDose(10.0);
            Concept doseUnits = new Concept();
            doseUnits.setFullySpecifiedName(new ConceptName("mg",Locale.getDefault()));
            drugOrder.setDoseUnits(doseUnits);
            drugOrder.setAsNeeded(true);
            drugOrder.setDosingInstructions("{\"instructions\":\"Before meals\",\"morningDose\":1,\"afternoonDose\":0,\"eveningDose\":1}");
            drugOrder.setDosingType(FlexibleDosingInstructions.class);
            drugOrders.add(drugOrder);
        }
        return drugOrders;
    }

    private List<DrugOrder> mockDrugOrdersWithoutAnyData(String[]... drugInfoList) throws ParseException {
        List<DrugOrder> drugOrders = new ArrayList<>();
        for (String[] drugInfo : drugInfoList) {
            DrugOrder drugOrder = new DrugOrder();
            drugOrder.setConcept(createMRSConcept(drugInfo[0]));
            drugOrder.setEncounter(createEncounterWithVisitDateInfo(getDateFromString(drugInfo[1]), getDateFromString(drugInfo[2])));
            drugOrder.setDrug(createDrugWithNameAndStrength(drugInfo[0], ""));
            drugOrders.add(drugOrder);
        }
        return drugOrders;
    }

    @Test
    public void shouldMapLabOrders() throws ParseException {
        DiseaseSummaryLabMapper diseaseSummaryLabMapper = new DiseaseSummaryLabMapper();
        Map<String, Map<String, ConceptValue>> labOrderData = diseaseSummaryLabMapper.map(mockLabOrders(), DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_VISITS);

        assertNotNull(labOrderData);
        assertEquals(2, labOrderData.size());

        Map<String, ConceptValue> firstDayValue = labOrderData.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-07-22")));
        assertEquals(1, firstDayValue.size());
        assertEquals("120", firstDayValue.get("Blood glucose").getValue());

        Map<String, ConceptValue> secondDayValue = labOrderData.get(frameDiseaseSummaryMapKey(simpleDateFormat.parse("2014-07-23")));
        assertEquals(2, secondDayValue.size());
        assertEquals("140", secondDayValue.get("Blood glucose").getValue());
        assertEquals("3.0", secondDayValue.get("serum creatinine").getValue());

    }

    private List<LabOrderResult> mockLabOrders() throws ParseException {
        List<LabOrderResult> labOrderResults = new ArrayList<>();
        labOrderResults.add(createLabOrder("2014-07-22","Blood glucose","120"));
        labOrderResults.add(createLabOrder("2014-07-23","Blood glucose","140"));
        labOrderResults.add(createLabOrder("2014-07-23","serum creatinine","3.0"));
        return labOrderResults;
    }

    private LabOrderResult createLabOrder(String visitDate, String conceptName, String value) throws ParseException {

        LabOrderResult labOrderResult = new LabOrderResult();
        Date date = simpleDateFormat.parse(visitDate);
        labOrderResult.setVisitStartTime(date);
        labOrderResult.setResult(value);
        labOrderResult.setTestName(conceptName);
        return labOrderResult;
    }


    private List<DrugOrder> mockDrugOrders(String[]... drugInfoList) throws ParseException {
        List<DrugOrder> drugOrders = new ArrayList<>();
        for (String[] drugInfo : drugInfoList) {
            DrugOrder drugOrder = new DrugOrder();
            drugOrder.setConcept(createMRSConcept(drugInfo[0]));
            drugOrder.setEncounter(createEncounterWithVisitDateInfo(getDateFromString(drugInfo[1]), getDateTimeFromString(drugInfo[2])));
            drugOrder.setDrug(createDrugWithNameAndStrength(drugInfo[0], drugInfo[0] + "-500mg"));
            drugOrder.setDose(10.0);
            Concept doseUnits = new Concept();
            doseUnits.setFullySpecifiedName(new ConceptName("mg",Locale.getDefault()));
            drugOrder.setDoseUnits(doseUnits);
            drugOrder.setAsNeeded(true);
            OrderFrequency frequency = new OrderFrequency();
            frequency.setConcept(createMRSConcept("daily"));
            drugOrder.setFrequency(frequency);
            drugOrders.add(drugOrder);
        }
        return drugOrders;
    }

    private Drug createDrugWithNameAndStrength(String drugName, String strength) {
        Drug drug = new Drug();
        drug.setName(drugName);
        drug.setStrength(strength);
        drug.setConcept(createMRSConcept(drugName));
        return drug;
    }

    private Encounter createEncounterWithVisitDateInfo(Date visitDate, Date encounterDate) {
        Encounter encounter = new Encounter();
        Visit visit = new Visit();
        visit.setStartDatetime(visitDate);
        encounter.setVisit(visit);
        encounter.setEncounterDatetime(encounterDate);

        return encounter;
    }

    private Date getDateFromString(String dateString) throws ParseException {
        return simpleDateFormat.parse(dateString);
    }

    private Date getDateTimeFromString(String dateString) throws ParseException {
        return simpleDateTimeFormat.parse(dateString);
    }

    private Concept createMRSConcept(String drugName) {
        Concept concept =  new Concept();
        concept.setFullySpecifiedName(new ConceptName(drugName, Locale.getDefault()));
        return concept;
    }


    private List<BahmniObservation> createBahmniObsList() throws ParseException {
        List<BahmniObservation> bahmniObservations =  new ArrayList<>();
        Date visit1 = simpleDateFormat.parse(date1);
        Date visit2 = simpleDateFormat.parse(date2);
        Date visit3 = simpleDateFormat.parse(date3);

        Date encounter1 = simpleDateTimeFormat.parse(visit1Encounter1Date);
        Date encounter2 = simpleDateTimeFormat.parse(visit1Encounter2Date);
        Date encounter3 = simpleDateTimeFormat.parse(visit1Encounter3Date);

        bahmniObservations.add(createBahmniObservation(visit1,encounter1, "Temperature","101"));
        bahmniObservations.add(createBahmniObservation(visit1,encounter1, "Pulse","90"));

        bahmniObservations.add(createBahmniObservation(visit1,encounter2, "Temperature","102"));
        bahmniObservations.add(createBahmniObservation(visit1,encounter3, "Temperature","103"));

        bahmniObservations.add(createBahmniObservation(visit2,simpleDateTimeFormat.parse(date2 +" 12:30"),"Pulse","100"));
        bahmniObservations.add(createBahmniObservation(visit3,simpleDateTimeFormat.parse(date3 +" 12:30"),"bp","120"));
        return bahmniObservations;
    }


    private BahmniObservation createBahmniObservation(Date visitStartTime, Date encounterDateTime, String conceptName, Object value) {
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setVisitStartDateTime(visitStartTime);
        bahmniObservation.setEncounterDateTime(encounterDateTime);
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept("uuid-"+conceptName,conceptName);
        concept.setShortName(conceptName.toLowerCase());
        bahmniObservation.setConcept(concept);
        bahmniObservation.setValue(value);
        bahmniObservation.setEncounterUuid("uuid-"+encounterDateTime);
        bahmniObservation.setUuid("uuid-obs-"+conceptName+Math.random());
        return bahmniObservation;
    }

    private String frameDiseaseSummaryMapKey(Date date) {
        return DateFormatUtils.format(date, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
    }
}
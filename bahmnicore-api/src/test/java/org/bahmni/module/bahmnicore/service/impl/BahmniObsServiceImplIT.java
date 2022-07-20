package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.obs.handler.LocationObsHandler;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.obs.ComplexObsHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class BahmniObsServiceImplIT extends BaseIntegrationTest {

    @Autowired
    BahmniObsService bahmniObsService;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private VisitService visitService;

    @Autowired
    ObsDao obsDao;

    private HashMap<String, String> metaDataSet = new HashMap<String, String>() {
        {
            put("diagnosis", "diagnosisMetadata.xml");
            put("disposition", "dispositionMetadata.xml");
            put("observation", "observationsTestData.xml");
            put("program", "patientProgramTestData.xml");
            put("complexObs", "complexObsData.xml");
        }
    };

    @Before
    public void setUp() throws Exception {
        setupMetaData(new String[] {"diagnosis", "disposition", "observation", "program" } );
    }

    private void setupMetaData(String[] list) throws Exception {
        for (String item : list) {
            String xmlFile = metaDataSet.get(item);
            if (!StringUtils.isBlank(xmlFile)) {
                executeDataSet(xmlFile);
            }
        }
    }

    @Test
    public void shouldGetComplexObsLocationData() throws Exception {
        setupMetaData(new String[] {"complexObs"});
        ObsService os = Context.getObsService();
        //TODO: this need to changed. os.getObs() should be called once the fix in core is in
        Obs complexObs = os.getComplexObs(44, ComplexObsHandler.RAW_VIEW);
        Assert.assertNotNull(complexObs);
        Assert.assertTrue(complexObs.isComplex());
        Assert.assertNotNull(complexObs.getValueComplex());
        Assert.assertNotNull(complexObs.getComplexData());
        Assert.assertEquals(Location.class, complexObs.getComplexData().getData().getClass());
        Assert.assertEquals(LocationObsHandler.class, os.getHandler(complexObs).getClass());
    }

    @Test
    public void shouldReturnLatestObsForEachConcept() {
        Concept vitalsConcept = conceptService.getConceptByName("Vitals");
        Collection<BahmniObservation> bahmniObservations = bahmniObsService.getLatest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                Arrays.asList(vitalsConcept), 3, null, false, null);
        BahmniObservation vitalObservation = bahmniObservations.iterator().next();
        Collection<BahmniObservation> vitalsGroupMembers = vitalObservation.getGroupMembers();
        assertEquals(2, vitalsGroupMembers.size());
        Iterator<BahmniObservation> observationIterator = vitalsGroupMembers.iterator();

        BahmniObservation weight = observationIterator.next();
        BahmniObservation pulse = observationIterator.next();
        assertEquals("Pulse", pulse.getConcept().getName());
        assertEquals("Weight", weight.getConcept().getName());
    }

    @Test
    public void shouldReturnLatestObsForEachConceptForSpecifiedNumberOfVisits() {
        Concept sittingConcept = conceptService.getConceptByName("Vitals");
        //Latest limited by last two visits.
        Collection<BahmniObservation> bahmniObservations = bahmniObsService.getLatest("86526ed5-3c11-11de-a0ba-001e378eb67a",
                Arrays.asList(sittingConcept), 2, null, false, null);
        assertEquals(0, bahmniObservations.size());
        bahmniObservations = bahmniObsService.getLatest("86526ed5-3c11-11de-a0ba-001e378eb67a", Arrays.asList(sittingConcept), 3, null, false, null);
        assertEquals(1, bahmniObservations.size());

        BahmniObservation sittingObservation = bahmniObservations.iterator().next();
        assertEquals("Vitals", sittingObservation.getConcept().getName());
    }

    @Test
    public void shouldReturnLatestObsForEachConceptForSpecifiedVisitUuid() {
        Concept sittingConcept = conceptService.getConceptByName("Sitting");
        Visit visit = visitService.getVisitByUuid("e10186d8-1c8e-11e4-bb80-f18add123456");

        Collection<BahmniObservation> latestObsByVisit = bahmniObsService.getLatestObsByVisit(visit, Arrays.asList(sittingConcept), null, false);
        assertEquals(1, latestObsByVisit.size());
        BahmniObservation sittingObservation = latestObsByVisit.iterator().next();
        assertEquals("1.5", sittingObservation.getValueAsString());
    }

    @Test
    public void shouldReturnEmptyListIfTheVisitDoesnotHaveData() throws Exception {
        Concept sittingConcept = conceptService.getConceptByName("Sitting");
        Visit visit = visitService.getVisitByUuid("e10186d8-1c8e-11e4-bb80-f1badd123456");

        Collection<BahmniObservation> latestObsByVisit = bahmniObsService.getLatestObsByVisit(visit, Arrays.asList(sittingConcept), null, false);

        assertEquals(0, latestObsByVisit.size());
    }

    @Test
    public void shouldReturnLatestObsFromAllEncountersInVisit() {
        Concept concept = conceptService.getConcept("100");
        Visit visit = visitService.getVisitByUuid("e10186d8-1c8e-11e4-bb80-f18add123456");
        Collection<BahmniObservation> latestObsByVisit = bahmniObsService.getLatestObsByVisit(visit, Arrays.asList(concept), null, false);

        assertEquals(1, latestObsByVisit.size());
        BahmniObservation obs = latestObsByVisit.iterator().next();
        assertEquals("100.0", obs.getValueAsString());

    }

    @Test
    public void returnOrphanedObsForPatient() throws Exception {
        Concept bloodPressureConcept = new ConceptBuilder().withName("Blood Pressure").build();
        Collection<BahmniObservation> obsForConceptSet = bahmniObsService.observationsFor("86526ed5-3c11-11de-a0ba-001e378eb67a",
                Arrays.asList(bloodPressureConcept), null, null, false, null, null, null);
        assertEquals(1, obsForConceptSet.size());
        Collection<BahmniObservation> bloodPressureMembers = obsForConceptSet.iterator().next().getGroupMembers();
        Iterator<BahmniObservation> bloodPressureMembersIterator = bloodPressureMembers.iterator();
        assertEquals(2, bloodPressureMembers.size());
        Collection<BahmniObservation> systolicMembers = bloodPressureMembersIterator.next().getGroupMembers();
        Collection<BahmniObservation> diastolicMembers = bloodPressureMembersIterator.next().getGroupMembers();
        assertEquals(2, systolicMembers.size());
        assertEquals(2, diastolicMembers.size());
    }

    @Test
    public void shouldReturnObsForAllConceptForGivenVisit() {
        List<BahmniObservation> bahmniObservations = (List<BahmniObservation>) bahmniObsService.getObservationForVisit("ad41fb41-a41a-4ad6-8835-2f59099acf5b", null, null, false, null);
        assertEquals(2, bahmniObservations.size());
        assertEquals(2, bahmniObservations.get(0).getGroupMembers().size());
        assertEquals(1, bahmniObservations.get(1).getGroupMembers().size());
    }

    @Test
    public void shouldReturnObsForGivenConceptForGivenVisitWithoutTakingObservationNamesCaseIntoAccount() {
        Collection<BahmniObservation> bahmniObservations =
                bahmniObsService.getObservationForVisit("ad41fb41-a41a-4ad6-8835-2f59099acf5b", Arrays.asList("SYSTOlic", "Diastolic"), null, false, null);
        assertEquals(2, bahmniObservations.size());
    }

    @Test
    public void shouldRetrieveObsForEncounter() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("Systolic");

        Collection<BahmniObservation> observations = bahmniObsService.getObservationsForEncounter("bb0af6767-707a-4629-9850-f15206e63ab0", conceptNames);

        assertEquals(1, observations.size());
        assertEquals("6d8f507a-fb89-11e3-bb80-f18addb6f9bd", observations.iterator().next().getUuid());
    }

    @Test
    public void shouldRetrieveObsForPatientProgram() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("conceptABC");

        Collection<BahmniObservation> observations = bahmniObsService.getObservationsForPatientProgram("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, null);

        assertEquals(1, observations.size());
        assertEquals("6d8f507a-fb899-11e3-bb80-996addb6f9we", observations.iterator().next().getUuid());

    }

    @Test
    public void shouldRetrieveEmptyObsListWhenPatientProgramUuidDoesNotExist() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("conceptABC");

        Collection<BahmniObservation> observations = bahmniObsService.getObservationsForPatientProgram("patientProgramUuid", conceptNames, null);

        assertEquals(0, observations.size());
    }

    @Test
    public void shouldRetrieveEmptyObsIfPatientProgramDoesNotHaveAnyEncounters() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("conceptABC");

        Collection<BahmniObservation> observations = bahmniObsService.getObservationsForPatientProgram("df0foifo-dkcd-475d-b939-6d82327f36a3", conceptNames, null);

        assertEquals(0, observations.size());
    }

    @Test
    public void shouldRetrieveBahmniObservationByObservationUuid() throws Exception {
        BahmniObservation bahmniObservation = bahmniObsService.getBahmniObservationByUuid("633dc076-1c8f-11e4-bkk0-f18addb6fmtb");

        assertNotNull("BahmniObservation should not be null", bahmniObservation);
        assertEquals("633dc076-1c8f-11e4-bkk0-f18addb6fmtb", bahmniObservation.getUuid());
    }

    @Test
    public void shouldRetrieveAllLatestObservationsForMultiSelectConcept() {
        List<BahmniObservation> observations = (List<BahmniObservation>) bahmniObsService.getLatestObservationsForPatientProgram("df0foif1-dkcd-475d-b939-6d82327f36a3", Arrays.asList("Systolic"), null);
        assertEquals(3, observations.size());
    }

    @Test
    public void shouldRetrieveAllLatestObservationSingleValueConcept() {
        List<BahmniObservation> observations = (List<BahmniObservation>) bahmniObsService.getLatestObservationsForPatientProgram("df0foif1-dkcd-475d-b939-6d82327f36a3", Arrays.asList("Diastolic"), null);
        assertEquals(1, observations.size());
        assertEquals(100.0, observations.get(0).getValue());
    }

    @Test
    public void shouldRetrieveRevisionBahmniObservationByObservationUuid() throws Exception {
        BahmniObservation bahmniObservation = bahmniObsService.getRevisedBahmniObservationByUuid("uuid99998");

        assertNotNull("BahmniObservation should not be null", bahmniObservation);
        assertEquals("uuid999982", bahmniObservation.getUuid());
    }

    @Test
    @Ignore("org.openmrs.api.UnchangeableObjectException: editing.fields.not.allowed") // This requires bean refactoring
    public void shouldNotRetrieveIgnoreObsAndItsChildrenForPatientProgram() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("Health Education");
        List<String> obsIgnoreList = new ArrayList<>();
        obsIgnoreList.add("HE, Marital status");

        Collection<BahmniObservation> observations = bahmniObsService.getObservationsForPatientProgram("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, obsIgnoreList);

        assertEquals(1, observations.size());
        assertEquals(1, observations.iterator().next().getGroupMembers().size());
        assertEquals("HE, Date of consultation", observations.iterator().next().getGroupMembers().iterator().next().getConceptNameToDisplay());
    }

    @Test
    @Ignore("org.openmrs.api.UnchangeableObjectException: editing.fields.not.allowed") // This requires bean refactoring
    public void shouldRetrieveAllObsIncludingChildrenForPatientProgram() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("Health Education");
        List<String> obsIgnoreList = new ArrayList<>();
        obsIgnoreList.add("HE, Date of consultation");

        Collection<BahmniObservation> observations = bahmniObsService.getObservationsForPatientProgram("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, obsIgnoreList);

        Collection<BahmniObservation> groupMembers = observations.iterator().next().getGroupMembers();
        Iterator<BahmniObservation> iterator = groupMembers.iterator();
        BahmniObservation observationOne = iterator.next();
        Collection<BahmniObservation> childMembers = observationOne.getGroupMembers();

        assertEquals(1, observations.size());
        assertEquals(1, groupMembers.size());
        assertEquals("HE, Marital status",observationOne.getConceptNameToDisplay());
        assertEquals(1, childMembers.size());
        assertEquals("HE, Date Of Marriage", childMembers.iterator().next().getConceptNameToDisplay());
    }

    @Test
    public void shouldReturnEmptyArrayIfConceptNameIsSameAsIgnoreListForPatientProgram() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("Health Education");
        List<String> obsIgnoreList = new ArrayList<>();
        obsIgnoreList.add("Health Education");

        Collection<BahmniObservation> observations = bahmniObsService.getObservationsForPatientProgram("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, obsIgnoreList);

        assertEquals(0, observations.size());
    }

    @Test
    public void shouldReturnAllObsInConceptNamesIfThereAreNoMatchesInObsIgnoreListForPatientProgram() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("HE, Date Of Marriage");
        List<String> obsIgnoreList = new ArrayList<>();
        obsIgnoreList.add("HE, Da Of Marriage");

        Collection<BahmniObservation> observations = bahmniObsService.getObservationsForPatientProgram("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, obsIgnoreList);

        assertEquals(1, observations.size());
    }

    @Test
    public void shouldRetrieveLatestObservationsNotInIgnoreListForMultiSelectConcept() {
        List<BahmniObservation> observations = (List<BahmniObservation>) bahmniObsService.getLatestObservationsForPatientProgram("df0foif1-dkcd-475d-b939-6d82327f36a3", Arrays.asList("Systolic"), Arrays.asList("Systolic"));
        assertEquals(0, observations.size());
    }

    @Test
    public void shouldRetrieveInitalObservationsNotInIgnoreListForPatientProgram() throws Exception {
        List<BahmniObservation> observations = (List<BahmniObservation>) bahmniObsService.getInitialObservationsForPatientProgram("df0foif1-dkcd-475d-b939-6d82327f36a3", Arrays.asList("Systolic"), Arrays.asList("Systolic"));
        assertEquals(0, observations.size());
    }

    @Test
    public void shouldRetrieveAllInitalObservationsForPatientProgram() throws Exception {
        List<BahmniObservation> observations = (List<BahmniObservation>) bahmniObsService.getInitialObservationsForPatientProgram("df0foif1-dkcd-475d-b939-6d82327f36a3", Arrays.asList("Systolic"), null);
        assertEquals(1, observations.size());
    }
}

package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ObsDaoImplIT extends BaseIntegrationTest {
    
    @Autowired
    ObsDao obsDao;

    @Autowired
    EncounterService encounterService;

    Map<Integer, Integer> conceptToObsMap = new HashMap<>();

    @Before
    public void setUp() throws Exception {
        executeDataSet("obsTestData.xml");
        executeDataSet("patientProgramTestData.xml");

        conceptToObsMap.put(9012, 5);
        conceptToObsMap.put(9011, 4);
    }

    @Test
    public void shouldGetLatestObsForConceptSetByVisit() {
        List<Obs> obsList = obsDao.getLatestObsForConceptSetByVisit("86526ed5-3c11-11de-a0ba-001e378eb67a", "Breast Cancer Intake", 901);
        assertEquals(2, obsList.size());
        for (Obs obs : obsList) {
            assertEquals("for concept : " + obs.getConcept().getName().getName(), conceptToObsMap.get(obs.getConcept().getId()), obs.getId());
        }
    }

    @Test
    public void shouldNotRetrieveIfObservationMadeInADifferentTemplate() {
        List<Obs> obsList = obsDao.getLatestObsForConceptSetByVisit("86526ed5-3c11-11de-a0ba-001e378eb67a", "Breast Cancer Progress", 901);
        assertEquals(2, obsList.size());
    }

    @Test
    public void shouldRetrieveObservationsForAnOrder() throws Exception {
        assertEquals(1, obsDao.getObsForOrder("5145792e-feb5-11e4-ae7f-080027b662ec").size());

        List<Obs> obsForOrder = obsDao.getObsForOrder("129de0a3-05c4-444a-be03-e01b4c4b2419");
        assertEquals(5, obsForOrder.size());
        assertEquals((Integer)12, obsForOrder.get(0).getId());
        assertEquals((Integer)11, obsForOrder.get(1).getId());
        assertEquals((Integer)10, obsForOrder.get(2).getId());
        assertEquals((Integer)8, obsForOrder.get(3).getId());
        assertEquals((Integer)7, obsForOrder.get(4).getId());

        assertEquals(0, obsDao.getObsForOrder("some-random-uuid").size());
    }

    @Test
    public void shouldRetrieveObservationWithinProgramsDateRange() throws Exception {
        String rootConceptName = "Breast Cancer Intake";
        String childConceptName = "Histopathology";
        String patientUUid = "86526ed5-3c11-11de-a0ba-001e378eb67a";
        Date startDate = BahmniDateUtil.convertToDate("2008-08-18T15:00:01.000", BahmniDateUtil.DateFormatType.UTC);
        Concept rootConcept = Context.getConceptService().getConceptByName(rootConceptName);
        Concept childConcept = Context.getConceptService().getConceptByName(childConceptName);
        List<Integer> listOfVisitIds = new ArrayList<Integer>();
        listOfVisitIds.add(902);
        rootConcept.getName().getName();

        List<Obs> bahmniObservations = obsDao.getObsFor(patientUUid, rootConcept, childConcept,listOfVisitIds, Collections.EMPTY_LIST, startDate, null);

        assertEquals(1, bahmniObservations.size());
        assertEquals(rootConceptName, bahmniObservations.get(0).getConcept().getName().getName());
        assertEquals(3, bahmniObservations.get(0).getGroupMembers(true).size());
    }


    @Test
    public void shouldFilterObservationsBasedOnEncounters() throws Exception {
        String rootConceptName = "Breast Cancer Intake";
        String childConceptName = "Histopathology";
        String patientUUid = "86526ed5-3c11-11de-a0ba-001e378eb67a";
        Date startDate = BahmniDateUtil.convertToDate("2008-08-18T15:00:01.000", BahmniDateUtil.DateFormatType.UTC);
        Concept rootConcept = Context.getConceptService().getConceptByName(rootConceptName);
        Concept childConcept = Context.getConceptService().getConceptByName(childConceptName);
        List<Integer> listOfVisitIds = new ArrayList<Integer>();
        listOfVisitIds.add(902);
        rootConcept.getName().getName();
        Encounter anEncounter = encounterService.getEncounter(40);
        Encounter anotherEncounter = encounterService.getEncounter(41);
        Encounter unrelatedEncounter = encounterService.getEncounter(3);

        List<Obs> bahmniObservations = obsDao.getObsFor(patientUUid, rootConcept, childConcept,listOfVisitIds, Arrays.asList(anEncounter, anotherEncounter), startDate, null);

        assertEquals(1, bahmniObservations.size());
        assertEquals(rootConceptName, bahmniObservations.get(0).getConcept().getName().getName());
        assertEquals(3, bahmniObservations.get(0).getGroupMembers(true).size());

        assertEquals(0, obsDao.getObsFor(patientUUid, rootConcept, childConcept, listOfVisitIds, Arrays.asList(unrelatedEncounter), startDate, null).size());
    }

    @Test
    public void shouldRetrieveObservationWithinEncounter() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("Breast Cancer Progress");
        List<Obs> observations = obsDao.getObsForConceptsByEncounter("f8ee38f6-1c8e-11e4-bb80-f18addb6f9bb", conceptNames);

        assertEquals(1, observations.size());
        assertEquals("6d8f507a-fb89-11e3-bb80-f18addb6f909", observations.get(0).getUuid());
    }


    @Test
    public void shouldRetrieveChildObservationFromParentGroup() throws Exception {
        Concept vitalsConcept = Context.getConceptService().getConceptByName("Histopathology");
        Obs observation = obsDao.getChildObsFromParent("7d8f507a-fb89-11e3-bb80-f18addb6f9a4", vitalsConcept);

        assertEquals((Integer)24, observation.getId());
    }

    @Test
    public void shouldRetrieveObsFromPatientProgramIdAndConceptNames() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("conceptABC");

        List<Obs> observations = obsDao.getObsByPatientProgramUuidAndConceptNames("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, null, null, null, null);

        assertEquals(1, observations.size());
    }

    @Test
    public void shouldRetrieveLatestObsFromPatientProgramIdAndConceptNamesOrderByObsDateTime() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("DiagnosisProgram");

        List<Obs> observations = obsDao.getObsByPatientProgramUuidAndConceptNames("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, 1, ObsDaoImpl.OrderBy.DESC, null, null);

        assertEquals(1, observations.size());
        assertEquals("2016-08-18 15:09:05.0", observations.get(0).getObsDatetime().toString());
    }

    @Test
    public void shouldRetrieveObsFromPatientProgramIdAndConceptNamesInDescendingOrderByObsDateTime() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("DiagnosisProgram");

        List<Obs> observations = obsDao.getObsByPatientProgramUuidAndConceptNames("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, -1, ObsDaoImpl.OrderBy.DESC, null, null);

        assertEquals(2, observations.size());
        assertEquals("2016-08-18 15:09:05.0", observations.get(0).getObsDatetime().toString());
        assertEquals("2015-08-18 15:09:05.0", observations.get(1).getObsDatetime().toString());
    }

    @Test
    public void shouldRetrieveObsFromPatientProgramIdAndConceptNamesInAscendingOrderByObsDateTime() throws Exception {
        ArrayList<String> conceptNames = new ArrayList<>();
        conceptNames.add("DiagnosisProgram");

        List<Obs> observations = obsDao.getObsByPatientProgramUuidAndConceptNames("dfdfoifo-dkcd-475d-b939-6d82327f36a3", conceptNames, -1, ObsDaoImpl.OrderBy.ASC, null, null);

        assertEquals(2, observations.size());
        assertEquals("2015-08-18 15:09:05.0", observations.get(0).getObsDatetime().toString());
        assertEquals("2016-08-18 15:09:05.0", observations.get(1).getObsDatetime().toString());
    }
}
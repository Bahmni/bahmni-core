package org.openmrs.module.bahmniemrapi.document.service.impl;

import org.bahmni.module.obsrelationship.api.ObsRelationService;
import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.BaseIntegrationTest;
import org.openmrs.module.bahmniemrapi.document.contract.Document;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentRequest;
import org.openmrs.module.bahmniemrapi.document.service.VisitDocumentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VisitDocumentServiceImplIT extends BaseIntegrationTest {

    public static final String FIRST_LOCATION_UUID = "8d6c993e-c2cc-11de-8d13-0040c6dffd0f";
    private static final String patientUUID = "86526ed5-3c11-11de-a0ba-001e378eb67a";
    private final String firstVisitUuid = "ad41fb41-a41a-4ad6-8835-2f59099acf5a";
    private final String secondVisitUuid = "d794516f-210d-4c4e-8978-467d97969f31";
    private final String visitTypeUUID = "f01c54cb-2225-471a-9cd5-d348552c337c";
    private final String firstEncounterTypeUUID = "759799ab-c9a5-435e-b671-77773ada74e4";
    private final String secondEncounterTypeUUID = "4ee21921-01cc-4720-a6bf-a61a17c4d05b";
    private final String firstProviderUuid = "331c6bf8-7846-11e3-a96a-0800271c1b75";
    private final String secondProviderUuid = "331c6bf8-7846-11e3-a96a-0800271c1333";
    private final String secondLocationUuid = "899c993e-c2cc-11de-8d13-0040c6dffd0f";
    private final String firstEncounterUuid = "6d0ae386-707a-4629-9850-f15206e63ab0";
    private final String secondEncounterUuid = "6d0ae386-707a-4629-9850-f15206e63222";
    private final String conceptUuid = "4f596de5-5caa-11e3-a4c0-0800271c1b75";

    @Autowired
    private VisitDocumentService visitDocumentService;
    @Autowired
    private EncounterService encounterService;
    @Autowired
    private ObsService obsService;
    @Autowired
    private VisitService visitService;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private ObsRelationService obsRelationService;

    private VisitDocumentRequest visitDocumentRequest;

    @Before
    public void setUp() throws Exception {
        executeDataSet("visitDocumentData.xml");
    }

    @Test
    public void shouldDeleteObservationsOfPreviousEncounters() throws ParseException {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");

        List<Document> documents = new ArrayList<>();
        String testUuid = "3f596de5-5caa-11e3-a4c0-0800271c1b75";
        String obsUuid = "6d0ae386-707a-4629-9850-f15206e63j8s";
        String providerUuid = "331c6bf8-7846-11e3-a96a-0800271c1b75";
        documents.add(new Document("/patient_file", null, testUuid, obsUuid, encounterDate, true));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                firstEncounterTypeUUID,
                encounterDate,
                documents,
                providerUuid, null, null);
        visitDocumentService.upload(visitDocumentRequest);

        Context.flushSession();
        Context.clearSession();

        Encounter encounter = encounterService.getEncounterByUuid(firstEncounterUuid);
        for (Obs obs : encounter.getAllObs(true)) {
            if (obs.getUuid().equals(obsUuid)) {
                assertThat(obs.getVoided(), is(true));
                assertThat(obs.getGroupMembers(true).iterator().next().getVoided(), is(true));
            }
        }
    }

    @Test
    public void shouldNotChangeObservationsIfSameDetailsProvidedOnceAgain() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/foo.jpg", null, conceptUuid, "6d0ae386-707a-4629-9850-f15206e63kj0", encounterDate, false));


        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                firstVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                documents,
                secondProviderUuid, secondLocationUuid, null);
        visitDocumentService.upload(visitDocumentRequest);

        Context.flushSession();
        Context.clearSession();

        Encounter encounter = encounterService.getEncounterByUuid(secondEncounterUuid);

        Obs savedDoc = getSavedDocument(encounter.getAllObs(), conceptUuid);

        assertNotNull(savedDoc);
        Set<Obs> groupMembers = savedDoc.getGroupMembers();
        assertThat(groupMembers.size(), is(equalTo(1)));
        assertThat(groupMembers.iterator().next().getValueText(), is("/radiology/foo.jpg"));
        assertThat(groupMembers.iterator().next().getUuid(), is("6d0ae386-707a-4629-9850-f15606e63666"));
    }

    @Test
    public void shouldPreferVoidOverUpdateWhenEditingADocument() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");

        List<Document> documents = new ArrayList<>();
        String obsUuid = "6d0ae386-707a-4629-9850-f15206e63kj0";
        documents.add(new Document("/radiology/foo.jpg", null, "3f596de5-5caa-11e3-a4c0-0800271c1b75", obsUuid, encounterDate, true));


        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                firstVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                documents,
                secondProviderUuid, secondLocationUuid,null);
        visitDocumentService.upload(visitDocumentRequest);

        Context.flushSession();
        Context.clearSession();

        Encounter encounter = encounterService.getEncounterByUuid(secondEncounterUuid);

        Obs savedObs = obsService.getObsByUuid(obsUuid);
        assertTrue("Observation is not voided", savedObs.getVoided());
        assertTrue("Observation is not voided", savedObs.getGroupMembers(true).iterator().next().getVoided());


        Obs savedDoc = getSavedDocument(encounter.getAllObs(), "3f596de5-5caa-11e3-a4c0-0800271c1b75");

        assertNull(savedDoc);
    }

    @Test
    public void shouldChangeObservationsOfPreviousEncounters() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/foo.jpg", null, "5f596de5-5caa-11e3-a4c0-0800271c1b75",
                "6d0ae386-707a-4629-9850-f15206e63kj0", encounterDate, false));


        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                firstVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                documents,
                secondProviderUuid, secondLocationUuid, null);
        executeDataSet("visitDocumentData.xml");
        visitDocumentService.upload(visitDocumentRequest);

        Context.flushSession();
        Context.clearSession();

        Encounter encounter = encounterService.getEncounterByUuid(secondEncounterUuid);
        for (Obs obs : encounter.getAllObs(true)) {
            if (obs.getUuid().equals("6d0ae386-707a-4629-9850-f15606e63666") ||
                    obs.getUuid().equals("6d0ae386-707a-4629-9850-f15206e63kj0")) {
                assertThat(obs.getVoided(), is(true));
            }
        }

        Obs savedDoc = getSavedDocument(encounter.getAllObs(), "5f596de5-5caa-11e3-a4c0-0800271c1b75");

        assertNotNull(savedDoc);
        assertThat(savedDoc.getConcept().getId(), is(333));
        assertThat(savedDoc.getGroupMembers().iterator().next().getValueText(), is("/radiology/foo.jpg"));
        assertEquals(FIRST_LOCATION_UUID, encounter.getLocation().getUuid());
    }

    @Test
    public void shouldCreateObservations() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");
        Date obsDate = getDateFromString("2014-06-24 00:10:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/fooo-bar.jpg", null, conceptUuid, null, obsDate, false));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                firstEncounterTypeUUID,
                encounterDate,
                documents,
                firstProviderUuid, FIRST_LOCATION_UUID, null);

        visitDocumentService.upload(visitDocumentRequest);

        Context.flushSession();
        Context.clearSession();

        Encounter encounter = encounterService.getEncounterByUuid(firstEncounterUuid);

        Obs savedDoc = getSavedDocument(encounter.getAllObs(), conceptUuid);

        assertNotNull(savedDoc);
        assertThat(savedDoc.getConcept().getId(), is(222));
        assertThat(savedDoc.getGroupMembers().iterator().next().getValueText(), is("/radiology/fooo-bar.jpg"));
        assertEquals(FIRST_LOCATION_UUID, encounter.getLocation().getUuid());
    }

    @Test
    public void shouldUploadImagesInOrderOfRequest() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");
        Date obsDate = getDateFromString("2014-06-24 00:10:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/1.jpg", null, conceptUuid, null, obsDate, false));
        documents.add(new Document("/radiology/2.jpg", null, conceptUuid, null, obsDate, false));
        documents.add(new Document("/radiology/3.jpg", null, conceptUuid, null, obsDate, false));
        documents.add(new Document("/radiology/4.jpg", null, conceptUuid, null, obsDate, false));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                firstEncounterTypeUUID,
                encounterDate,
                documents,
                firstProviderUuid, FIRST_LOCATION_UUID, null);
        visitDocumentService.upload(visitDocumentRequest);

        Context.flushSession();
        Context.clearSession();

        Encounter encounter = encounterService.getEncounterByUuid(firstEncounterUuid);
        List<Obs> savedDocuments = getSavedDocuments(encounter.getAllObs(), conceptUuid);
        assertEquals(4, savedDocuments.size());
        assertEquals("/radiology/1.jpg", getImageName(savedDocuments.get(0)));
        assertEquals("/radiology/2.jpg", getImageName(savedDocuments.get(1)));
        assertEquals("/radiology/3.jpg", getImageName(savedDocuments.get(2)));
        assertEquals("/radiology/4.jpg", getImageName(savedDocuments.get(3)));
    }

    private String getImageName(Obs obs) {
        Set<Obs> groupMembers = obs.getGroupMembers();
        return groupMembers.iterator().hasNext()?groupMembers.iterator().next().getValueText():null;
    }

    @Test
    public void shouldUseVisitStartTimeAsEncounterDateTimeForPreviousVisits() throws Exception {
        Date visitStartDate = getDateFromString("2010-09-22 00:00:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/foo-lalala.jpg", null, "3f596de5-5caa-11e3-a4c0-0800271c1b75", null, null, false));


        VisitDocumentRequest visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                firstVisitUuid,
                visitTypeUUID,
                null,
                null,
                firstEncounterTypeUUID,
                null,
                documents,
                secondProviderUuid, null, null);
        executeDataSet("visitDocumentData.xml");

//        Date currentDate = new Date(System.currentTimeMillis() - 1000);
        visitDocumentService.upload(visitDocumentRequest);
        Context.flushSession();
        Context.clearSession();

        Visit visit = visitService.getVisit(1);
        Set<Encounter> encounters = visit.getEncounters();
        Encounter encounter = getEncounterByTypeUuid(encounters, firstEncounterTypeUUID);
        boolean condition = encounter.getEncounterDatetime().compareTo(visitStartDate) >= 0;
        assertTrue(condition);

        Set<Obs> allObs = encounter.getAllObs();
        Obs savedDocument = getSavedDocument(allObs, "3f596de5-5caa-11e3-a4c0-0800271c1b75");
        assertTrue(savedDocument.getObsDatetime().compareTo(visitStartDate) == 0);
    }

    @Test
    public void shouldUseNewDateAsEncounterDateTimeForActiveVisits() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");
        Date obsDate = getDateFromString("2014-06-24 00:10:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/fooo-bar.jpg", null, conceptUuid, null, obsDate, false));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                documents,
                firstProviderUuid, null, null);

        Date currentDate = new Date(System.currentTimeMillis() - 1000);
        visitDocumentService.upload(visitDocumentRequest);
        Context.flushSession();
        Context.clearSession();

        Visit visit = visitService.getVisit(2);
        Set<Encounter> encounters = visit.getEncounters();
        Encounter encounter = getEncounterByTypeUuid(encounters, secondEncounterTypeUUID);
        boolean condition = encounter.getEncounterDatetime().compareTo(currentDate) >= 0;
        assertTrue(condition);

        Set<Obs> allObs = encounter.getAllObs();
        Obs savedDocument = getSavedDocument(allObs, conceptUuid);
        assertTrue(savedDocument.getObsDatetime().compareTo(currentDate) >= 0);
    }

    @Test
    public void shouldAddCommentsToObservationIfDocumentContainsComments() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");
        Date obsDate = getDateFromString("2014-06-24 00:10:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/fooo-bar.jpg", null, conceptUuid, null, obsDate, false, "something went wrong"));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                documents,
                firstProviderUuid, null, null);

        Date currentDate = new Date(System.currentTimeMillis() - 1000);
        visitDocumentService.upload(visitDocumentRequest);
        Visit visit = visitService.getVisit(2);
        Set<Encounter> encounters = visit.getEncounters();
        Encounter encounter = getEncounterByTypeUuid(encounters, secondEncounterTypeUUID);
        boolean condition = encounter.getEncounterDatetime().compareTo(currentDate) >= 0;
        assertTrue(condition);

        Set<Obs> allObs = encounter.getAllObs();
        Obs savedDocument = getSavedDocument(allObs, conceptUuid);
        assertEquals("something went wrong", savedDocument.getGroupMembers().iterator().next().getComment());
    }

    @Test
    public void shouldUpdateTheCommentsOnTheObservationIfDocumentContainsCommentsAndSavedObservationDoesntHaveComments() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");
        Date obsDate = getDateFromString("2014-06-24 00:10:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/fooo-bar.jpg", null, conceptUuid, null, obsDate, false));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                documents,
                firstProviderUuid, null, null);

        visitDocumentService.upload(visitDocumentRequest);
        Visit visit = visitService.getVisit(2);
        Set<Encounter> encounters = visit.getEncounters();
        Encounter encounter = getEncounterByTypeUuid(encounters, secondEncounterTypeUUID);
        Set<Obs> allObs = encounter.getAllObs();
        Obs savedDocument = getSavedDocument(allObs, conceptUuid);
        String obsUuid = savedDocument.getUuid();


        List<Document> modifiedDocuments = new ArrayList<>();
        modifiedDocuments.add(new Document("/radiology/fooo-bar.jpg", null, conceptUuid, obsUuid, obsDate, false, "comment on second save"));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                modifiedDocuments,
                firstProviderUuid, null, null);

        visitDocumentService.upload(visitDocumentRequest);
        Visit finalVisit = visitService.getVisit(2);
        Set<Encounter> finalVisitEncounters = finalVisit.getEncounters();
        Encounter finalEncounter = getEncounterByTypeUuid(finalVisitEncounters, secondEncounterTypeUUID);

        Set<Obs> finalAllObs = finalEncounter.getAllObs();
        Obs finalSavedDocument = getSavedDocument(finalAllObs, conceptUuid);
        assertEquals("comment on second save", finalSavedDocument.getGroupMembers().iterator().next().getComment());
    }

    @Test
    public void shouldUpdateTheCommentsAndObsRelationshipOnTheObservationIfDocumentContainsCommentsAndSavedObservationDoesntHaveComments() throws Exception {
        Date visitStartDate = getDateFromString("2014-06-22 00:00:00");
        Date encounterDate = getDateFromString("2014-06-23 00:00:00");
        Date obsDate = getDateFromString("2014-06-24 00:10:00");

        List<Document> documents = new ArrayList<>();
        documents.add(new Document("/radiology/fooo-bar.jpg", null, conceptUuid, null, obsDate, false));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                documents,
                firstProviderUuid, null, null);

        visitDocumentService.upload(visitDocumentRequest);
        Visit visit = visitService.getVisit(2);
        Set<Encounter> encounters = visit.getEncounters();
        Encounter encounter = getEncounterByTypeUuid(encounters, secondEncounterTypeUUID);
        Set<Obs> allObs = encounter.getAllObs();
        Obs savedDocument = getSavedDocument(allObs, conceptUuid);
        String obsUuid = savedDocument.getUuid();

        Obs impressionObs = obsService.getObs(1031);
        ObsRelationshipType obsRelationshipType = obsRelationService.getRelationshipTypeByName("qualified-by");
        ObsRelationship obsRelationship = new ObsRelationship();
        obsRelationship.setObsRelationshipType(obsRelationshipType);
        obsRelationship.setTargetObs(savedDocument.getGroupMembers().iterator().next());
        obsRelationship.setSourceObs(impressionObs);
        ObsRelationship savedObsRelation = obsRelationService.saveOrUpdate(obsRelationship);

        assertEquals(1031, savedObsRelation.getSourceObs().getObsId().intValue());

        assertEquals(savedDocument.getGroupMembers().iterator().next().getId(), savedObsRelation.getTargetObs().getObsId());
        List<Document> modifiedDocuments = new ArrayList<>();
        modifiedDocuments.add(new Document("/radiology/fooo-bar.jpg", null, conceptUuid, obsUuid, obsDate, false, "comment on second save"));

        visitDocumentRequest = new VisitDocumentRequest(patientUUID,
                secondVisitUuid,
                visitTypeUUID,
                visitStartDate,
                null,
                secondEncounterTypeUUID,
                encounterDate,
                modifiedDocuments,
                firstProviderUuid, null, null);

        visitDocumentService.upload(visitDocumentRequest);

        Visit finalVisit = visitService.getVisit(2);
        Set<Encounter> finalVisitEncounters = finalVisit.getEncounters();
        Encounter finalEncounter = getEncounterByTypeUuid(finalVisitEncounters, secondEncounterTypeUUID);

        Set<Obs> finalAllObs = finalEncounter.getAllObs();
        Obs finalSavedDocument = getSavedDocument(finalAllObs, conceptUuid);
        assertEquals("comment on second save", finalSavedDocument.getGroupMembers().iterator().next().getComment());

        ObsRelationship editedObsRelation = obsRelationService.getRelationByUuid(savedObsRelation.getUuid());

        assertEquals(1031, editedObsRelation.getSourceObs().getObsId().intValue());
        assertEquals(finalSavedDocument.getGroupMembers().iterator().next().getId(), editedObsRelation.getTargetObs().getId());
    }

    private Encounter getEncounterByTypeUuid(Set<Encounter> encounters, String encounterUuid) {
        for (Encounter encounter : encounters) {
            if (encounter.getEncounterType().getUuid().equals(encounterUuid)) {
                return encounter;
            }
        }
        return null;
    }

    private List<Obs> getSavedDocuments(Set<Obs> allObs, String conceptUuid) {
        List<Obs> obsList = new ArrayList<>();
        for (Obs obs : allObs) {
            if (obs.getConcept().getUuid().equals(conceptUuid)) {
                obsList.add(obs);
            }
        }
        Collections.sort(obsList, new IdBasedComparator());
        return obsList;
    }

    private Obs getSavedDocument(Set<Obs> allObs, String conceptUuid) {
        List<Obs> savedDocuments = getSavedDocuments(allObs, conceptUuid);
        return savedDocuments.size() == 0 ? null : savedDocuments.get(0);
    }

    private Date getDateFromString(String date) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        return simpleDateFormat.parse(date);
    }

    class IdBasedComparator implements Comparator<Obs> {
        @Override
        public int compare(Obs o1, Obs o2) {
            return o1.getId().compareTo(o2.getId());
        }
    }
}
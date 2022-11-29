package org.bahmni.module.bahmnicore.util;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.LocationTag;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class VisitIdentificationHelperIT extends BaseModuleWebContextSensitiveTest {
    public static final String TEST_VISIT_TYPE = "TEST VISIT TYPE";

    @Autowired
    VisitService visitService;
    @Autowired
    BahmniVisitLocationService bahmniVisitLocationService;
    @Autowired
    PatientService patientService;

    VisitIdentificationHelper visitIdentificationHelper;

    @Before
    public void setUp() throws Exception {
        executeDataSet("visitIdentificationHelper.xml");

        visitIdentificationHelper = new VisitIdentificationHelper(visitService, bahmniVisitLocationService);
    }

    @Test
    public void shouldInitializeNewVisitWhenNextVisitWithIn24Hours() throws Exception {
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-13 03:00:00");

        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate, null, null,"l3602jn5-9fhb-4f20-866b-0ece24561525");
        assertEquals(8, visit.getId().intValue());

        Date stopTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-13 06:00:00");
        assertEquals(accessionDate, visit.getStartDatetime());
        assertEquals(stopTime, visit.getStopDatetime());
    }

    @Test
    public void shouldInitializeNewVisitWhenNextVisitNotWithIn24Hours() throws Exception {
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-18 03:00:00");
        Date stopTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-18 23:59:59");

        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate, null, null,"l3602jn5-9fhb-4f20-866b-0ece24561525");

        assertTrue("Setup (visitIdentificationHelper.xml) creates visit ids 1-5. New visit id should be greater than 5", visit.getId() > 5);
        assertEquals(accessionDate, visit.getStartDatetime());
        assertEquals(stopTime, visit.getStopDatetime());
    }

    @Test
    public void shouldInitializeNewVisitWhenNextVisitDoesNotExist() throws Exception {
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-21 03:00:00");
        Date stopTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-21 23:59:59");

        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate, null,null,"l3602jn5-9fhb-4f20-866b-0ece24561525");

        assertTrue("Setup (visitIdentificationHelper.xml) creates visit ids 1-5. New visit id should be greater than 5", visit.getId() > 5);
        assertEquals(accessionDate, visit.getStartDatetime());
        assertEquals(stopTime, visit.getStopDatetime());
    }

    @Test
    public void stretchEarlierVisitWhenMultipleVisitsForADate() throws Exception {
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-05-20 00:00:00");

        Context.getLocationService().getLocationByUuid("9356400c-a5a2-4532-8f2b-2361b3446eb8").addTag(new LocationTag("Visit Location","Visit Location"));
        Visit visit = visitIdentificationHelper.getVisitFor(patient, TEST_VISIT_TYPE, accessionDate,null,null, "9356400c-a5a2-4532-8f2b-2361b3446eb8");

        assertNotNull(visit);
        assertEquals(accessionDate, visit.getStartDatetime());

        Date stopTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-05-20 04:00:00");
        assertEquals(stopTime, visit.getStopDatetime());
    }

    @Test
    public void shouldSetVisitLocationWhileCreatingNewVisit() throws Exception {
        Patient patient = patientService.getPatient(1);
        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-21 03:00:00");

        Visit visit = visitIdentificationHelper.getVisitFor(patient,TEST_VISIT_TYPE,accessionDate,null,null,"l3602jn5-9fhb-4f20-866b-0ece24561525");

        assertEquals(visit.getLocation().getUuid(),"l38923e5-9fhb-4f20-866b-0ece24561525");
    }

    @Test
    public void shouldFetchTheExistingVisitForTheVisitLocation() throws Exception {

        Patient patient = patientService.getPatient(1);

        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-13 01:00:00");
        Visit visit = visitIdentificationHelper.getVisitFor(patient,TEST_VISIT_TYPE,accessionDate,null,null,"l3602jn5-9fhb-4f20-866b-0ece24561525");
        assertNotEquals(2, visit.getId().intValue());
        assertEquals(8, visit.getId().intValue());
    }

    @Test
    public void shouldCreateNewVisitIfThereIsNoExistingVisitForThatVisitLocation() throws Exception {
        Patient patient = patientService.getPatient(1);

        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-02-10 10:00:00");
        Visit visit = visitIdentificationHelper.getVisitFor(patient,TEST_VISIT_TYPE,accessionDate,null,null,"l3602jn5-9fhb-4f20-866b-0ece24561525");
        int existingActiveVisitIdInAnotherLocation = 1;
        String locationUuidOfNewVisitCreated = "l38923e5-9fhb-4f20-866b-0ece24561525";
        assertNotEquals(existingActiveVisitIdInAnotherLocation, visit.getId().intValue());
        assertEquals(visit.getLocation().getUuid(), locationUuidOfNewVisitCreated);
    }

    @Test
    public void shouldGetVisitAndUpdateLocationWhenThereIsActiveVisitWithoutLocationForPatient() throws Exception {
        Patient patient = patientService.getPatient(1);

        Date accessionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2016-02-13 00:00:00");
        Visit visit = visitIdentificationHelper.getVisitFor(patient,TEST_VISIT_TYPE,accessionDate,null,null,"l3602jn5-9fhb-4f20-866b-0ece24561525");
        int existingActiveVisitIdWithLocationNull = 9;

        assertEquals(existingActiveVisitIdWithLocationNull, visit.getId().intValue());
        assertEquals(visit.getLocation().getUuid(), "l38923e5-9fhb-4f20-866b-0ece24561525");
    }
}
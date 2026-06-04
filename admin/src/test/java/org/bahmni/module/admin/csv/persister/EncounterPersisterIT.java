package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.KeyValue;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.csv.models.MultipleEncounterRow;
import org.bahmni.module.admin.csv.utils.CSVUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
@Ignore
public class EncounterPersisterIT extends BaseIntegrationTest {
    
    @Autowired
    private EncounterPersister encounterPersister;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private VisitService visitService;

    protected UserContext userContext;
    private boolean shouldMatchExactPatientId = false;

    @Before
    public void setUp() throws Exception {
        executeDataSet("baseMetaData.xml");
        executeDataSet("diagnosisMetaData.xml");
        executeDataSet("dispositionMetaData.xml");
        executeDataSet("dataSetup.xml");
        String path = Thread.currentThread().getContextClassLoader().getResource("").getPath();
        System.setProperty("OPENMRS_APPLICATION_DATA_DIRECTORY", path);


        Context.authenticate("admin", "test");
        userContext = Context.getUserContext();
        boolean shouldMatchExactPatientId = false;
        boolean shouldPerformForm2Validations = false;
        encounterPersister.init(userContext, null, shouldMatchExactPatientId, null, shouldPerformForm2Validations);
    }

    @Test
    public void failValidationForEmptyEncounterType() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        Messages rowResult = encounterPersister.persist(multipleEncounterRow);
        assertTrue("No Encounter details. Should have failed", !rowResult.isEmpty());
    }

    @Test
    public void failValidationForEncounterTypeNotFound() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "INVALID ENCOUNTER TYPE";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.encounterDateTime = "11-11-1111";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages validationErrors = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Invalid Encounter Type not found. Error Message:",
                validationErrors.toString().contains("Encounter type:'INVALID ENCOUNTER TYPE' not found"));
    }

    @Test
    public void failValidationForVisitTypeNotFound() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "INVALID VISIT TYPE";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages validationErrors = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Invalid Visit Type not found. Error Message:" + validationErrors.toString(),
                validationErrors.toString().contains("Visit type:'INVALID VISIT TYPE' not found"));
    }

    @Test
    public void failValidationForEmptyVisitType() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages validationErrors = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Visit Type null not found. Error Message:" + validationErrors.toString(),
                validationErrors.toString().contains("Visit type:'null' not found"));
    }

    @Test
    public void failValidationForEncounterDateInIncorrectFormat() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.patientIdentifier = "GAN200000";
        multipleEncounterRow.visitType = "OPD";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.encounterDateTime = "1977/08/23";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages validationErrors = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Encounter date time is required and should be 'dd/mm/yyyy' format.. Error Message:" + validationErrors.toString(),
                validationErrors.toString().contains("Unparseable date: \"1977/08/23\""));
    }

    @Test
    public void noValidationForEncounters() {
        Messages validationErrors = encounterPersister.validate(new MultipleEncounterRow());
        assertTrue("No Validation failure. Encounter Import does not run validation stage", validationErrors.isEmpty());
    }

    @Test
    public void persistEncountersForPatient() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row.", errorMessages.isEmpty());

        Context.openSession();
        Context.authenticate("admin", "test");
        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.flushSession();
        Context.closeSession();

        assertEquals(1, encounters.size());

        Encounter encounter = encounters.get(0);
        assertEquals("Anad", encounter.getPatient().getGivenName());
        assertEquals("Kewat", encounter.getPatient().getFamilyName());
        assertEquals("OPD", encounter.getVisit().getVisitType().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());

        Date encounterDatetime = encounter.getEncounterDatetime();
        assertEquals("1111-11-11", new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN).format(encounterDatetime));
    }

    @Test
    public void createVisitAsPerDatesInFile() throws Exception {
        String registrationNumber = "GAN200000";
        String visitStartDate = "2011-11-11";
        String visitEndDate = "2011-12-13";
        String encounterDateTime = "2011-12-12";

        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = registrationNumber;
        multipleEncounterRow.visitStartDate = visitStartDate;
        multipleEncounterRow.visitEndDate = visitEndDate;

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.encounterDateTime = encounterDateTime;

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row.", errorMessages.isEmpty());

        Context.openSession();
        Context.authenticate("admin", "test");

        Patient patient = new Patient();
        PatientIdentifier patientIdentifier = new PatientIdentifier();
        patientIdentifier.setIdentifier(registrationNumber);
        patient.addIdentifier(patientIdentifier);

        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.flushSession();
        Context.closeSession();

        Visit newlyCreatedVisit = encounters.get(0).getVisit();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN);
        assertEquals(visitStartDate, simpleDateFormat.format(newlyCreatedVisit.getStartDatetime()));
        assertEquals(visitEndDate, simpleDateFormat.format(newlyCreatedVisit.getStopDatetime()));
        assertEquals(encounterDateTime, simpleDateFormat.format(newlyCreatedVisit.getEncounters().iterator().next().getEncounterDatetime()));
    }


    @Test
    public void persistEncounterWithObservationHierarchyForPatient() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("Vitals.WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages persistenceResult = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row." + persistenceResult.asString(), StringUtils.isEmpty(persistenceResult.asString()));

        Context.openSession();
        Context.authenticate("admin", "test");

        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.flushSession();
        Context.closeSession();

        assertEquals(1, encounters.size());

        Encounter encounter = encounters.get(0);
        assertEquals("Anad", encounter.getPatient().getGivenName());
        assertEquals("Kewat", encounter.getPatient().getFamilyName());
        assertEquals("OPD", encounter.getVisit().getVisitType().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());

        assertEquals(1, encounter.getAllObs().size());

        Obs vitals = encounter.getAllObs().iterator().next();
        assertEquals("Vitals", vitals.getConcept().getName().getName());
        assertEquals(1, vitals.getGroupMembers().size());

        Obs weight = vitals.getGroupMembers().iterator().next();
        assertEquals("WEIGHT", weight.getConcept().getName().getName());
        assertEquals(Double.valueOf(150), weight.getValueNumeric());

        Date encounterDatetime = encounter.getEncounterDatetime();
        assertEquals("1111-11-11", new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN).format(encounterDatetime));
    }

    @Test
    public void persistEncountersWithSameDateWhichHasSameRootObservationsInIt() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow weightEncounter = new EncounterRow();
        weightEncounter.obsRows = new ArrayList<>();
        weightEncounter.obsRows.add(new KeyValue("Vitals.WEIGHT", "150"));
        weightEncounter.encounterDateTime = "1111-11-11";

        EncounterRow heightEncounter = new EncounterRow();
        heightEncounter.obsRows = new ArrayList<>();
        heightEncounter.obsRows.add(new KeyValue("Vitals.HEIGHT", "150"));
        heightEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(weightEncounter);
        multipleEncounterRow.encounterRows.add(heightEncounter);

        Messages persistenceResult = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row." + persistenceResult.asString(), StringUtils.isEmpty(persistenceResult.asString()));


        Context.openSession();
        Context.authenticate("admin", "test");

        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.flushSession();
        Context.closeSession();

        assertEquals(1, encounters.size());

        Encounter savedEncounter = encounters.get(0);
        assertEquals("Anad", savedEncounter.getPatient().getGivenName());
        assertEquals("Kewat", savedEncounter.getPatient().getFamilyName());
        assertEquals("OPD", savedEncounter.getVisit().getVisitType().getName());
        assertEquals("Consultation", savedEncounter.getEncounterType().getName());

        assertEquals(2, savedEncounter.getAllObs().size());

        Iterator<Obs> allObs = savedEncounter.getAllObs().iterator();
        Obs vitals1 = allObs.next();
        assertEquals("Vitals", vitals1.getConcept().getName().getName());
        Set<Obs> groupMembers = vitals1.getGroupMembers();
        assertEquals(1, groupMembers.size());

        Obs vitals2 = allObs.next();
        assertEquals("Vitals", vitals2.getConcept().getName().getName());
        assertEquals(1, vitals2.getGroupMembers().size());

        Date encounterDatetime = savedEncounter.getEncounterDatetime();
        assertEquals("1111-11-11", new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN).format(encounterDatetime));
    }

    @Test
    public void persistEncounterWithObservationHierarchyWithMultipleGroupMembers() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("Vitals.HEIGHT", "30"));
        anEncounter.obsRows.add(new KeyValue("Vitals.WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages persistenceResult = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row." + persistenceResult.asString(), StringUtils.isEmpty(persistenceResult.asString()));

        Context.openSession();
        Context.authenticate("admin", "test");

        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.flushSession();
        Context.closeSession();

        assertEquals(1, encounters.size());

        Encounter encounter = encounters.get(0);
        assertEquals("Anad", encounter.getPatient().getGivenName());
        assertEquals("Kewat", encounter.getPatient().getFamilyName());
        assertEquals("OPD", encounter.getVisit().getVisitType().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());

        assertEquals(1, encounter.getAllObs().size());
        Obs vitals = encounter.getAllObs().iterator().next();
        assertEquals("Vitals", vitals.getConcept().getName().getName());
        Set<Obs> groupMembers = vitals.getGroupMembers();
        assertEquals(2, groupMembers.size());
        assertEquals(Double.valueOf(150), findObs(groupMembers, "WEIGHT").getValueNumeric());
        assertEquals(Double.valueOf(30), findObs(groupMembers, "HEIGHT").getValueNumeric());
        Date encounterDatetime = encounter.getEncounterDatetime();
        assertEquals("1111-11-11", new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN).format(encounterDatetime));
    }

    @Test
    public void persistEncounterWithAbnormalObservation() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("Vitals.Height Data.HEIGHT", "30"));
        anEncounter.obsRows.add(new KeyValue("Vitals.Height Data.Height Abnormal", "true"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages persistenceResult = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row." + persistenceResult.asString(), StringUtils.isEmpty(persistenceResult.asString()));

        Context.openSession();
        Context.authenticate("admin", "test");
        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.flushSession();
        Context.closeSession();

        assertEquals(1, encounters.size());

        Encounter encounter = encounters.get(0);
        assertEquals("Anad", encounter.getPatient().getGivenName());
        assertEquals("Kewat", encounter.getPatient().getFamilyName());
        assertEquals("OPD", encounter.getVisit().getVisitType().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());

        assertEquals(1, encounter.getAllObs().size());

        Obs vitals = encounter.getAllObs().iterator().next();
        assertEquals("Vitals", vitals.getConcept().getName().getName());

        assertEquals(1, vitals.getGroupMembers().size());
        Obs heightData = vitals.getGroupMembers().iterator().next();
        assertEquals("Height Data", heightData.getConcept().getName().getName());

        assertEquals(2, heightData.getGroupMembers().size());
        Obs heightValue = findObs(heightData.getGroupMembers(), "HEIGHT");
        assertEquals(Double.valueOf(30), heightValue.getValueNumeric());

        Obs heightAbnormal = findObs(heightData.getGroupMembers(), "Height Abnormal");
        assertEquals("YES", heightAbnormal.getValueCoded().getName().getName());

        Date encounterDatetime = encounter.getEncounterDatetime();
        assertEquals("1111-11-11", new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN).format(encounterDatetime));
    }


    @Test
    public void persistMultipleEncountersForPatient() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";

        EncounterRow anotherEncounter = new EncounterRow();
        anotherEncounter.obsRows = new ArrayList<>();
        anotherEncounter.obsRows.add(new KeyValue("HEIGHT", "75"));
        anotherEncounter.encounterDateTime = "1111-11-12";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);
        multipleEncounterRow.encounterRows.add(anotherEncounter);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row.", errorMessages.isEmpty());

        Context.openSession();
        Context.authenticate("admin", "test");
        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.flushSession();
        Context.closeSession();

        assertEquals(2, encounters.size());
    }

    @Test
    public void persistObservationsForPatient() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row.", errorMessages.isEmpty());

        Context.openSession();
        Context.authenticate("admin", "test");
        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.closeSession();

        Encounter encounter = encounters.get(0);
        assertEquals(1, encounters.size());
        assertEquals("Anad", encounter.getPatient().getGivenName());
        assertEquals("Kewat", encounter.getPatient().getFamilyName());
        assertEquals("OPD", encounter.getVisit().getVisitType().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());
        assertEquals(1, encounter.getAllObs().size());
        assertEquals("WEIGHT", encounter.getAllObs().iterator().next().getConcept().getName().getName());
        Date obsDatetime = encounter.getAllObs().iterator().next().getObsDatetime();
        assertEquals("1111-11-11", new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN).format(obsDatetime));
        assertEquals("150.0", encounter.getAllObs().iterator().next().getValueAsString(Context.getLocale()));
    }

    @Test
    public void persistDiagnosis() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";
        anEncounter.diagnosesRows = new ArrayList<>();
        anEncounter.diagnosesRows.add(new KeyValue("Diagnosis1", "Diabetes"));

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounters.", errorMessages.isEmpty());

        Context.openSession();
        Context.authenticate("admin", "test");
        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.closeSession();

        Encounter encounter = encounters.get(0);
        assertEquals(1, encounters.size());
        assertEquals("Anad", encounter.getPatient().getGivenName());
        assertEquals("Kewat", encounter.getPatient().getFamilyName());
        assertEquals("OPD", encounter.getVisit().getVisitType().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());

        List<Obs> allObs = new ArrayList<>();
        allObs.addAll(encounter.getAllObs());
        assertEquals(2, allObs.size());

        int weightIndex = 0;
        int diagnosisIndex = 0;
        if (allObs.get(0).getGroupMembers() == null || allObs.get(0).getGroupMembers().size() == 0) {
            diagnosisIndex = 1;
        } else {
            weightIndex = 1;
        }
        Obs weightObs = allObs.get(weightIndex);
        Obs diagnosisObs = allObs.get(diagnosisIndex);
        assertEquals("WEIGHT", weightObs.getConcept().getName().getName());
        assertEquals("150.0", weightObs.getValueAsString(Context.getLocale()));
        assertEquals("Diagnosis Concept Set", diagnosisObs.getConcept().getName().getName());
        assertEquals("1111-11-11", new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN).format(diagnosisObs.getObsDatetime()));

        List<String> obsConceptNames = new ArrayList<>();
        for (Obs obs : diagnosisObs.getGroupMembers()) {
            obsConceptNames.add(obs.getConcept().getName().getName());
        }
        assertTrue(obsConceptNames.contains("Diabetes"));
        assertTrue(obsConceptNames.contains("Diagnosis Certainty"));
        assertTrue(obsConceptNames.contains("Diagnosis Order"));
        assertTrue(obsConceptNames.contains("Bahmni Diagnosis Status"));
        assertTrue(obsConceptNames.contains("Bahmni Diagnosis Revised"));
        assertTrue(obsConceptNames.contains("Bahmni Initial Diagnosis"));
    }

    @Test
    public void rollBackTransactionOncePersistenceFailsForOneResource() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";
        multipleEncounterRow.encounterRows = new ArrayList<>();

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));

        multipleEncounterRow.encounterRows.add(anEncounter);

        multipleEncounterRow.encounterType = "O1PD";
        encounterPersister.persist(multipleEncounterRow);
        Context.openSession();
        Context.authenticate("admin", "test");

        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        List<Visit> visits = visitService.getVisitsByPatient(new Patient(1));
        Context.closeSession();
        assertEquals(0, visits.size());
        assertEquals(0, encounters.size());
    }

    @Test
    public void throwErrorWhenPatientNotFound() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200001";

        encounterPersister.init(userContext, "NoMatch.groovy", shouldMatchExactPatientId, null, false);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);
        assertThat(errorMessages.size(), is(Matchers.greaterThan(0)));
        assertTrue(errorMessages.toString().contains("No matching patients found with ID:'GAN200001'"));
    }

    @Test
    public void throwErrorWhenMultiplePatientsFound() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "200000";
        encounterPersister.init(userContext, "MultipleMatchPatient.groovy", shouldMatchExactPatientId, null, false);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);

        assertTrue(errorMessages.toString().contains("GAN200000, SEM200000"));
    }

    @Test
    public void externalAlgorithmShouldReturnOnlyPatientsWithGanIdentifier() throws Exception {
        String patientId = "200000";

        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = patientId;
        encounterPersister.init(userContext, "GANIdentifier.groovy", shouldMatchExactPatientId, null, false);

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);

        assertTrue("Should have persisted the encounters.", errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");

        multipleEncounterRow.patientIdentifier = "GAN" + patientId;
        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.closeSession();
        assertEquals(1, encounters.size());
    }

    @Test
    public void externalAlgorithmReturnsPatientsMatchingIdAndName() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";
        multipleEncounterRow.patientAttributes = getPatientAttributes();
        encounterPersister.init(userContext, "IdAndNameMatch.groovy", shouldMatchExactPatientId, null, false);

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounters.", errorMessages.isEmpty());
        Context.openSession();
        Context.authenticate("admin", "test");

        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.closeSession();
        assertEquals(1, encounters.size());
    }

    @Test
    @Ignore
    public void persistCaseInsensitiveCodedConceptValues() {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("Diagnosis Certainty", "ConFirmeD"));
        anEncounter.encounterDateTime = "1111-11-11";
        anEncounter.diagnosesRows = new ArrayList<>();

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        encounterPersister.persist(multipleEncounterRow);

        Context.openSession();
        Context.authenticate("admin", "test");
        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.closeSession();

        Set<Obs> allObs = encounters.get(0).getAllObs();
        assertEquals(407, allObs.iterator().next().getValueCoded().getId().intValue());
    }

    @Test
    public void persistMultipleObservationForSameConcepts() throws Exception {
        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.encounterType = "Consultation";
        multipleEncounterRow.visitType = "OPD";
        multipleEncounterRow.patientIdentifier = "GAN200000";

        EncounterRow anEncounter = new EncounterRow();
        anEncounter.obsRows = new ArrayList<>();
        anEncounter.obsRows.add(new KeyValue("WEIGHT", "150"));
        anEncounter.obsRows.add(new KeyValue("HEIGHT", "70"));
        anEncounter.obsRows.add(new KeyValue("HEIGHT", "200"));
        anEncounter.obsRows.add(new KeyValue("Vitals.Height Data.HEIGHT", "10"));
        anEncounter.obsRows.add(new KeyValue("Vitals.Height Data.WEIGHT", "20"));
        anEncounter.encounterDateTime = "1111-11-11";

        multipleEncounterRow.encounterRows = new ArrayList<>();
        multipleEncounterRow.encounterRows.add(anEncounter);

        Messages errorMessages = encounterPersister.persist(multipleEncounterRow);
        assertTrue("Should have persisted the encounter row.", errorMessages.isEmpty());

        Context.openSession();
        Context.authenticate("admin", "test");
        List<Encounter> encounters = encounterService.getEncountersByPatientIdentifier(multipleEncounterRow.patientIdentifier);
        Context.closeSession();

        Encounter encounter = encounters.get(0);
        assertEquals(1, encounters.size());
        assertEquals("Anad", encounter.getPatient().getGivenName());
        assertEquals("Kewat", encounter.getPatient().getFamilyName());
        assertEquals("OPD", encounter.getVisit().getVisitType().getName());
        assertEquals("Consultation", encounter.getEncounterType().getName());
        Set<Obs> allObs = encounter.getAllObs();
        assertEquals(4, allObs.size());
        assertTrue(isObsPresentInEncounter(allObs, "WEIGHT", "150.0"));
        assertTrue(isObsPresentInEncounter(allObs, "HEIGHT", "70.0"));
        assertTrue(isObsPresentInEncounter(allObs, "HEIGHT", "200.0"));
        Obs vitals = findObsFromAllObs(allObs, "Vitals");
        Set<Obs> heightDataObs = vitals.getGroupMembers().iterator().next().getGroupMembers();
        assertEquals(1, vitals.getGroupMembers().size());
        assertEquals(2, heightDataObs.size());
        assertTrue(isObsPresentInEncounter(heightDataObs, "HEIGHT", "10.0"));
        assertTrue(isObsPresentInEncounter(heightDataObs, "WEIGHT", "20.0"));
        Date obsDatetime = allObs.iterator().next().getObsDatetime();
        assertEquals("1111-11-11", new SimpleDateFormat(CSVUtils.ENCOUNTER_DATE_PATTERN).format(obsDatetime));
    }

    private Obs findObsFromAllObs(Set<Obs> allObs, String concept) {
        for (Obs obs : allObs) {
            if(obs.getConcept().getName().getName().equals(concept))
                return obs;
        }
        return null;
    }

    private boolean isObsPresentInEncounter(Set<Obs> allObs, String concept, String value) {
        for (Obs obs : allObs) {
            if(obs.getConcept().getName().getName().equals(concept) && obs.getValueAsString(Context.getLocale()).equals(value))
                return true;
        }
        return false;
    }

    private List<KeyValue> getPatientAttributes() {
        List<KeyValue> patientAttributes = new ArrayList<>();
        patientAttributes.add(new KeyValue("given_name", "Anad"));
        return patientAttributes;
    }

    private Obs findObs(Set<Obs> groupMembers, String conceptName) {
          for (Obs groupMember : groupMembers) {
                  if (conceptName.equals(groupMember.getConcept().getName().getName())){
                        return groupMember;
                    }
              }
               return null;
           }
}
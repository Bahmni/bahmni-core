package org.openmrs.module.bahmniemrapi.encountertransaction.impl;

import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.BaseIntegrationTest;
import org.openmrs.module.bahmniemrapi.builder.BahmniDiagnosisRequestBuilder;
import org.openmrs.module.bahmniemrapi.builder.BahmniEncounterTransactionBuilder;
import org.openmrs.module.bahmniemrapi.builder.BahmniObservationBuilder;
import org.openmrs.module.bahmniemrapi.builder.ETConceptBuilder;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.VisitIdentificationHelper;
import org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship;
import org.openmrs.module.emrapi.CareSettingType;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BahmniEncounterTransactionServiceImplIT extends BaseIntegrationTest {

    private final String VISIT_UUID = "4e663d66-6b78-11e0-93c3-18a905e044dc";
    private final String PATIENT_UUID = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
    private final String LOCATION_UUID = "l3602jn5-9fhb-4f20-866b-0ece24561525";
    private final String ENCOUNTER_TYPE_UUID = "07000be2-26b6-4cce-8b40-866d8435b613";
    private final String VISIT_TYPE_UUID = "c0c579b0-8e59-401d-8a4a-976a0b183593";
    private final String VISIT_UUID1 = "1e5d5d48-6b78-11e0-93c3-18a905e044ce";
    private final String DISCHARGE_ENCOUNTER_TYPE_UUID = "02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad0";

    @Autowired
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private MockEncounterTransactionHandler mockEncounterTransactionHandler;

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionMetadata.xml");
        executeDataSet("obsRelationshipDataset.xml");
        executeDataSet("visitAttributeDataSet.xml");
        executeDataSet("drugOrderTestData.xml");
        executeDataSet("concepts.xml");
    }

    @Test
    public void shouldSaveFutureDrugOrdersInEncounterTransaction() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();

        Patient patient = patientService.getPatientByUuid(PATIENT_UUID);
        List<Order> originalOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"),
                orderService.getCareSettingByName("OUTPATIENT"), null);

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<>();
        providerSet.add(provider);

        EncounterTransaction.Concept concept = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation bahmniObservation = new BahmniObservationBuilder().withUuid(obsUuid).withConcept(concept)
                .withObsDateTime(obsDate)
                .withValue("obs-value")
                .build();
        EncounterTransaction.DrugOrder etDrugOrder = createETDrugOrder("1ce527b5-d6de-43f0-bc62-4616abacd77e", null, null,
                new DateTime().plusDays(2).toDate());

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withObservation(bahmniObservation)
                .withVisitTypeUuid(VISIT_TYPE_UUID)
                .withProviders(providerSet)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID)
                .withDrugOrders(Arrays.asList(etDrugOrder))
                .build();

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        List<Order> latestOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"),
                orderService.getCareSettingByName("OUTPATIENT"), null);
        assertEquals(originalOrders.size() + 1, latestOrders.size());
        assertEquals(Order.Action.NEW, latestOrders.get(originalOrders.size()).getAction());
        assertEquals(1, encounterTransaction.getDrugOrders().size());
    }

    @Test
    public void shouldSavePastDrugOrdersInEncounterTransaction() throws ParseException {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();

        Patient patient = patientService.getPatientByUuid(PATIENT_UUID);
        List<Order> originalOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"),
                orderService.getCareSettingByName("OUTPATIENT"), null);

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<>();
        providerSet.add(provider);

        EncounterTransaction.Concept concept = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation bahmniObservation = new BahmniObservationBuilder().withUuid(obsUuid).withConcept(concept)
                .withObsDateTime(obsDate)
                .withValue("obs-value")
                .build();
        Date pastScheduledDateForDrugOrder = new DateTime().minusDays(2).toDate();
        EncounterTransaction.DrugOrder etDrugOrder = createETDrugOrder("1ce527b5-d6de-43f0-bc62-4616abacd77e",
                null, null, pastScheduledDateForDrugOrder);

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withObservation(bahmniObservation)
                .withVisitTypeUuid(VISIT_TYPE_UUID)
                .withProviders(providerSet)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID)
                .withLocationUuid(LOCATION_UUID)
                .withDrugOrders(Arrays.asList(etDrugOrder))
                .build();

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        List<Order> latestOrders = orderService.getActiveOrders(patient, orderService.getOrderTypeByName("Drug Order"),
                orderService.getCareSettingByName("OUTPATIENT"), null);
        assertEquals(originalOrders.size() + 1, latestOrders.size());
        assertEquals(Order.Action.NEW, latestOrders.get(originalOrders.size()).getAction());
        assertEquals(0, encounterTransaction.getDrugOrders().size());

        //we are dropping millis here because DropMillisecondsHibernateInterceptor drops the milliseconds of objects before saving
        Date pastScheduledDateWithoutMillis = DateUtils.setMilliseconds(pastScheduledDateForDrugOrder, 0);
        //Ensure that two encounters are created.
        EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
                .setPatient(patient)
                .setFromDate(pastScheduledDateWithoutMillis)
                .setIncludeVoided(false)
                .createEncounterSearchCriteria();
        List<Encounter> encounters = encounterService.getEncounters(encounterSearchCriteria);


        assertEquals(2, encounters.size());
        assertEquals(1, encounters.get(0).getOrders().size());
        assertEquals(0, encounters.get(1).getOrders().size());
        assertEquals(1, encounterTransaction.getObservations().size());
        assertEquals(obsUuid, encounterTransaction.getObservations().iterator().next().getUuid());

    }

    @Test
    public void shouldSavePastDrugOrdersInEncounterTransactionWhenThereIsNoRetrospectiveVisit() throws ParseException {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();

        Patient patient = patientService.getPatientByUuid(PATIENT_UUID);

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<>();
        providerSet.add(provider);

        EncounterTransaction.Concept concept = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation bahmniObservation = new BahmniObservationBuilder().withUuid(obsUuid).withConcept(concept)
                .withObsDateTime(obsDate)
                .withValue("obs-value")
                .build();
        Date pastScheduledDateForDrugOrder = new DateTime().minusYears(12).toDate();
        EncounterTransaction.DrugOrder etDrugOrder = createETDrugOrder("1ce527b5-d6de-43f0-bc62-4616abacd77e", null, null, pastScheduledDateForDrugOrder);

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder().withObservation(bahmniObservation)
                .withVisitTypeUuid(VISIT_TYPE_UUID)
                .withProviders(providerSet)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID)
                .withLocationUuid(LOCATION_UUID)
                .withDrugOrders(Arrays.asList(etDrugOrder))
                .build();

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
        //we are dropping millis here because DropMillisecondsHibernateInterceptor drops the milliseconds of objects before saving
        Date pastScheduledDateWithoutMillis = DateUtils.setMilliseconds(pastScheduledDateForDrugOrder, 0);

        //Ensure that two encounters are created.
        EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
                .setPatient(patient)
                .setFromDate(pastScheduledDateWithoutMillis)
                .setToDate(pastScheduledDateWithoutMillis)
                .setIncludeVoided(false)
                .createEncounterSearchCriteria();
        List<Encounter> encounters = encounterService.getEncounters(encounterSearchCriteria);

        assertEquals(1, encounters.size());
        assertEquals(1, encounters.get(0).getOrders().size());
        DrugOrder order = (DrugOrder) encounters.get(0).getOrders().iterator().next();
        assertEquals("1ce527b5-d6de-43f0-bc62-4616abacd77e", order.getDrug().getUuid());
        assertEquals(1, encounterTransaction.getObservations().size());
        BahmniObservation next = encounterTransaction.getObservations().iterator().next();
        assertEquals(obsUuid, next.getUuid());

    }


    @Test
    public void shouldSaveBahmniEncounterTransactionWithBahmniObservationsWithGivenUuid() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();

        EncounterTransaction.Concept build = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation bahmniObservation = new BahmniObservationBuilder().withUuid(obsUuid).withConcept(build)
                .withObsDateTime(obsDate)
                .withValue("obs-value")
                .build();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withObservation(bahmniObservation)
                .withVisitTypeUuid(VISIT_TYPE_UUID)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID)
                .build();

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        assertNotNull(encounterTransaction);
        assertEquals(1, encounterTransaction.getObservations().size());
        assertEquals(bahmniObservation.getValue(), encounterTransaction.getObservations().iterator().next().getValue());
        assertEquals(obsUuid, encounterTransaction.getObservations().iterator().next().getUuid());
        assertEquals("OPD", bahmniEncounterTransaction.getVisitType());
    }

    @Test
    public void shouldNotCreateANewVisitIfThereIsAnActiveVisit() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String patientUuid = PATIENT_UUID;
        String visitType = "OPD";
        Patient patientByUuid = patientService.getPatientByUuid(patientUuid);
        VisitIdentificationHelper visitIdentificationHelper = new VisitIdentificationHelper(visitService, null);

        EncounterTransaction.Concept concept = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation bahmniObservation = new BahmniObservationBuilder().withUuid(obsUuid).withConcept(concept)
                .withObsDateTime(obsDate)
                .withValue("obs-value")
                .build();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withObservation(bahmniObservation)
                .withPatientId("4")
                .withPatientUuid(patientUuid)
                .withEncounterTypeUuid("02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad1")
                .withVisitType(visitType)
                .build();

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        assertNotNull(visitIdentificationHelper.hasActiveVisit(patientByUuid));
        assertNotNull(savedEncounterTransaction);
        assertEquals(savedEncounterTransaction.getObservations().iterator().next().getUuid(), bahmniObservation.getUuid());
    }


    @Test
    public void shouldCreateANewVisitAndSetVisitLocationToVisitIfNoActiveVisit() throws Exception {
        executeDataSet("VisitLocationDataSet.xml");
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();
        String patientUuid = "75e04d42-3ca8-11e3-bf2b-0800271c1b75";
        String visitType = "Emergency";
        String locationUuid = "l3602jn5-9fhb-4f20-866b-0ece24561526";
        String encounterTypeUuid = "02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad1";

        EncounterTransaction.Concept concept = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation bahmniObservation = new BahmniObservationBuilder().withUuid(obsUuid).withConcept(concept)
                .withObsDateTime(obsDate)
                .withValue("obs-value")
                .build();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder().withPatientId("4")
                .withLocationUuid(locationUuid)
                .withPatientUuid(patientUuid)
                .withEncounterTypeUuid(encounterTypeUuid)
                .withVisitType(visitType)
                .withObservation(bahmniObservation)
                .build();
        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        Visit visit = Context.getVisitService().getVisitByUuid(savedEncounterTransaction.toEncounterTransaction().getVisitUuid());
        assertEquals(locationUuid, visit.getLocation().getUuid());
    }

    @Test
    public void shouldCreateVisitAttributeOfVisitStatusAsOpdIrrespectiveOfVisitType() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withVisitUuid(VISIT_UUID1)
                .withPatientUuid(PATIENT_UUID).withEncounterTypeUuid(ENCOUNTER_TYPE_UUID).build();

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(1, visit.getAttributes().size());
        assertEquals("OPD", visit.getAttributes().iterator().next().getValue());
    }

    @Test
    public void shouldCreateVisitAttributeOfVisitStatusAsIpdIfTheEncounterIsOfAdmissionType() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withVisitUuid(VISIT_UUID1)
                .withPatientUuid(PATIENT_UUID)
                .withEncounterTypeUuid("02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad9")
                .build();

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(2, visit.getAttributes().size());
        assertEquals("IPD", visit.getAttributes().iterator().next().getValue());
    }

    @Test
    public void shouldCreateVisitAttributeOfAdmissionStatusAsAdmittedIfTheEncounterIsOfAdmissionType() throws Exception {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withVisitUuid(VISIT_UUID1)
                .withPatientUuid(PATIENT_UUID)
                .withEncounterTypeUuid("02c533ab-b74b-4ee4-b6e5-ffb6d09a0ad9")
                .build();

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(2, visit.getAttributes().size());
        Iterator<VisitAttribute> visitAttributeIterator = visit.getAttributes().iterator();
        assertEquals("IPD", visitAttributeIterator.next().getValue());
        assertEquals("Admitted", visitAttributeIterator.next().getValue());
    }

    @Test
    public void shouldCreateVisitAttributeOfAdmissionStatusAsDischargedIfTheEncounterIsOfDischargeType() throws Exception {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withVisitUuid(VISIT_UUID1)
                .withPatientUuid(PATIENT_UUID)
                .withEncounterTypeUuid(DISCHARGE_ENCOUNTER_TYPE_UUID)
                .build();

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(2, visit.getAttributes().size());
        Iterator<VisitAttribute> visitAttributeIterator = visit.getAttributes().iterator();
        assertEquals("OPD", visitAttributeIterator.next().getValue());
        assertEquals("Discharged", visitAttributeIterator.next().getValue());
    }

    @Test
    public void shouldNotCreateVisitAttributeOfAdmissionStatusIfTheEncounterTypeIsOfOtherThanAdmissionAndDischarged()
            throws Exception {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withVisitUuid(VISIT_UUID1)
                .withPatientUuid(PATIENT_UUID)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .build();

        BahmniEncounterTransaction savedEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        Visit visit = visitService.getVisitByUuid(savedEncounterTransaction.getVisitUuid());
        assertNotNull(visit);
        assertEquals(1, visit.getAttributes().size());
        Iterator<VisitAttribute> visitAttributeIterator = visit.getAttributes().iterator();
        assertEquals("OPD", visitAttributeIterator.next().getValue());
    }

    @Test
    public void shouldCreateVisitAttributeWhenTheDischargeIsRolledBack() {
        String encounterUuid = "bb0af6767-707a-4629-9850-f1529a163ab0";
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder().withEncounterTypeUuid(DISCHARGE_ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID1)
                .withEncounterUuid(encounterUuid)
                .withReason("Undo Discharge")
                .build();

        bahmniEncounterTransactionService.delete(bahmniEncounterTransaction);

        Encounter encounter = encounterService.getEncounterByUuid("bb0af6767-707a-4629-9850-f1529a163ab0");
        assertTrue(encounter.isVoided());

        Visit visit = visitService.getVisitByUuid(VISIT_UUID1);
        assertNotNull(visit);

        VisitAttribute visitAttribute = getAdmittedVisitAttribute(visit);
        assertNotNull(visitAttribute);
        assertEquals("Admitted", visitAttribute.getValue());
    }

    private VisitAttribute getAdmittedVisitAttribute(Visit visit) {
        for (VisitAttribute visitAttribute : visit.getAttributes()) {
            if (visitAttribute.getAttributeType().getName().equalsIgnoreCase("Admission Status")) {
                return visitAttribute;
            }
        }
        return null;
    }

    @Test
    public void shouldSaveObsRelationShipWhenBothObservationsAreInSameEncounter() {
        Date obsDate = new Date();
        String srcObsUuid = UUID.randomUUID().toString();
        String targetObsUuid = UUID.randomUUID().toString();

        EncounterTransaction.Concept targetConcept = new ETConceptBuilder()
                .withUuid("c607c80f-1ea9-4da3-bb88-6276ce8868dd")
                .withName("WEIGHT (KG)")
                .build();
        BahmniObservation targetObs = new BahmniObservationBuilder().withUuid(targetObsUuid).withConcept(targetConcept)
                .withObsDateTime(obsDate)
                .withComment("comment")
                .withTargetObsRelationship(new ObsRelationship(null, null, "qualified-by"))
                .withValue(150.0)
                .build();

        EncounterTransaction.Concept srcConcept = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation srcObs = new BahmniObservationBuilder().withUuid(srcObsUuid).withConcept(srcConcept)
                .withObsDateTime(obsDate)
                .withComment("comment")
                .withTargetObsRelationship(new ObsRelationship(targetObs, null, "qualified-by"))
                .withValue("src-value")
                .build();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withVisitUuid(VISIT_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withObservation(srcObs, targetObs)
                .build();


        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        assertEquals(2, encounterTransaction.getObservations().size());

        BahmniObservation savedSrcObs = getObservationByConceptUuid(encounterTransaction.getObservations(),
                srcConcept.getUuid());
        assertEquals(srcObs.getValue(), savedSrcObs.getValue());
        assertEquals(srcObsUuid, savedSrcObs.getUuid());
        assertEquals(srcConcept.getUuid(), savedSrcObs.getConceptUuid());

        assertEquals(targetObs.getValue(), savedSrcObs.getTargetObsRelation().getTargetObs().getValue());
        assertEquals(targetObsUuid, savedSrcObs.getTargetObsRelation().getTargetObs().getUuid());
        assertEquals(targetConcept.getUuid(), savedSrcObs.getTargetObsRelation().getTargetObs().getConceptUuid());
    }

    @Test
    public void shouldSaveObsRelationShipWhenBothObservationsAreInDifferentEncounter() throws ParseException {
        Date obsDate = new Date();
        String srcObsUuid = UUID.randomUUID().toString();
        String targetObsUuid = "f6ec1267-8eac-415f-a3f0-e47be2c8bb67";


        EncounterTransaction.Concept targetConcept = new ETConceptBuilder()
                .withUuid("a09ab2c5-878e-4905-b25d-5784167d0216")
                .withName("CD4 COUNT")
                .build();
        BahmniObservation targetObs = new BahmniObservationBuilder().withUuid(targetObsUuid).withConcept(targetConcept)
                .withObsDateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S").parse("2008-08-15 00:00:00.0"))
                .withComment("comment")
                .withTargetObsRelationship(new ObsRelationship(null, null, "qualified-by"))
                .withValue(175d)
                .build();

        EncounterTransaction.Concept srcConcept = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation srcObs = new BahmniObservationBuilder().withUuid(srcObsUuid).withConcept(srcConcept)
                .withObsDateTime(obsDate)
                .withComment("comment")
                .withTargetObsRelationship(new ObsRelationship(targetObs, null, "qualified-by"))
                .withValue("src-value")
                .build();

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder().withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID).withObservation(srcObs).build();

        BahmniEncounterTransaction mappedBahmniEncounterTransaction = bahmniEncounterTransactionService
                .save(bahmniEncounterTransaction);

        assertEquals(1, mappedBahmniEncounterTransaction.getObservations().size());
        BahmniObservation savedSrcObs = mappedBahmniEncounterTransaction.getObservations().iterator().next();
        assertEquals(srcObs.getValue(), savedSrcObs.getValue());
        assertEquals(srcObsUuid, savedSrcObs.getUuid());
        assertEquals(srcObs.getConcept().getUuid(), savedSrcObs.getConceptUuid());
        assertEquals(targetObs.getValue(), savedSrcObs.getTargetObsRelation().getTargetObs().getValue());
        assertEquals(targetObs.getUuid(), savedSrcObs.getTargetObsRelation().getTargetObs().getUuid());
        assertEquals(targetConcept.getUuid(), savedSrcObs.getTargetObsRelation().getTargetObs().getConceptUuid());
        assertEquals(targetObs.getObservationDateTime(),
                savedSrcObs.getTargetObsRelation().getTargetObs().getObservationDateTime());
    }

    @Test
    public void shouldSavePastDrugOrdersInEncounterTransactionWhenThereIsNoRetrospectiveVisitWithNoVisitTypeUuid() throws ParseException {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();

        Patient patient = patientService.getPatientByUuid(PATIENT_UUID);

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<EncounterTransaction.Provider>();
        providerSet.add(provider);

        EncounterTransaction.Concept obsConcept = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation bahmniObservation = new BahmniObservationBuilder().withUuid(obsUuid).withConcept(obsConcept)
                .withObsDateTime(obsDate)
                .withComment("comment")
                .withTargetObsRelationship(new ObsRelationship(null, null, "qualified-by"))
                .withValue("obs-value")
                .build();
        Date pastScheduledDateForDrugOrder = new DateTime().minusYears(12).toDate();
        EncounterTransaction.DrugOrder etDrugOrder = createETDrugOrder("1ce527b5-d6de-43f0-bc62-4616abacd77e", null, null, pastScheduledDateForDrugOrder);

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withObservation(bahmniObservation)
                .withVisitType("Hospitalization")
                .withProviders(providerSet)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID)
                .withLocationUuid(LOCATION_UUID)
                .withDrugOrders(Arrays.asList(etDrugOrder))
                .build();


        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
        //we are dropping millis here because DropMillisecondsHibernateInterceptor drops the milliseconds of objects before saving
        Date pastScheduledDateWithoutMillis = DateUtils.setMilliseconds(pastScheduledDateForDrugOrder, 0);

        //Ensure that two encounters are created.
        EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
                .setPatient(patient)
                .setFromDate(pastScheduledDateWithoutMillis)
                .setToDate(pastScheduledDateWithoutMillis)
                .setIncludeVoided(false)
                .createEncounterSearchCriteria();
        List<Encounter> encounters = encounterService.getEncounters(encounterSearchCriteria);


        assertEquals(1, encounters.size());
        Encounter encounter = encounters.get(0);

        assertEquals(1, encounter.getOrders().size());
        DrugOrder order = (DrugOrder) encounter.getOrders().iterator().next();
        assertEquals("1ce527b5-d6de-43f0-bc62-4616abacd77e", order.getDrug().getUuid());
        assertEquals(1, encounterTransaction.getObservations().size());
        assertEquals(obsUuid, encounterTransaction.getObservations().iterator().next().getUuid());

    }

    @Test
    public void shouldSaveDiagnoses(){
        EncounterTransaction.Concept feverConcept = new ETConceptBuilder().withName("Fever")
                .withUuid("9169366f-3c7f-11e3-8f4c-005056823ee3")
                .build();
        BahmniDiagnosisRequest bahmniDiagnosis = new BahmniDiagnosisRequestBuilder()
                .withCodedAnswer(feverConcept)
                .withOrder("PRIMARY")
                .withCertainty("PRESUMED")
                .withStatus(new ETConceptBuilder()
                        .withUuid("d102c80f-1yz9-4da3-bb88-8122ce8868eg")
                        .withName("Ruled Out").build())
                .build();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withDiagnoses(bahmniDiagnosis)
                .withVisitTypeUuid(VISIT_TYPE_UUID)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID)
                .build();

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
        Context.flushSession();
        Context.clearSession();

        assertThat(encounterTransaction.getBahmniDiagnoses().size(), is(equalTo(1)));
        BahmniDiagnosis diagnosis = encounterTransaction.getBahmniDiagnoses().get(0);
        assertThat(diagnosis.getCertainty(), is(equalTo("PRESUMED")));
        assertThat(diagnosis.getOrder(), is(equalTo("PRIMARY")));
        assertThat(diagnosis.getCodedAnswer().getName(), is(equalTo("Fever")));
        Encounter savedEncounter = Context.getEncounterService().getEncounterByUuid(encounterTransaction
                .getEncounterUuid());
        assertThat(savedEncounter.getObsAtTopLevel(true).size(), is(equalTo(1)));
        assertThat(savedEncounter.getAllObs(true).size(), is(equalTo(6)));

        encounterTransaction.getBahmniDiagnoses().get(0).setCertainty("CONFIRMED");


        encounterTransaction = bahmniEncounterTransactionService.save(encounterTransaction);
        Context.flushSession();
        Context.clearSession();

        assertThat(encounterTransaction.getBahmniDiagnoses().size(), is(equalTo(1)));
        diagnosis = encounterTransaction.getBahmniDiagnoses().get(0);
        assertThat(diagnosis.getCertainty(), is(equalTo("CONFIRMED")));
        assertThat(diagnosis.getOrder(), is(equalTo("PRIMARY")));
        assertThat(diagnosis.getCodedAnswer().getName(), is(equalTo("Fever")));
        savedEncounter = Context.getEncounterService().getEncounterByUuid(encounterTransaction
                .getEncounterUuid());
        assertThat(savedEncounter.getObsAtTopLevel(true).size(), is(equalTo(1)));
        assertThat(savedEncounter.getAllObs(true).size(), is(equalTo(7)));
        assertThat(savedEncounter.getAllObs(false).size(), is(equalTo(6)));
    }

    @Test
    public void shouldRunAllRegisteredHandlers() {
        Date obsDate = new Date();
        String obsUuid = UUID.randomUUID().toString();

        EncounterTransaction.Concept build = new ETConceptBuilder()
                .withUuid("96408258-000b-424e-af1a-403919332938")
                .withName("FAVORITE FOOD, NON-CODED")
                .build();
        BahmniObservation bahmniObservation = new BahmniObservationBuilder().withUuid(obsUuid).withConcept(build)
                .withObsDateTime(obsDate)
                .withValue("obs-value")
                .build();
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransactionBuilder()
                .withObservation(bahmniObservation)
                .withVisitTypeUuid(VISIT_TYPE_UUID)
                .withEncounterTypeUuid(ENCOUNTER_TYPE_UUID)
                .withPatientUuid(PATIENT_UUID)
                .withVisitUuid(VISIT_UUID)
                .build();

        int numberOfTimesSaveWasCalled = mockEncounterTransactionHandler.numberOfTimesSaveWasCalled;
        bahmniEncounterTransactionService.save(bahmniEncounterTransaction);

        assertThat(mockEncounterTransactionHandler.numberOfTimesSaveWasCalled, is(equalTo(numberOfTimesSaveWasCalled +
                1)));
    }

    private BahmniObservation getObservationByConceptUuid(Collection<BahmniObservation> bahmniObservations,
                                                          String conceptUuid) {
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            if (conceptUuid.equals(bahmniObservation.getConceptUuid())) {
                return bahmniObservation;
            }
        }
        return null;
    }

    private EncounterTransaction.DrugOrder createETDrugOrder(String drugUuid, String action, String previousOrderUuid,
                                                             Date scheduledDate) {
        EncounterTransaction.Drug encounterTransactionDrug = new EncounterTransaction.Drug();
        encounterTransactionDrug.setUuid(drugUuid);

        EncounterTransaction.DrugOrder drugOrder = new EncounterTransaction.DrugOrder();
        drugOrder.setCareSetting(CareSettingType.OUTPATIENT);
        drugOrder.setAction(action);
        drugOrder.setOrderType("Drug Order");
        drugOrder.setPreviousOrderUuid(previousOrderUuid);
        drugOrder.setDrug(encounterTransactionDrug);
        drugOrder.setDosingInstructionType(
                "org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions.FlexibleDosingInstructions");
        drugOrder.setDuration(10);
        drugOrder.setDurationUnits("Days");

        drugOrder.setScheduledDate(scheduledDate);
        drugOrder.setDateActivated(null);
        drugOrder.setVoided(false);

        EncounterTransaction.DosingInstructions dosingInstructions = new EncounterTransaction.DosingInstructions();
        dosingInstructions.setAdministrationInstructions("{\"instructions\":\"As directed\"}");
        dosingInstructions.setAsNeeded(false);
        dosingInstructions.setDose(1.0);
        dosingInstructions.setDoseUnits("tab (s)");
        dosingInstructions.setFrequency("1/day x 7 days/week");
        dosingInstructions.setNumberOfRefills(0);
        dosingInstructions.setQuantity(10.0);
        dosingInstructions.setQuantityUnits(Context.getConceptService().getConcept(51).getName().getName());
        dosingInstructions.setRoute("UNKNOWN");
        drugOrder.setDosingInstructions(dosingInstructions);

        return drugOrder;
    }
}

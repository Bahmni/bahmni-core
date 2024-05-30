package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.ApplicationDataDirectory;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.net.URISyntaxException;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderDaoImplIT extends BaseIntegrationTest {

    @Autowired
    private OrderDaoImpl orderDao;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PatientService patientService;


    @Test
    public void getPrescribedDrugOrdersShouldNotGetDiscontinueOrders() throws Exception {
        executeDataSet("patientWithDiscontinuedOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null, null, null, false);

        assertThat(drugOrdersInLastVisit.size(), is(3));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(15, 16, 18));
    }

    @Test
    public void getPrescribedDrugOrdersShouldGetRevisedOrdersAloneIfRevisionIsInSameEncounter() throws Exception {
        executeDataSet("patientWithOrderRevisedInSameEncounter.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null, null, null, false);

        assertThat(drugOrdersInLastVisit.size(), is(1));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(16));
    }

    @Test
    public void getPrescribedDrugOrdersShouldGetBothRevisedOrdersAndPreviousOrderIfRevisionIsInDifferentEncounter() throws Exception {
        executeDataSet("patientWithOrderRevisedInDifferentEncounter.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, true, null, null, null, false);

        assertThat(drugOrdersInLastVisit.size(), is(2));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(15, 16));
    }

    @Test
    public void getPrescribedDrugOrdersShouldFetchAllPrescribedDrugOrdersInPastVisits() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrdersInLastVisit = orderDao.getPrescribedDrugOrders(patient, false, 1, null, null, false);
        assertThat(drugOrdersInLastVisit.size(), is(equalTo(1)));
        assertThat(getOrderIds(drugOrdersInLastVisit), hasItems(17));

        List<DrugOrder> drugOrdersInLastTwoVisit = orderDao.getPrescribedDrugOrders(patient, false, 2, null, null, false);
        assertThat(drugOrdersInLastTwoVisit.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrdersInLastTwoVisit), hasItems(15, 16, 17));

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, false, null, null, null, false);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17));
    }

    @Test
    public void getPrescribedDrugOrdersShouldFetchAllPrescribedDrugOrdersIncludingActiveVisit() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, null, null, false);
        assertThat(drugOrders.size(), is(equalTo(9)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17, 19, 21, 23, 24, 26, 27));


        drugOrders = orderDao.getPrescribedDrugOrders(patient, null, null, null, null, false);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17));
    }

    @Test
    public void getPrescribedDrugOrdersShouldFetchAllPrescribedDrugOrdersWithInGivenDateRange() throws Exception{
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        Date startDate = BahmniDateUtil.convertToDate("2003-01-01T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate("2013-09-09T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, startDate, null, false);
        assertThat(drugOrders.size(), is(equalTo(9)));
        assertThat(getOrderIds(drugOrders), hasItems(16, 15,21, 23, 24, 19, 17, 26, 27));

        drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, startDate, endDate, false);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15,16,17));

        drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, null, endDate, false);
        assertThat(drugOrders.size(), is(equalTo(3)));
        assertThat(getOrderIds(drugOrders), hasItems(15, 16, 17));

    }

    @Test
    public void getPrescribedDrugOrdersShouldFetchAllPastDrugOrdersThatAreActiveInGivenDateRange() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        Date startDate = BahmniDateUtil.convertToDate("2015-01-01T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate("2015-09-09T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);

        List<DrugOrder> drugOrders = orderDao.getPrescribedDrugOrders(patient, true, null, startDate, endDate, false);
        assertThat(drugOrders.size(), is(equalTo(9)));
        assertThat(getOrderIds(drugOrders), hasItems(21, 23, 24, 19, 17 ,16, 15, 26, 27));
    }

    @Test
    public void getVisitsWithOrdersShouldFetchVisitsWithGivenOrderType() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);

        List<Visit> visits = orderDao.getVisitsWithActiveOrders(patient, "Order", true, 1);

        assertThat(visits.size(), is(equalTo(1)));
        assertThat(visits.get(0).getId(), is(equalTo(5)));
    }

    @Test
    public void getPrescribedDrugOrdersForConceptsShouldFetchAllPrescribedDrugOrdersForGivenConceptsForGivenNoOfVisits() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = patientService.getPatient(2);

        List<Concept> concepts = new ArrayList<>();
        concepts.add(conceptService.getConcept(3));
        concepts.add(conceptService.getConcept(25));

        List<Visit> visits = orderService.getVisitsWithOrders(patient, "DrugOrder", true, 1);
        assertEquals(1, visits.size());

        List<DrugOrder> result = orderDao.getPrescribedDrugOrdersForConcepts(patient, true, visits, concepts, null, null);
        assertEquals(2, result.size());
        assertThat(getOrderIds(result), hasItems(55, 57));

    }

    @Test
    public void shouldFetchAllPrescribedDrugOrdersForGivenConceptsForGivenNoOfVisitsWithinGivenDateRange() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Date startDate = BahmniDateUtil.convertToDate("2013-08-07T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate("2013-08-09T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);

        Patient patient = patientService.getPatient(2);

        List<Concept> concepts = new ArrayList<>();
        concepts.add(conceptService.getConcept(25));
        concepts.add(conceptService.getConcept(26));

        List<Visit> visits = orderService.getVisitsWithOrders(patient, "DrugOrder", true, 1);
        assertEquals(1, visits.size());

        List<DrugOrder> result = orderDao.getPrescribedDrugOrdersForConcepts(patient, true, visits, concepts, startDate, endDate);
        assertEquals(1, result.size());
        assertThat(getOrderIds(result), hasItems(57));

    }

    @Test
    public void shouldRetrieveAllVisitsRequested() throws Exception {
        executeDataSet("patientWithOrders.xml");
        String visitUuid1 = "1e5d5d48-6b78-11e0-93c3-18a97ba044dc";
        String visitUuid2 = "1e5d5d48-6b78-11e0-93c3-18a97b8ca4dc";
        String[] visitUuids = {visitUuid1, visitUuid2};

        List<Visit> visits = orderDao.getVisitsForUUids(visitUuids);

        assertThat(visits.size(), is(equalTo(visitUuids.length)));
        assertTrue(visitWithUuidExists(visitUuid1, visits));
        assertTrue(visitWithUuidExists(visitUuid2, visits));
    }

    @Test
    public void shouldGetDrugOrdersByVisitUuid() throws Exception {
        executeDataSet("patientWithOrders.xml");
        String visitUuid1 = "1e5d5d48-6b78-11e0-93c3-18a97ba044dc";
        String visitUuid2 = "1e5d5d48-6b78-11e0-93c3-18a97b8ca4dc";

        List<DrugOrder> prescribedDrugOrders = orderDao.getPrescribedDrugOrders(Arrays.asList(visitUuid1, visitUuid2));

        assertEquals(6, prescribedDrugOrders.size());
    }

    @Test
    public void getDrugOrderForRegimenShouldRetrieveDrugOrdersAssignedToTheRegimen() throws Exception {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json"))
                .thenReturn(new File(this.getClass().getClassLoader().getResource("templates.json").toURI()));
        orderDao.setApplicationDataDirectory(applicationDataDirectory);


        Collection<EncounterTransaction.DrugOrder> drugOrdersForCancerRegimen = orderDao.getDrugOrderForRegimen("Cancer Regimen, CAF");
        Collection<EncounterTransaction.DrugOrder> drugOrdersForBreastCancer = orderDao.getDrugOrderForRegimen("Breast Cancer - AC");

        assertEquals(1, drugOrdersForCancerRegimen.size());
        EncounterTransaction.DrugOrder drugOrder = drugOrdersForCancerRegimen.iterator().next();
        assertThat(drugOrder.getDrug().getName(), is(equalTo("DNS")));
        assertEquals(10, drugOrdersForBreastCancer.size());

    }

    @Test(expected = NullPointerException.class)
    public void getDrugOrderForRegimenShouldFailWhenFileDoesNotExist() {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json")).thenThrow(NullPointerException.class);
        orderDao.setApplicationDataDirectory(applicationDataDirectory);

        orderDao.getDrugOrderForRegimen("Breast Cancer - AC");
    }

    @Test
    public void getDrugOrderForRegimenShouldReturnEmptyListWhenRegimenNotFound() throws URISyntaxException {
        ApplicationDataDirectory applicationDataDirectory = mock(ApplicationDataDirectory.class);
        when(applicationDataDirectory.getFile("ordertemplates/templates.json"))
                .thenReturn(new File(this.getClass().getClassLoader().getResource("templates.json").toURI()));
        orderDao.setApplicationDataDirectory(applicationDataDirectory);

        Collection<EncounterTransaction.DrugOrder> drugOrders = orderDao.getDrugOrderForRegimen("Non existing regimen");
        assertThat(drugOrders.size(), is(equalTo(0)));

    }

    @Test
    public void getAllOrdersForVisitsShouldReturnEmptyListWhenNoVisitsFound() {
        assertThat(orderDao.getAllOrdersForVisits(null, null).size(), is(equalTo(0)));
        assertThat(orderDao.getAllOrdersForVisits(null, new ArrayList<Visit>()).size(), is(equalTo(0)));
    }

    @Test
    public void getAllOrdersForVisitsShouldReturnAllOrdersGivenAVisitAndAPatient() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Visit visit = Context.getVisitService().getVisit(1);
        OrderType orderType = Context.getOrderService().getOrderType(15);

        List<Order> allOrdersForVisits = orderDao.getAllOrdersForVisits(orderType, Arrays.asList(visit));

        assertThat(allOrdersForVisits.size(), is(equalTo(2)));

        Order firstOrder = Context.getOrderService().getOrder(15);
        Order secondOrder = Context.getOrderService().getOrder(16);
        assertThat(allOrdersForVisits, hasItems(firstOrder, secondOrder));
    }

    @Test
    public void getActiveDrugOrdersForPatient() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        List<Order> activeOrders = orderDao.getActiveOrders(patient, orderType, null, new Date(), null, null, null, null, null);

        assertEquals(3, activeOrders.size());
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f836");
        assertEquals(activeOrders.get(1).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f838");
        assertEquals(activeOrders.get(2).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f841");
    }

    @Test
    public void getActiveDrugOrdersForPatientFilteredByEncounters() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        Encounter encounter1 = Context.getEncounterService().getEncounter(19);
        Encounter encounter2 = Context.getEncounterService().getEncounter(20);
        Concept concept = Context.getConceptService().getConcept(16);
        HashSet<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);

        List<Order> activeOrders = orderDao.getActiveOrders(patient, orderType, null, new Date(), null, null, null, null, Arrays.asList(encounter1, encounter2));
        assertEquals(activeOrders.size(), 2);
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f836");
        assertEquals(activeOrders.get(1).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f841");

        List<Order> activeOrdersOfConcept = orderDao.getActiveOrders(patient, orderType, null, new Date(), concepts, null, null, null, Arrays.asList(encounter1, encounter2));
        assertEquals(activeOrdersOfConcept.size(), 1);
        assertEquals(activeOrdersOfConcept.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f836");

    }

    @Test
    public void getActiveDrugOrdersForPatientFilteredByDrugConcepts() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        Concept concept = Context.getConceptService().getConcept(16);
        HashSet<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);

        List<Order> activeOrders = orderDao.getActiveOrders(patient, orderType, null, new Date(), concepts, null, null, null, null);

        assertEquals(activeOrders.size(), 1);
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f836");
    }

    @Test
    public void getInactiveDrugOrdersForPatient() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        List<Order> activeOrders = orderDao.getInactiveOrders(patient, orderType, null, new Date(), null, null, null);

        assertEquals(3, activeOrders.size());
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f837", activeOrders.get(0).getUuid());
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f839", activeOrders.get(1).getUuid());
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f987", activeOrders.get(2).getUuid());
    }

    @Test
    public void getInactiveDrugOrdersForPatientFilteredByEncounters() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        Encounter encounter = Context.getEncounterService().getEncounter(19);

        List<Order> activeOrders = orderDao.getInactiveOrders(patient, orderType, null, new Date(), null, null, Arrays.asList(encounter));

        assertEquals(activeOrders.size(), 1);
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f839");
    }

    @Test
    public void getActiveDrugOrdersForPatientWithinDateRange() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        Date startDate = BahmniDateUtil.convertToDate("2014-01-01T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate("2014-09-09T00:00:00.000", BahmniDateUtil.DateFormatType.UTC);

        List<Order> activeOrders = orderDao.getActiveOrders(patient, orderType, null, new Date(), null, null, startDate, endDate, null);

        assertEquals(activeOrders.size(), 3);
        assertEquals(activeOrders.get(0).getUuid(), "cba00378-0c03-11e4-bb80-f18addb6f836");
    }

    @Test
    public void getInactiveDrugOrdersForPatientFilteredByDrugConcepts() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        Concept concept = Context.getConceptService().getConcept(16);
        HashSet<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);

        List<Order> activeOrders = orderDao.getInactiveOrders(patient, orderType, null, new Date(), concepts, null, null);

        assertEquals(activeOrders.size(), 2);
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f839", activeOrders.get(0).getUuid());
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f987", activeOrders.get(1).getUuid());
    }

    @Test
    public void getChildOrder() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Order order = Context.getOrderService().getOrderByUuid("cba00378-0c03-11e4-bb80-f18addb6f837");
        Order childOrder = Context.getOrderService().getOrderByUuid("cba00378-0c03-11e4-bb80-f18addb6f838");
        Order actual = orderDao.getChildOrder(order);
        assertEquals(actual, childOrder);
    }

    @Test
    public void getOrdersByPatientProgram() throws Exception {
        executeDataSet("patientWithOrders.xml");
        OrderType orderType = Context.getOrderService().getOrderType(1);
        Concept concept = Context.getConceptService().getConcept(16);
        HashSet<Concept> concepts = new HashSet<Concept>();
        concepts.add(concept);

        List<Order> activeOrders = orderDao.getOrdersByPatientProgram("dfdfoifo-dkcd-475d-b990-6d82327f36a3", orderType, null);

        assertEquals(2, activeOrders.size());
        assertEquals(activeOrders.get(0).getUuid(), "0246222e-f5f5-11e3-b47b-c8b69a44dcba");
        assertEquals(activeOrders.get(1).getUuid(), "0246222e-f5f5-11e3-b47b-c8b69a44badc");
    }

    @Test
    public void getOrdersByPatientProgramWithConceptNames() throws Exception {
        executeDataSet("patientWithOrders.xml");
        OrderType orderType = Context.getOrderService().getOrderType(1);
        HashSet<Concept> concepts = new HashSet<Concept>();
        Concept paracetamolConcept = Context.getConceptService().getConcept(24);
        concepts.add(paracetamolConcept);
        Concept nonOrderedDrugConcept = Context.getConceptService().getConcept(26);
        concepts.add(nonOrderedDrugConcept);

        List<Order> activeOrders = orderDao.getOrdersByPatientProgram("dfdfoifo-dkcd-475d-b990-6d82327f36a3", orderType, concepts);

        assertEquals(1, activeOrders.size());
        assertEquals(activeOrders.get(0).getUuid(), "0246222e-f5f5-11e3-b47b-c8b69a44dcba");
    }

    @Test
    public void getOrdersByLocationsWhenLocationUuidsAreProvided() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        List<String> locationUuids = new ArrayList<>();

        locationUuids.add("8d6c993e-c2cc-11de-7921-0010c6affd0f");
        locationUuids.add("8d6c993e-c2cc-11de-7000-0010c6affd0f");

        List<Order> activeOrders = orderDao.getAllOrders(patient, orderType, null, null, locationUuids);

        assertEquals(4, activeOrders.size());
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f836", activeOrders.get(0).getUuid());
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f839", activeOrders.get(1).getUuid());
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f841", activeOrders.get(2).getUuid());
        assertEquals("cba00378-0c03-11e4-bb80-f18addb6f987", activeOrders.get(3).getUuid());
    }

    @Test
    public void shouldReturnAllOrdersWhenLocationUuidsAreNotProvided() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        List<String> locationUuids = new ArrayList<>();

        List<Order> activeOrders = orderDao.getAllOrders(patient, orderType, null, null, locationUuids);

        assertEquals(3, activeOrders.size());
    }

    @Test
    public void shouldReturnEmptyListOfOrdersWhenEncountersAreNotThereForGivenLocationUuids() throws Exception {
        executeDataSet("patientWithOrders.xml");
        Patient patient = Context.getPatientService().getPatient(1001);
        OrderType orderType = Context.getOrderService().getOrderType(1);
        List<String> locationUuids = new ArrayList<>();
        locationUuids.add("8d6c993e-c2cc-11de-8d13-0010c6dffd0f");

        List<Order> activeOrders = orderDao.getAllOrders(patient, orderType, null, null, locationUuids);

        assertEquals(0, activeOrders.size());
    }

    private boolean visitWithUuidExists(String uuid, List<Visit> visits) {
        boolean exists = false;
        for (Visit visit : visits) {
            exists |= visit.getUuid().equals(uuid);
        }
        return exists;
    }

    private List<Integer> getOrderIds(List<DrugOrder> drugOrders) {
        ArrayList<Integer> ids = new ArrayList<>();
        for (DrugOrder drugOrder : drugOrders) {
            ids.add(drugOrder.getOrderId());
        }
        return ids;
    }
}
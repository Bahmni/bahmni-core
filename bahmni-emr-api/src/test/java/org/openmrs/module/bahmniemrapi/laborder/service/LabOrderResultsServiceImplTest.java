package org.openmrs.module.bahmniemrapi.laborder.service;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class LabOrderResultsServiceImplTest {

    @Mock
    private EncounterTransaction encounterTransaction;

    @Mock
    private Encounter encounter;

    @InjectMocks
    private LabOrderResultsServiceImpl labOrderResultsServiceImpl;

    @Before
    public void init() {
        initMocks(this);
        when(encounter.getVisit()).thenReturn(new Visit());
    }

    @Test
    public void filterTestOrdersEvenWhenTheyAreDiscontinued() throws Exception {
        List<String> concepts = Arrays.asList("concept1", "concept2","concept3");
        Map<String, Encounter> encounterTestOrderUuidMap = new HashMap<>();
        EncounterTransaction.Order order1 = createOrder("uuid1","concept1", Order.Action.NEW.toString(), null);
        EncounterTransaction.Order order2 = createOrder("uuid2", "concept2", Order.Action.REVISE.toString(), null);
        EncounterTransaction.Order order3 = createOrder("uuid3", "concept3", Order.Action.NEW.toString(), new Date());
        when(encounterTransaction.getOrders()).thenReturn(Arrays.asList(order1, order2, order3));

        List<EncounterTransaction.Order> orders = labOrderResultsServiceImpl.filterTestOrders(encounterTransaction, encounter, encounterTestOrderUuidMap, concepts, null, null);

        assertEquals(3, orders.size());
    }

    @Test
    public void filterTestOrdersShouldNotFilterByConcept() throws Exception {
        Map<String, Encounter> encounterTestOrderUuidMap = new HashMap<>();
        EncounterTransaction.Order order1 = createOrder("uuid1","concept1", Order.Action.NEW.toString(), null);
        when(encounterTransaction.getOrders()).thenReturn(Arrays.asList(order1));

        List<EncounterTransaction.Order> orders = labOrderResultsServiceImpl.filterTestOrders(encounterTransaction, encounter, encounterTestOrderUuidMap, null, null, null);

        assertEquals(1, orders.size());
    }

    @Test
    public void mapOrdersWithObsShouldMapAllObservationsToLabOrderResults() {
        EncounterTransaction.Order order1 = createOrder("uuid1","concept1", Order.Action.NEW.toString(), null);
        EncounterTransaction.Order order2 = createOrder("uuid2", "concept2", Order.Action.REVISE.toString(), null);
        List<EncounterTransaction.Order> testOrders = Arrays.asList(order1, order2);
        EncounterTransaction.Observation order1_Obs1 = createObservation("obsuuid1", order1.getUuid());
        EncounterTransaction.Observation order1_Obs2 = createObservation("obsuuid2", order1.getUuid());
        EncounterTransaction.Observation order2_Obs1 = createObservation("obsuuid3", order2.getUuid());
        List<EncounterTransaction.Observation> observations = Arrays.asList(order1_Obs1, order1_Obs2, order2_Obs1);
        Map<String, Encounter> orderToEncounterMapping = new HashMap<>();
        orderToEncounterMapping.put(order1.getUuid(), encounter);
        orderToEncounterMapping.put(order2.getUuid(), encounter);
        Map<String, Encounter> observationToEncounterMapping = new HashMap<>();
        observationToEncounterMapping.put(order1_Obs1.getUuid(), encounter);
        observationToEncounterMapping.put(order1_Obs2.getUuid(), encounter);
        observationToEncounterMapping.put(order2_Obs1.getUuid(), encounter);

        List<LabOrderResult> results = labOrderResultsServiceImpl.mapOrdersWithObs(testOrders, observations, orderToEncounterMapping, observationToEncounterMapping, new HashMap());

        assertEquals(3, results.size());
    }

    @Test
    public void mapOrdersWithObsShouldMapLabTestWithoutResultToLabOrderResult() {
        EncounterTransaction.Order order1 = createOrder("uuid1","concept1", Order.Action.NEW.toString(), null);
        List<EncounterTransaction.Order> testOrders = Arrays.asList(order1);
        Map<String, Encounter> orderToEncounterMapping = new HashMap<>();
        orderToEncounterMapping.put(order1.getUuid(), encounter);

        List<LabOrderResult> results = labOrderResultsServiceImpl.mapOrdersWithObs(testOrders, new ArrayList<EncounterTransaction.Observation>(), orderToEncounterMapping, new HashMap(), new HashMap());

        assertEquals(1, results.size());
    }

    @Test
    public void mapOrdersWithObsShouldNotMapDiscontinuedLabTestWithoutResultsToLabOrderResult() {
        EncounterTransaction.Order discontinuedOrder = createOrder("uuid1","concept1", Order.Action.NEW.toString(), new Date());
        List<EncounterTransaction.Order> testOrders = Arrays.asList(discontinuedOrder);
        Map<String, Encounter> orderToEncounterMapping = new HashMap<>();
        orderToEncounterMapping.put(discontinuedOrder.getUuid(), encounter);

        List<LabOrderResult> results = labOrderResultsServiceImpl.mapOrdersWithObs(testOrders, new ArrayList<EncounterTransaction.Observation>(), orderToEncounterMapping, new HashMap(), new HashMap());

        assertEquals(0, results.size());
    }

    private EncounterTransaction.Order createOrder(String uuid, String conceptName, String action, Date dateStopped) {
        EncounterTransaction.Order order = new EncounterTransaction.Order();
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setName(conceptName);
        order.setConcept(concept);
        order.setAction(action);
        order.setDateStopped(dateStopped);
        order.setUuid(uuid);
        order.setOrderType(LabOrderResultsServiceImpl.LAB_ORDER_TYPE);
        return order;
    }

    private EncounterTransaction.Observation createObservation(String uuid, String orderUuid) {
        EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
        observation.setUuid(uuid);
        observation.setOrderUuid(orderUuid);
        observation.setConcept(new EncounterTransaction.Concept());
        return observation;
    }

}
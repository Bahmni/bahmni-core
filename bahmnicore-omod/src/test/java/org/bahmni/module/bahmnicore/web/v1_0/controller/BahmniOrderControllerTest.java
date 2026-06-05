package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniOrderService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.order.contract.BahmniOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class BahmniOrderControllerTest {

    @Mock
    private BahmniOrderService bahmniOrderService;

    @Mock
    private ConceptService conceptService;

    private Patient patient;
    private Concept concept;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        concept = new Concept();
        patient = new Patient();
        patient.setUuid("patientUuid");
        when(conceptService.getConceptByName("Weight")).thenReturn(concept);
    }

    @Test
    public void shouldReturnBahmniOrdersForOrderType() throws Exception {
        BahmniObservation obs = new BahmniObservation();
        BahmniOrder bahmniOrder = new BahmniOrder();
        obs.setUuid("initialId");
        bahmniOrder.setBahmniObservations(Arrays.asList(obs));
        List<String> locationUuids = new ArrayList<>();
        locationUuids.add("LocationUuid");

        when(bahmniOrderService.ordersForOrderType("patientUuid", Arrays.asList(concept), null, null, "OrderTypeUuid", true, locationUuids)).thenReturn(Arrays.asList(bahmniOrder));

        BahmniOrderController bahmniOrderController = new BahmniOrderController(conceptService, bahmniOrderService);
        List<BahmniOrder> bahmniOrders = bahmniOrderController.get("patientUuid", Arrays.asList("Weight"),"OrderTypeUuid", null, null, null, null, true, locationUuids);

        verify(bahmniOrderService, never()).ordersForOrderUuid("patientUuid", Arrays.asList(concept), null, "someUuid");
        verify(bahmniOrderService, never()).ordersForVisit("visitUuid", "orderTypeUuid", Arrays.asList("Weight"), Arrays.asList(concept));
        verify(bahmniOrderService, times(1)).ordersForOrderType("patientUuid", Arrays.asList(concept),null, null, "OrderTypeUuid", true, locationUuids);
        assertEquals(1, bahmniOrders.size());
    }

    @Test
    public void shouldReturnBahmniOrdersForOrderUuid() throws Exception {
        BahmniObservation obs = new BahmniObservation();
        BahmniOrder bahmniOrder = new BahmniOrder();
        obs.setUuid("initialId");
        bahmniOrder.setBahmniObservations(Arrays.asList(obs));

        when(bahmniOrderService.ordersForOrderUuid("patientUuid", Arrays.asList(this.concept), null, "OrderUuid")).thenReturn(Arrays.asList(bahmniOrder));
        BahmniOrderController bahmniOrderController = new BahmniOrderController(conceptService, bahmniOrderService);
        List<BahmniOrder> bahmniOrders = bahmniOrderController.get("patientUuid", Arrays.asList("Weight"), null, null, "OrderUuid", 0, null, true, null);

        verify(bahmniOrderService, never()).ordersForOrderType("patientUuid", Arrays.asList(concept), null, null, "someUuid", true, null);
        verify(bahmniOrderService, never()).ordersForVisit("visitUuid", "orderTypeUuid", Arrays.asList("Weight"), Arrays.asList(concept));
        assertEquals(1, bahmniOrders.size());
    }

    @Test
    public void shouldReturnBahmniOrdersForVisit() throws Exception {
        BahmniObservation obs = new BahmniObservation();
        BahmniOrder bahmniOrder = new BahmniOrder();
        obs.setUuid("initialId");
        bahmniOrder.setBahmniObservations(Arrays.asList(obs));

        when(bahmniOrderService.ordersForVisit("visitUuid", "orderTypeUuid",  Arrays.asList("Weight"), Arrays.asList(concept))).thenReturn(Arrays.asList(bahmniOrder));
        BahmniOrderController bahmniOrderController = new BahmniOrderController(conceptService, bahmniOrderService);
        List<BahmniOrder> bahmniOrders = bahmniOrderController.get("patientUuid", Arrays.asList("Weight"), "orderTypeUuid", "visitUuid", null, null, Arrays.asList("Weight"), false, null);

        verify(bahmniOrderService, never()).ordersForOrderType("patientUuid", Arrays.asList(concept), null, null, "someUuid", true, null);
        verify(bahmniOrderService, never()).ordersForOrderUuid("patientUuid", Arrays.asList(concept), null, "someUuid");
        verify(bahmniOrderService, atLeastOnce()).ordersForVisit("visitUuid", "orderTypeUuid", Arrays.asList("Weight"), Arrays.asList(concept));
        assertEquals(1, bahmniOrders.size());
    }
}
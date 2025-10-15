package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.junit.Assert;
import org.junit.Test;
import org.openmrs.CareSetting;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class OrderServiceImplIT extends BaseIntegrationTest {

    @Autowired
    private OrderService bahmniOrderService;
    @Autowired
    private org.openmrs.api.OrderService orderService;
    @Autowired
    private PatientService patientService;

    @Test
    public void shouldCheckForExistenceOfConcept() throws Exception {
        String patientUuid = "75e04d42-3ca8-11e3-bf2b-0800271c1b75";
        String radiologyOrderTypeUuid = "92c1bdef-72d4-49d9-8a1f-804892f66abd";

        executeDataSet("radiologyOrderTestData.xml");
        ensureCorrectDataSetup(patientUuid, radiologyOrderTypeUuid);

        List<Order> pendingOrders = bahmniOrderService.getPendingOrders(patientUuid, radiologyOrderTypeUuid);

        Assert.assertEquals(1, pendingOrders.size());

        Order pendingOrder = pendingOrders.get(0);
        Assert.assertEquals("6d0ae386-707a-4629-9850-f15206e63ab0", pendingOrder.getUuid());
        Assert.assertEquals(patientUuid, pendingOrder.getPatient().getUuid());
        Assert.assertEquals(radiologyOrderTypeUuid, pendingOrder.getOrderType().getUuid());
        Assert.assertEquals("Radiology Order", pendingOrder.getOrderType().getName());
    }

    @Test
    public void shouldGetAllVisitsWhenNumberOfVisitsIsNull() throws Exception{
        executeDataSet("drugOrdersForVisits.xml");
        String patientUuid = "86526ed5-3c11-11de-a0ba-001ed98eb67a";
        Patient patient = patientService.getPatientByUuid(patientUuid);
        List<Visit> visitsWithOrders = bahmniOrderService.getVisitsWithOrders(patient, "DrugOrder", true, null);
        Assert.assertFalse(visitsWithOrders.isEmpty());
        Assert.assertEquals(2, visitsWithOrders.size());
        Assert.assertNotEquals((Integer)3001, visitsWithOrders.get(0).getId());
        Assert.assertNotEquals((Integer)3001, visitsWithOrders.get(1).getId());
    }


    @Test
    public void shouldGetOrdersForPatientAndOrderType() throws Exception{
        executeDataSet("patientWithOrders.xml");
        String orderTypeUuid = "bf7f3ab0-ae06-11e3-a5e2-0800200c9a66";
        String patientUuid = "75e04d42-3ca8-11e3-bf2b-0800271c1b75";

        List<Order> allOrders = bahmniOrderService.getAllOrders(patientUuid, orderTypeUuid, null, null, null);
        Assert.assertEquals(5, allOrders.size());
        Assert.assertEquals((Integer)20, allOrders.get(0).getId());
        Assert.assertEquals((Integer)19, allOrders.get(1).getId());
        Assert.assertEquals((Integer)15, allOrders.get(2).getId());
        Assert.assertEquals((Integer)16, allOrders.get(3).getId());
        Assert.assertEquals((Integer)17, allOrders.get(4).getId());

    }

    @Test
    public void shouldGetPagedOrdersForPatientAndOrderType() throws Exception{
        executeDataSet("patientWithOrders.xml");
        String orderTypeUuid = "bf7f3ab0-ae06-11e3-a5e2-0800200c9a66";
        String patientUuid = "75e04d42-3ca8-11e3-bf2b-0800271c1b75";

        List<Order> allOrders = bahmniOrderService.getAllOrders(patientUuid, orderTypeUuid, 1, 3, null);
        Assert.assertEquals(3, allOrders.size());
        Assert.assertEquals((Integer)19, allOrders.get(0).getId());
        Assert.assertEquals((Integer)15, allOrders.get(1).getId());
        Assert.assertEquals((Integer)16, allOrders.get(2).getId());
    }

    @Test
    public void shouldGetOrdersForPatientAndOrderTypeAndLocationUuid() throws Exception{
        executeDataSet("patientWithOrders.xml");
        String orderTypeUuid = "131168f4-15f5-102d-96e4-000c29c2a5d7";
        String patientUuid = "75e04d42-3ca8-11e3-bf2b-0800271c1b75";
        List<String> locationUuids = new ArrayList<>();
        locationUuids.add("8d6c993e-c2cc-11de-7921-0010c6affd0f");

        List<Order> allOrders = bahmniOrderService.getAllOrders(patientUuid, orderTypeUuid, 1, 1,locationUuids);

        Assert.assertEquals(2, allOrders.size());
        Assert.assertEquals((Integer)26, allOrders.get(0).getId());
    }

    private void ensureCorrectDataSetup(String patientUuid, String radiologyOrderTypeUuid) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        OrderType orderType = orderService.getOrderTypeByUuid(radiologyOrderTypeUuid);
        CareSetting careSetting = orderService.getCareSettingByName(CareSetting.CareSettingType.OUTPATIENT.toString());
        List<Order> allRadiologyOrdersForPatient = orderService.getOrders(patient, careSetting, orderType, true);
        Assert.assertTrue("More than 1 radiology orders are setup for the patient", allRadiologyOrdersForPatient.size() > 1);
    }

}
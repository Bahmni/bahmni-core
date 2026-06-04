package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.contract.drugorder.DrugOrderConfigResponse;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.DrugOrder;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class BahmniDrugOrderServiceImplIT extends BaseIntegrationTest {

    @Autowired
    private BahmniDrugOrderServiceImpl bahmniDrugOrderService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("drugOrdersTestData.xml");
        executeDataSet("visitAttributeDataSet.xml");
    }


    @Test
    public void shouldReturnOrderAttributeConceptNamesWithGetConfig() throws ParseException {
        DrugOrderConfigResponse config = bahmniDrugOrderService.getConfig();
        List<EncounterTransaction.Concept> orderAttributes = config.getOrderAttributes();

        assertEquals(2, orderAttributes.size());
        assertEquals("dispensed", orderAttributes.get(0).getName());
        assertEquals("administered", orderAttributes.get(1).getName());
    }

    @Test
    public void shouldReturnDiscontinuedOrderMap() throws Exception {
        executeDataSet("patientWithStoppedOrders.xml");
        DrugOrder newFirstOrder = (DrugOrder) Context.getOrderService().getOrder(15);
        DrugOrder revisedFirstOrder = (DrugOrder) Context.getOrderService().getOrder(16);
        DrugOrder newSecondOrder = (DrugOrder) Context.getOrderService().getOrder(18);
        DrugOrder discontinuedFirstOrder = (DrugOrder) Context.getOrderService().getOrder(17);
        DrugOrder discontinuedSecondOrder = (DrugOrder) Context.getOrderService().getOrder(19);

        List<DrugOrder> drugOrdersList = Arrays.asList(newFirstOrder, revisedFirstOrder, newSecondOrder);
        Map<String, DrugOrder> discontinuedOrderMap = bahmniDrugOrderService.getDiscontinuedDrugOrders(drugOrdersList);
        assertEquals(discontinuedOrderMap.get("2").getUuid(), discontinuedFirstOrder.getUuid());
        assertEquals(discontinuedOrderMap.get("4").getUuid(), discontinuedSecondOrder.getUuid());
        assertNull(discontinuedOrderMap.get("1"));

    }

    @Test
    public void shouldReturnEmptyDiscontinuedOrderMapWhenThereAreNoActiveDrugOrders() throws Exception {
        List<DrugOrder> drugOrdersList = new ArrayList<>();
        Map<String, DrugOrder> discontinuedOrderMap = bahmniDrugOrderService.getDiscontinuedDrugOrders(drugOrdersList);
        Assert.assertNotNull(discontinuedOrderMap);
        assertEquals(0, discontinuedOrderMap.size());

    }


}

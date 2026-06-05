/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.bahmni.module.bahmnicore.model;

import org.bahmni.module.bahmnicore.mapper.builder.BahmniDrugOrderBuilder;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class BahmniFeedDrugOrdersTest {
    @Test
    public void testGetUniqueOrdersReturnsUniqueOrdersWithDosageAndQuantityAdjusted() throws Exception {
        BahmniFeedDrugOrder order1 = new BahmniDrugOrderBuilder().withProductUuid("11").withNumberOfDaysAndDosage(10, 2).build();
        BahmniFeedDrugOrder order2 = new BahmniDrugOrderBuilder().withProductUuid("22").withNumberOfDaysAndDosage(5, 1).build();
        BahmniFeedDrugOrder order3 = new BahmniDrugOrderBuilder().withProductUuid("11").withNumberOfDaysAndDosage(10, 1).build();
        BahmniFeedDrugOrders bahmniFeedDrugOrders = new BahmniFeedDrugOrders(Arrays.asList(order1, order2, order3));

        List<BahmniFeedDrugOrder> uniqueOrders = bahmniFeedDrugOrders.getUniqueOrders();

        assertEquals(2, uniqueOrders.size());
        assertEquals("11", uniqueOrders.get(0).getProductUuid());
        assertEquals(30.0, (Object)uniqueOrders.get(0).getQuantity());
        assertEquals(20, uniqueOrders.get(0).getNumberOfDays());
        assertEquals(1.5, (Object)uniqueOrders.get(0).getDosage());
        assertEquals("22", uniqueOrders.get(1).getProductUuid());
        assertEquals(5.0, (Object)uniqueOrders.get(1).getQuantity());
        assertEquals(5, uniqueOrders.get(1).getNumberOfDays());
        assertEquals(1.0, (Object)uniqueOrders.get(1).getDosage());
    }
}

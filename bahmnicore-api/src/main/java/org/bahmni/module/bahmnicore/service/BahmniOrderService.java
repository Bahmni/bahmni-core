/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service;

import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.order.contract.BahmniOrder;

import java.util.List;

public interface BahmniOrderService{
    List<BahmniOrder> ordersForOrderType(String patientUuid, List<Concept> concepts, Integer numberOfVisits, List<String> obsIgnoreList, String orderTypeUuid, Boolean includeObs, List<String> locationUuids);

    List<BahmniOrder> ordersForOrderUuid(String patientUuid, List<Concept> concepts, List<String> obsIgnoreList, String orderUuid);

    List<BahmniOrder> ordersForVisit(String visitUuid, String orderTypeUuid, List<String> conceptNames, List<Concept> obsIgnoreList);

    Order getChildOrder(Order order);
}

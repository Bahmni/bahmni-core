/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service;

import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;

import java.util.List;

public interface OrderService {
    List<Order> getPendingOrders(String patientUuid, String orderTypeUuid);

    List<Order> getAllOrders(String patientUuid, String orderTypeUuid, Integer offset, Integer limit, List<String> locationUuids);

    List<Visit> getVisitsWithOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits);

    List<Order> getAllOrdersForVisits(String patientUuid, String orderType, Integer numberOfVisits);

    Order getOrderByUuid(String orderUuid);

    List<Order> getAllOrdersForVisitUuid(String visitUuid, String orderTypeUuid);

    Order getChildOrder(Order order);
}

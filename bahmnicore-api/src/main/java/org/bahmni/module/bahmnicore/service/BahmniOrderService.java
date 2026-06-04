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

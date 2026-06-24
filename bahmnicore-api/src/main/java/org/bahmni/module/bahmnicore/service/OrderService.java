package org.bahmni.module.bahmnicore.service;

import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;

import javax.validation.constraints.NotNull;
import java.util.List;

public interface OrderService {
    List<Order> getPendingOrders(String patientUuid, String orderTypeUuid);

    List<Order> getAllOrders(String patientUuid, String orderTypeUuid, Integer offset, Integer limit, List<String> locationUuids);

    List<Visit> getVisitsWithOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits);

    List<Order> getAllOrdersForVisits(String patientUuid, String orderType, Integer numberOfVisits);

    Order getOrderByUuid(String orderUuid);

    List<Order> getAllOrdersForVisitUuid(String visitUuid, String orderTypeUuid);

    Order getChildOrder(Order order);

    List<Order> getLabOrdersForPatient(@NotNull String patientUuid, @NotNull Integer numberOfVisits);


    List<Obs> getObsForOrders(List<Order> orders);
}

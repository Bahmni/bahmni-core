package org.bahmni.module.bahmnicore.dao;

import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface OrderDao {
    List<Order> getCompletedOrdersFrom(List<Order> orders);

    List<DrugOrder> getPrescribedDrugOrders(Patient patient, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly);

    List<Visit> getVisitsWithActiveOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits);

    List<Visit> getVisitsWithAllOrders(Patient patient, String orderType, Boolean includeActiveVisit, Integer numberOfVisits);

    List<DrugOrder> getPrescribedDrugOrders(List<String> visitUuids);

    List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, List<Visit> visits,
                                                       List<Concept> conceptIds, Date startDate, Date endDate);

    Collection<EncounterTransaction.DrugOrder> getDrugOrderForRegimen(String regimenName);

    List<Visit> getVisitsForUUids(String[] visitUuids);

    List<Order> getAllOrders(Patient patient, List<OrderType> orderTypes, Integer offset, Integer limit);

    List<Order> getAllOrdersForVisits(OrderType orderType, List<Visit> visits);

    Order getOrderByUuid(String orderUuid);

    List<Order> getOrdersForVisitUuid(String visitUuid, String orderTypeUuid);

    List<Order> getAllOrders(Patient patientByUuid, OrderType drugOrderTypeUuid, Set<Concept> conceptsForDrugs, Set<Concept> drugConceptsToBeExcluded, Collection<Encounter> encounters);

    Map<String,DrugOrder> getDiscontinuedDrugOrders(List<DrugOrder> drugOrders);

    List<Order> getActiveOrders(Patient patient, OrderType orderType, CareSetting careSetting, Date asOfDate, Set<Concept> conceptsToFilter,
                                Set<Concept> conceptsToExclude, Date startDate, Date endDate, Collection<Encounter> encounters);

    List<Order> getInactiveOrders(Patient patient, OrderType orderTypeByName, CareSetting careSettingByName, Date asOfDate,
                                  Set<Concept> concepts, Set<Concept> drugConceptsToBeExcluded, Collection<Encounter> encounters);

    Order getChildOrder(Order order);

    List<Order> getOrdersByPatientProgram(String patientProgramUuid, OrderType orderTypeByUuid, Set<Concept> conceptsForDrugs);

    List<Order> getAllOrders(Patient patientByUuid, OrderType drugOrderTypeUuid, Integer offset, Integer limit, List<String> locationUuids);
}

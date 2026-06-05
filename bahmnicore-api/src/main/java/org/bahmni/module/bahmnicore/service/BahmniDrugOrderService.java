package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.drugorder.DrugOrderConfigResponse;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BahmniDrugOrderService {
    List<DrugOrder> getActiveDrugOrders(String patientUuid, Date startDate, Date endDate);

    List<DrugOrder> getActiveDrugOrders(String patientUuid);

    List<DrugOrder> getPrescribedDrugOrders(List<String> visitUuids, String patientUuid, Boolean includeActiveVisit,
                                            Integer numberOfVisit, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly);

    List<DrugOrder> getPrescribedDrugOrdersForConcepts(Patient patient, Boolean includeActiveVisit, List<Visit> visits, List<Concept> concepts, Date startDate, Date endDate);

    DrugOrderConfigResponse getConfig();

    List<Order> getAllDrugOrders(String patientUuid, String patientProgramUuid, Set<Concept> conceptsForDrugs,
                                 Set<Concept> drugConceptsToBeExcluded, Collection<Encounter> encounters) throws ParseException;

    Map<String,DrugOrder> getDiscontinuedDrugOrders(List<DrugOrder> drugOrders);

    List<DrugOrder> getInactiveDrugOrders(String patientUuid, Set<Concept> concepts, Set<Concept> drugConceptsToBeExcluded,
                                          Collection<Encounter> encountersByPatientProgramUuid);

    List<BahmniDrugOrder> getDrugOrders(String patientUuid, Boolean isActive, Set<Concept> conceptsToFilter, Set<Concept> conceptsToExclude,
                                        String patientProgramUuid) throws ParseException;

    DrugOrder getDrugOrderByOrderId(String orderId);
}

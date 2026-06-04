package org.bahmni.module.bahmnicore.dao;

import org.bahmni.module.bahmnicore.dao.impl.ObsDaoImpl;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Person;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface ObsDao {
    List<Obs> getNumericObsByPerson(String personUUID);

    List<Concept> getNumericConceptsForPerson(String personUUID);

    List<Obs> getObsFor(String patientUuid, Concept rootConcept, Concept childConcept, List<Integer> visitIdsFor, Collection<Encounter> encounters, Date startDate, Date endDate);

    List<Obs> getObsForFormBuilderForms(String patientUuid, List<String> formNames, List<Integer> visitIdsFor, Collection<Encounter> encounters, Date startDate, Date endDate);

    List<Obs> getLatestObsFor(String patientUuid, String conceptName, Integer limit);

    List<Obs> getLatestObsForConceptSetByVisit(String patientUuid, String conceptNames, Integer visitId);

    List<Obs> getObsForOrder(String orderUuid);

    List<Obs> getObsForVisits(List<Person> persons, ArrayList<Encounter> visit, List<Concept> conceptsForNames, Collection<Concept> obsIgnoreList, Boolean filterOutOrders, Order order);

    List<Obs> getObsByPatientAndVisit(String patientUuid, List<String> conceptNames, List<Integer> listOfVisitIds, Integer limit, ObsDaoImpl.OrderBy sortOrder, List<String> obsIgnoreList, Boolean filterOutOrderObs, Order order, Date startDate, Date endDate);

    List<Obs> getObsForConceptsByEncounter(String encounterUuid, List<String> conceptNames);

    Obs getChildObsFromParent(String parentObsUuid, Concept childConcept);

    List<Obs> getObsByPatientProgramUuidAndConceptNames(String patientProgramUuid, List<String> conceptNames, Integer limit, ObsDaoImpl.OrderBy sortOrder, Date startDate, Date endDate);
}

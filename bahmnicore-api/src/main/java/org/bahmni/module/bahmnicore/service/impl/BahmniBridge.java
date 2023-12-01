package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.ObsDao;
import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.dao.impl.ObsDaoImpl;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.joda.time.LocalDate;
import org.joda.time.Years;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.OMRSObsToBahmniObsMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.mapper.OrderMapper1_12;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Bridge between extension scripts of Bahmni and Bahmni core as well as OpenMRS core.
 */
@Component
@Scope("prototype")
public class BahmniBridge {

    private String patientUuid;
    private String patientProgramUuid;
    private String visitUUid;

    private ObsDao obsDao;
    private PatientService patientService;
    private PersonService personService;
    private ConceptService conceptService;
    private OrderDao orderDao;
    private BahmniDrugOrderService bahmniDrugOrderService;
    private OMRSObsToBahmniObsMapper omrsObsToBahmniObsMapper;
    private BahmniConceptService bahmniConceptService;

    private OrderMapper1_12 drugOrderMapper = new OrderMapper1_12();
    /**
     * Factory method to construct objects of <code>BahmniBridge</code>.
     * This is provided so that <code>BahmniBridge</code> can be called by extensions without having to use the
     * Spring application context. Prefer using this as opposed to the constructor.
     *
     * @return instance of BahmniBridge registered with OpenMRS Context
     */
    public static BahmniBridge create() {
        return Context.getRegisteredComponents(BahmniBridge.class).iterator().next();
    }

    @Autowired
    public BahmniBridge(ObsDao obsDao, PatientService patientService, PersonService personService, ConceptService conceptService, OrderDao orderDao, BahmniDrugOrderService bahmniDrugOrderService, OMRSObsToBahmniObsMapper omrsObsToBahmniObsMapper, BahmniConceptService bahmniConceptService) {
        this.obsDao = obsDao;
        this.patientService = patientService;
        this.personService = personService;
        this.conceptService = conceptService;
        this.orderDao = orderDao;
        this.bahmniDrugOrderService = bahmniDrugOrderService;
        this.omrsObsToBahmniObsMapper = omrsObsToBahmniObsMapper;
        this.bahmniConceptService = bahmniConceptService;
    }

    /**
     * Set patient uuid. This will be used by methods that require the patient to perform its operations.
     * Setting patient uuid might be mandatory depending on the operation you intend to perform using the bridge.
     *
     * @param patientUuid uuid of the patient
     * @return reference to the instance
     */
    public BahmniBridge forPatient(String patientUuid) {
        this.patientUuid = patientUuid;
        return this;
    }

    /**
     * Set patient program uuid. This will be used by methods that require the patient to perform its operations associated with a specific program.
     * Setting patient program uuid might be mandatory depending on the operation you intend to perform using the bridge.
     *
     * @param patientProgramUuid patient's program uuid
     * @return reference to the instance
     */
    public BahmniBridge forPatientProgram(String patientProgramUuid) {
        this.patientProgramUuid = patientProgramUuid;
        return this;
    }

    /**
     * Set visit uuid. This will be used by methods that require a visit to perform its operations.
     * Setting visit uuid might be mandatory depending on the operation you intend to perform using the bridge.
     *
     * @param visitUuid visit uuid
     * @return reference to the instance
     */
    public BahmniBridge forVisit(String visitUuid) {
        this.visitUUid = visitUuid;
        return this;
    }

    /**
     * Retrieve last observation for <code>patientUuid</code> set using {@link org.bahmni.module.bahmnicore.service.impl.BahmniBridge#forPatient(String)}
     * for the given <code>conceptName</code>.
     *
     * @param conceptName concept name FSN in preferred language
     * @return latest obs captured for the concept
     */
    public Obs latestObs(String conceptName) {
        List<Obs> obsList;
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add(conceptName);
        if (patientProgramUuid != null) {
            obsList = obsDao.getObsByPatientProgramUuidAndConceptNames(patientProgramUuid, conceptNames, 1,  ObsDaoImpl.OrderBy.DESC, null, null);
        } else {
            obsList = obsDao.getLatestObsFor(patientUuid, conceptName, 1);
        }
        if (obsList.size() > 0) {
            return obsList.get(0);
        }
        return null;
    }

    /**
     * Retrieve age in years for <code>patientUuid</code> set using {@link org.bahmni.module.bahmnicore.service.impl.BahmniBridge#forPatient(String)}
     *
     * @param asOfDate date as of today
     * @return age in years
     */
    public Integer ageInYears(Date asOfDate) {
        Date birthdate = patientService.getPatientByUuid(patientUuid).getBirthdate();
        return Years.yearsBetween(new LocalDate(birthdate), new LocalDate(asOfDate)).getYears();

    }

    /**
     * Retrieve drug orders set for <code>regimenName</code>
     *
     * @param regimenName drug regimen name
     * @return list of DrugOrder
     */
    public Collection<EncounterTransaction.DrugOrder> drugOrdersForRegimen(String regimenName) {
        return orderDao.getDrugOrderForRegimen(regimenName);
    }

    /**
     * Retrieve active Drug orders for patientUuid
     *
     * @return List of Drug Order
     */
    public List<EncounterTransaction.DrugOrder> activeDrugOrdersForPatient() {
        List<DrugOrder> activeOpenMRSDrugOrders = bahmniDrugOrderService.getActiveDrugOrders(patientUuid);
        List<EncounterTransaction.DrugOrder> drugOrders = new ArrayList<>();
        for (DrugOrder activeOpenMRSDrugOrder : activeOpenMRSDrugOrders) {
            EncounterTransaction.DrugOrder drugOrder = drugOrderMapper.mapDrugOrder(activeOpenMRSDrugOrder);
            if ((isNotScheduled(drugOrder) || hasScheduledOrderBecameActive(drugOrder)) && isNotStopped(drugOrder)) {
                drugOrders.add(drugOrder);
            }
        }
        return drugOrders;
    }

    /**
     * Retrieve person attribute type for <code>attributeType</code>
     * @param attributeType name of the attribute
     * @return patient attribute def
     */
    public PersonAttributeType getPersonAttributeType(String attributeType) {
        return personService.getPersonAttributeTypeByName(attributeType);
    }

    /**
     * Retrieve concept for <code>conceptName</code>
     *
     * @return concept identified by name
     */
    public Concept getConcept(String conceptName) {
        return conceptService.getConceptByName(conceptName);
    }
    private boolean isNotScheduled(EncounterTransaction.DrugOrder drugOrder) {
        return drugOrder.getScheduledDate() == null;
    }

    private boolean isNotStopped(EncounterTransaction.DrugOrder drugOrder) {
        return drugOrder.getEffectiveStopDate() == null || drugOrder.getEffectiveStopDate().after(new Date());
    }

    private boolean hasScheduledOrderBecameActive(EncounterTransaction.DrugOrder drugOrder) {

        return drugOrder.getScheduledDate().before(new Date());
    }

    /**
     * Retrieve concept by FullySpecifiedName
     */

    public Concept getConceptByFullySpecifiedName(String conceptName) {
        return bahmniConceptService.getConceptByFullySpecifiedName(conceptName);
    }

    /**
     * Retrieve concept for <code>conceptName</code>
     *
     * @return start date of treatment
     */
    public Date getStartDateOfTreatment() throws ParseException {
        List<Order> allDrugOrders = bahmniDrugOrderService.getAllDrugOrders(patientUuid, null, null, null, null);

        sortOders(allDrugOrders);
        return allDrugOrders.get(0).getScheduledDate() !=null ? allDrugOrders.get(0).getScheduledDate() : allDrugOrders.get(0).getDateActivated();
    }

    private void sortOders(List<Order> drugOrders) {
        Collections.sort(drugOrders, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                if (o1.getDateActivated().before(o2.getDateActivated()))
                    return -1;
                else if (o1.getDateActivated().after(o2.getDateActivated()))
                    return 1;
                else
                    return 0;
            }
        });
    }

    public BahmniObservation getChildObsFromParentObs(String parentObsGroupUuid, String childConceptName){
        Concept childConcept = conceptService.getConceptByName(childConceptName);
        return omrsObsToBahmniObsMapper.map(obsDao.getChildObsFromParent(parentObsGroupUuid, childConcept), null);
    }

    public BahmniObservation getLatestBahmniObservationFor(String conceptName){
        Obs obs = latestObs(conceptName);
        if(obs != null) {
            return omrsObsToBahmniObsMapper.map(obs, null);
        }
        return null;
    }
}

package org.bahmni.module.bahmnicore.web.v1_0.controller;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.util.BahmniDateUtil;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniOrderAttribute;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniDrugOrderMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class BahmniDrugOrderController extends BaseRestController {

    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/drugOrders";
    @Autowired
    private BahmniDrugOrderService drugOrderService;

    @Autowired
    private BahmniObsService bahmniObsService;

    @Autowired
    private ConceptService conceptService;

    private static Logger logger = LogManager.getLogger(BahmniDrugOrderController.class);

    private BahmniDrugOrderMapper bahmniDrugOrderMapper;

    public BahmniDrugOrderController(BahmniDrugOrderService drugOrderService) {
        this.drugOrderService = drugOrderService;
        this.bahmniDrugOrderMapper = new BahmniDrugOrderMapper();
    }
    public BahmniDrugOrderController() {
        this.bahmniDrugOrderMapper = new BahmniDrugOrderMapper();
    }


    //TODO: Active orders are available in OMRS 1.10.x. Consider moving once we upgrade OpenMRS.
    @RequestMapping(value = baseUrl + "/active", method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getActiveDrugOrders(@RequestParam(value = "patientUuid") String patientUuid,
                                                     @RequestParam(value = "startDate", required = false) String startDateStr,
                                                     @RequestParam(value = "endDate", required = false) String endDateStr) throws ParseException {
        logger.info("Retrieving active drug orders for patient with uuid {}", patientUuid);
        Date startDate = BahmniDateUtil.convertToDate(startDateStr, BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate(endDateStr, BahmniDateUtil.DateFormatType.UTC);
        return getActiveOrders(patientUuid, startDate, endDate);
    }

    @RequestMapping(value = baseUrl + "/prescribedAndActive", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Collection<BahmniDrugOrder>> getVisitWisePrescribedAndOtherActiveOrders(
                            @RequestParam(value = "patientUuid") String patientUuid,
                            @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
                            @RequestParam(value = "getOtherActive", required = false) Boolean getOtherActive,
                            @RequestParam(value = "visitUuids", required = false) List visitUuids,
                            @RequestParam(value = "startDate", required = false) String startDateStr,
                            @RequestParam(value = "endDate", required = false) String endDateStr,
                            @RequestParam(value = "getEffectiveOrdersOnly", required = false) Boolean getEffectiveOrdersOnly) throws ParseException {

        Map<String, Collection<BahmniDrugOrder>> visitWiseOrders = new HashMap<>();
        Date startDate = BahmniDateUtil.convertToDate(startDateStr, BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate(endDateStr, BahmniDateUtil.DateFormatType.UTC);

        List<BahmniDrugOrder> prescribedOrders = getPrescribedOrders(visitUuids, patientUuid, true, numberOfVisits, startDate, endDate, Boolean.TRUE.equals(getEffectiveOrdersOnly));
        visitWiseOrders.put("visitDrugOrders", prescribedOrders);

        if (Boolean.TRUE.equals(getOtherActive)) {
            List<BahmniDrugOrder> activeDrugOrders = getActiveOrders(patientUuid, null, null);
            activeDrugOrders.removeAll(prescribedOrders);
            visitWiseOrders.put("Other Active DrugOrders", activeDrugOrders);
        }

        return visitWiseOrders;
    }

    @RequestMapping(value = baseUrl, method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getPrescribedDrugOrders(@RequestParam(value = "patientUuid") String patientUuid,
                                                         @RequestParam(value = "includeActiveVisit", required = false) Boolean includeActiveVisit,
                                                         @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
                                                         @RequestParam(value = "startDate", required = false) String startDateStr,
                                                         @RequestParam(value = "endDate", required = false) String endDateStr ) throws ParseException {

        Date startDate = BahmniDateUtil.convertToDate(startDateStr, BahmniDateUtil.DateFormatType.UTC);
        Date endDate = BahmniDateUtil.convertToDate(endDateStr, BahmniDateUtil.DateFormatType.UTC);


        return getPrescribedOrders(null, patientUuid, includeActiveVisit, numberOfVisits, startDate, endDate, false);
    }


    @RequestMapping(value = baseUrl + "/drugOrderDetails", method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniDrugOrder> getDrugOrderDetails(@RequestParam(value = "patientUuid") String patientUuid,
                                                                      @RequestParam(value = "isActive", required = false) Boolean isActive,
                                                                      @RequestParam(value = "includeConceptSet", required = false) String drugConceptSetNameToBeFiltered,
                                                                      @RequestParam(value = "excludeConceptSet", required = false) String drugConceptSetNameToBeExcluded,
                                                                      @RequestParam(value = "patientProgramUuid", required = false) String patientProgramUuid) throws ParseException {
        Set<Concept> drugConceptsToBeFiltered = getDrugConcepts(drugConceptSetNameToBeFiltered);
        Set<Concept> drugConceptsToBeExcluded = getDrugConcepts(drugConceptSetNameToBeExcluded);
        return drugOrderService.getDrugOrders(patientUuid, isActive, drugConceptsToBeFiltered, drugConceptsToBeExcluded, patientProgramUuid);
    }

    @RequestMapping(value = baseUrl + "/{orderId}", method = RequestMethod.GET)
    @ResponseBody
    public BahmniDrugOrder getDrugOrderByOrderId(@PathVariable String orderId) {
        DrugOrder drugOrder = drugOrderService.getDrugOrderByOrderId(orderId);
        Map<String, DrugOrder> discontinuedDrugOrderMap = drugOrderService.getDiscontinuedDrugOrders(Collections.singletonList(drugOrder));
        if (drugOrder == null) {
            throw new ResourceNotFoundException("Drug order not found with orderId: " + orderId);
        }
        return bahmniDrugOrderMapper.mapToResponse(drugOrder, discontinuedDrugOrderMap);
    }

    Set<Concept> getDrugConcepts(String drugConceptSetName){
        if(drugConceptSetName == null) return  null;
        Set<Concept> drugConcepts = new HashSet<>();
        Concept concept = conceptService.getConceptByName(drugConceptSetName);
        if(concept == null) return  null;
        populateDrugConcepts(concept, drugConcepts);
        return drugConcepts;
    }

    private void populateDrugConcepts(Concept concept, Set<Concept> drugConcepts) {
        if (concept.isSet()) {
            for (Concept conceptSetMember : concept.getSetMembers()) {
                populateDrugConcepts(conceptSetMember, drugConcepts);
            }
        } else {
            drugConcepts.add(concept);
        }
    }

    private Collection<Concept> getOrdAttributeConcepts() {
        Concept orderAttribute = conceptService.getConceptByName(BahmniOrderAttribute.ORDER_ATTRIBUTES_CONCEPT_SET_NAME);
        return orderAttribute == null ? Collections.EMPTY_LIST : orderAttribute.getSetMembers();
    }

    private List<BahmniDrugOrder> getActiveOrders(String patientUuid, Date startDate, Date endDate) {
        List<DrugOrder> activeDrugOrders = drugOrderService.getActiveDrugOrders(patientUuid, startDate, endDate);
        logger.info("{} active drug orders found", activeDrugOrders.size());
        return getBahmniDrugOrders(patientUuid,activeDrugOrders);
    }

    private List<BahmniDrugOrder> getPrescribedOrders(List<String> visitUuids, String patientUuid, Boolean includeActiveVisit, Integer numberOfVisits, Date startDate, Date endDate, Boolean getEffectiveOrdersOnly) {
        List<DrugOrder> prescribedDrugOrders = drugOrderService.getPrescribedDrugOrders(visitUuids, patientUuid, includeActiveVisit, numberOfVisits, startDate, endDate, getEffectiveOrdersOnly);
        logger.info("prescribed drug orders found {}", prescribedDrugOrders.size());
        return getBahmniDrugOrders(patientUuid, prescribedDrugOrders);
    }

    private List<BahmniDrugOrder> getBahmniDrugOrders(String patientUuid, List<DrugOrder> drugOrders) {
        Map<String, DrugOrder> drugOrderMap = drugOrderService.getDiscontinuedDrugOrders(drugOrders);
        try {
            Collection<BahmniObservation> orderAttributeObs = bahmniObsService.observationsFor(patientUuid, getOrdAttributeConcepts(), null, null, false, null, null, null);
            List<BahmniDrugOrder> bahmniDrugOrders = bahmniDrugOrderMapper.mapToResponse(drugOrders, orderAttributeObs, drugOrderMap , null);
            return sortDrugOrdersAccordingToTheirSortWeight(bahmniDrugOrders);
        } catch (IOException e) {
            logger.error("Could not parse drug order", e);
            throw new RuntimeException("Could not parse drug order", e);
        }
    }

    private List<BahmniDrugOrder> sortDrugOrdersAccordingToTheirSortWeight(List<BahmniDrugOrder> bahmniDrugOrders) {
        Map<String, ArrayList<BahmniDrugOrder>> bahmniDrugOrderMap = groupDrugOrdersAccordingToOrderSet(bahmniDrugOrders);
        List<BahmniDrugOrder> sortDrugOrders = new ArrayList<>();
        for (String key : bahmniDrugOrderMap.keySet()) {
            if(key == null) {
                continue;
            }
            List<BahmniDrugOrder> bahmniDrugOrder = bahmniDrugOrderMap.get(key);
            Collections.sort(bahmniDrugOrder, new Comparator<BahmniDrugOrder>() {
                @Override
                public int compare(BahmniDrugOrder o1, BahmniDrugOrder o2) {
                    return o1.getSortWeight().compareTo(o2.getSortWeight());
                }
            });
        }

        for (String s : bahmniDrugOrderMap.keySet()) {
            sortDrugOrders.addAll(bahmniDrugOrderMap.get(s));
        }
        return sortDrugOrders;
    }

    private Map<String, ArrayList<BahmniDrugOrder>> groupDrugOrdersAccordingToOrderSet(List<BahmniDrugOrder> bahmniDrugOrders) {
        Map<String, ArrayList<BahmniDrugOrder>> groupedDrugOrders = new LinkedHashMap<>();

        for (BahmniDrugOrder bahmniDrugOrder: bahmniDrugOrders) {
            String orderSetUuid = null == bahmniDrugOrder.getOrderGroup() ? null : bahmniDrugOrder.getOrderGroup().getOrderSet().getUuid();

            if(!groupedDrugOrders.containsKey(orderSetUuid)){
                groupedDrugOrders.put(orderSetUuid, new ArrayList<BahmniDrugOrder>());
            }

            groupedDrugOrders.get(orderSetUuid).add(bahmniDrugOrder);
        }

        return groupedDrugOrders;
    }
}

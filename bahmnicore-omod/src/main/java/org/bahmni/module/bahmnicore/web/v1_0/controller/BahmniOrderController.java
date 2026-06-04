package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.BahmniOrderService;
import org.bahmni.module.bahmnicore.util.MiscUtils;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.order.contract.BahmniOrder;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/orders")
public class BahmniOrderController extends BaseRestController {
    private ConceptService conceptService;
    private BahmniOrderService bahmniOrderService;

    @Autowired
    public BahmniOrderController(ConceptService conceptService, BahmniOrderService bahmniOrderService) {
        this.conceptService = conceptService;
        this.bahmniOrderService = bahmniOrderService;
    }


    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniOrder> get(@RequestParam(value = "patientUuid", required = true) String patientUuid,
                                 @RequestParam(value = "concept", required = false) List<String> rootConceptNames,
                                 @RequestParam(value = "orderTypeUuid", required = false) String orderTypeUuid,
                                 @RequestParam(value = "visitUuid", required = false) String visitUuid,
                                 @RequestParam(value = "orderUuid", required = false) String orderUuid,
                                 @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
                                 @RequestParam(value = "obsIgnoreList", required = false) List<String> obsIgnoreList,
                                 @RequestParam(value = "includeObs", required = false, defaultValue ="true") boolean includeObs,
                                 @RequestParam(value = "locationUuids", required = false) List<String> locationUuids) {


        if (visitUuid != null) {
            return bahmniOrderService.ordersForVisit(visitUuid, orderTypeUuid, rootConceptNames, MiscUtils.getConceptsForNames(obsIgnoreList, conceptService));
        }

        List<Concept> rootConcepts = getConcepts(rootConceptNames);
        if (orderUuid != null) {
            return bahmniOrderService.ordersForOrderUuid(patientUuid, rootConcepts, obsIgnoreList, orderUuid);
        }
        else {
            return  bahmniOrderService.ordersForOrderType(patientUuid, rootConcepts, numberOfVisits, obsIgnoreList, orderTypeUuid, includeObs, locationUuids);
        }

    }

    private List<Concept> getConcepts(List<String> rootConceptNames) {
        List<Concept> rootConcepts = new ArrayList<>();
        if(rootConceptNames!=null) {
            for (String rootConceptName : rootConceptNames) {
                rootConcepts.add(conceptService.getConceptByName(rootConceptName));
            }
        }
        return rootConcepts;
    }
}

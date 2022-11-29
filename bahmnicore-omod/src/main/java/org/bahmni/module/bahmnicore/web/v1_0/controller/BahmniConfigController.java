package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bahmni.module.admin.config.model.BahmniConfig;
import org.bahmni.module.admin.config.service.BahmniConfigService;
import org.bahmni.module.bahmnicore.contract.drugorder.DrugOrderConfigResponse;
import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.contract.encounter.response.EncounterConfigResponse;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicommons.api.contract.patient.response.PatientConfigResponse;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.OrderType;
import org.openmrs.VisitType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderService;
import org.openmrs.api.VisitService;
import org.openmrs.module.rulesengine.engine.RulesEngine;
import org.openmrs.module.rulesengine.engine.RulesEngineImpl;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/config")
public class BahmniConfigController extends BaseRestController {

    @Autowired
    private BahmniConfigService bahmniConfigService;
    @Autowired
    private BahmniPatientService bahmniPatientService;
    @Autowired
    private BahmniDrugOrderService drugOrderService;
    @Autowired
    private VisitService visitService;
    @Autowired
    private EncounterService encounterService;
    @Autowired
    private ConceptService conceptService;
    @Autowired
    private OrderService orderService;

    private RulesEngine rulesEngine = new RulesEngineImpl();

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public BahmniConfig get(@RequestParam("appName") String appName, @RequestParam(value = "configName") String configName) {
        return bahmniConfigService.get(appName, configName);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{appName}/{configName:.+}")
    @ResponseBody
    public String getConfig(@PathVariable("appName") String appName, @PathVariable(value = "configName") String configName) {
        return StringEscapeUtils.unescapeJava(bahmniConfigService.get(appName, configName).getConfig());
    }

    @RequestMapping(method = RequestMethod.GET, value = "all")
    @ResponseBody
    public List<BahmniConfig> getAll(@RequestParam("appName") String appName) {
        return bahmniConfigService.getAllFor(appName);
    }

    @RequestMapping(method = RequestMethod.GET, value = "allApps")
    @ResponseBody
    public List<String> getAll() {
        return bahmniConfigService.getAll();
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public BahmniConfig insert(@RequestBody BahmniConfig bahmniConfig) {
        return bahmniConfigService.save(bahmniConfig);
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public BahmniConfig update(@RequestBody BahmniConfig bahmniConfig) {
        return bahmniConfigService.update(bahmniConfig);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/patient")
    @ResponseBody
    public PatientConfigResponse getPatientConfig() {
        return bahmniPatientService.getConfig();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/drugOrders")
    @ResponseBody
    public DrugOrderConfigResponse getDrugOrderConfig() throws Exception {
        String[] ruleNames=rulesEngine.getRuleNames();
        DrugOrderConfigResponse configResponse=drugOrderService.getConfig();
        configResponse.setDosingRules(ruleNames);
        return configResponse;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/bahmniencounter")
    @ResponseBody
    public EncounterConfigResponse getConfig(@RequestParam("callerContext")String callerContext) {
        EncounterConfigResponse encounterConfigResponse = new EncounterConfigResponse();
        List<VisitType> visitTypes = visitService.getAllVisitTypes();
        for (VisitType visitType : visitTypes) {
            if (!visitType.isRetired()) {
                encounterConfigResponse.addVisitType(visitType.getName(), visitType.getUuid());
            }
        }
        List<EncounterType> allEncounterTypes = encounterService.getAllEncounterTypes(false);
        for (EncounterType encounterType : allEncounterTypes) {
            encounterConfigResponse.addEncounterType(encounterType.getName(), encounterType.getUuid());
        }
        Concept conceptSetConcept = conceptService.getConcept(callerContext);
        if (conceptSetConcept != null) {
            List<Concept> conceptsByConceptSet = conceptService.getConceptsByConceptSet(conceptSetConcept);
            for (Concept concept : conceptsByConceptSet) {
                ConceptData conceptData = new ConceptData(concept.getUuid(), concept.getName().getName());
                encounterConfigResponse.addConcept(concept.getName().getName(), conceptData);
            }
        }
        List<OrderType> orderTypes = orderService.getOrderTypes(true);
        for (OrderType orderType : orderTypes) {
            encounterConfigResponse.addOrderType(orderType.getName(), orderType.getUuid());
        }
        return encounterConfigResponse;
    }


}

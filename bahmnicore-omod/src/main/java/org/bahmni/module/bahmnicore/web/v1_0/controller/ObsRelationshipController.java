package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.obsrelationship.api.ObsRelationService;
import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.ObsRelationshipMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/obsrelationships")
public class ObsRelationshipController extends BaseRestController{

    @Autowired
    private ObsRelationService obsRelationService;
    @Autowired
    private ObsRelationshipMapper obsRelationshipMapper;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<BahmniObservation> find(@RequestParam(value = "targetObsUuid", required = true) String targetObsUuid){
        List<ObsRelationship> obsRelationships = obsRelationService.getObsRelationshipsByTargetObsUuid(targetObsUuid);

        return obsRelationshipMapper.map(obsRelationships);
    }
}

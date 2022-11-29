package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/visitLocation")
public class BahmniVisitLocationController extends BaseRestController {

    private BahmniVisitLocationService bahmniVisitLocationService;

    @Autowired
    public BahmniVisitLocationController(BahmniVisitLocationService bahmniVisitLocationService) {
        this.bahmniVisitLocationService = bahmniVisitLocationService;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/{loginLocationUuid}")
    @ResponseBody
    public HashMap<String, String> getVisitLocationInfo(@PathVariable("loginLocationUuid") String locationUuid ) {
        HashMap<String, String> visitLocation = new HashMap<>();
        visitLocation.put("uuid",bahmniVisitLocationService.getVisitLocationUuid(locationUuid));
        return visitLocation;
    }

}

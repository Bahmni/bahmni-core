package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicommons.api.visitlocation.BahmniVisitLocationService;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
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
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore")
public class BahmniVisitLocationController extends BaseRestController {

    private BahmniVisitLocationService bahmniVisitLocationService;

    @Autowired
    public BahmniVisitLocationController(BahmniVisitLocationService bahmniVisitLocationService) {
        this.bahmniVisitLocationService = bahmniVisitLocationService;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/visitLocation/{loginLocationUuid}")
    @ResponseBody
    public HashMap<String, String> getVisitLocationInfo(@PathVariable("loginLocationUuid") String locationUuid ) {
        HashMap<String, String> visitLocation = new HashMap<>();
        visitLocation.put("uuid",bahmniVisitLocationService.getVisitLocationUuid(locationUuid));
        return visitLocation;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/facilityLocation/{locationUuid}")
    @ResponseBody
    public HashMap<String, String> getFacilityVisitLocationInfo(@PathVariable("locationUuid") String locationUuid ) {
        Location location = Context.getLocationService().getLocationByUuid(locationUuid);
        HashMap<String, String> facilityVisitLocation = new HashMap<>();
        Location facilityLocation = getParentVisitLocationUuid(location);
        facilityVisitLocation.put("uuid", facilityLocation!=null ? facilityLocation.getUuid() : null);
        facilityVisitLocation.put("name", facilityLocation!=null ? facilityLocation.getName() : null);
        return facilityVisitLocation;
    }

    private Location getParentVisitLocationUuid(Location location) {
        if(isVisitLocation(location)) {
            return location.getParentLocation() != null ? getParentVisitLocationUuid(location.getParentLocation()) : location;
        } else {
            return location.getParentLocation() != null ? getParentVisitLocationUuid(location.getParentLocation()) : null;
        }
    }

    private Boolean isVisitLocation(Location location) {
        return (location.getTags().size() > 0 && location.getTags().stream().filter(tag -> tag.getName().equalsIgnoreCase("Visit Location")).count() != 0);
    }

}

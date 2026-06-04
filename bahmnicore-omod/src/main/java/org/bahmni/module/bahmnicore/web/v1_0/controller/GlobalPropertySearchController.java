package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/globalProperty")
public class GlobalPropertySearchController extends BaseRestController {

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    @RequestMapping(method = RequestMethod.GET, value = "passwordPolicyProperties")
    @ResponseBody
    public ResponseEntity<HashMap<String, String>> getPasswordPolicies() {
        List<GlobalProperty> allGlobalProperties = administrationService.getAllGlobalProperties();
        HashMap<String, String> passwordPolicyProperties = new HashMap<>();

        allGlobalProperties.forEach(globalProperty -> {
            if (globalProperty.getProperty().contains("security.")) {
                passwordPolicyProperties.put(globalProperty.getProperty(), globalProperty.getPropertyValue());
            }
        });

        return new ResponseEntity<>(passwordPolicyProperties, HttpStatus.OK);
    }

}

package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.SqlSearchService;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping(method = RequestMethod.GET, value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/sql")
public class SqlSearchController extends BaseRestController {

    @Autowired
    private SqlSearchService sqlSearchService;

    @Autowired
    @Qualifier("adminService")
    AdministrationService administrationService;

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<SimpleObject> search(@RequestParam("q") String query, HttpServletRequest request) throws Exception {
        return sqlSearchService.search(query, request.getParameterMap());
    }

    @RequestMapping(method = RequestMethod.GET, value = "globalproperty")
    @ResponseBody
    public Object retrieve(@RequestParam(value = "property", required = true) String name) {
        return administrationService.getGlobalProperty(name);
    }

}

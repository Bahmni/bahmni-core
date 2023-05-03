package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.service.TsConceptSearchService;
import org.bahmni.module.bahmnicore.service.impl.TsConceptSearchServiceImpl;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.web.bind.annotation.ValueConstants.DEFAULT_NONE;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmni/terminologies")
public class BahmniConceptSearchController extends BaseRestController {

    private TsConceptSearchService tsConceptSearchService;



    @Autowired
    public BahmniConceptSearchController(TsConceptSearchService tsConceptSearchService) {
        this.tsConceptSearchService = tsConceptSearchService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "concepts")
    @ResponseBody
    public Object search(@RequestParam("term") String query, @RequestParam Integer limit,
                         @RequestParam(required = false, defaultValue = DEFAULT_NONE) String locale) throws Exception {
      return tsConceptSearchService.getConcepts(query, limit, locale);
    }
}

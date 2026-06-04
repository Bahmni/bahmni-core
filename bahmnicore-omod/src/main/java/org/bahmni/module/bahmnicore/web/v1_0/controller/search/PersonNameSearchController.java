package org.bahmni.module.bahmnicore.web.v1_0.controller.search;

import org.bahmni.module.bahmnicore.dao.PersonNameDao;
import org.bahmni.module.bahmnicore.model.ResultList;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.WSDoc;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/search/personname")
public class PersonNameSearchController extends BaseRestController {
	
	private PersonNameDao namesDao;
	
	@Autowired
	public PersonNameSearchController(PersonNameDao namesDao) {
		this.namesDao = namesDao;
	}
	
	@RequestMapping(method = RequestMethod.GET, params = { "q", "key" })
	@WSDoc("Returns unique patient attributes for the given key that match the query term")
	public ResultList searchFor(@RequestParam String q, @RequestParam String key) {
		return namesDao.getUnique(key, q);
	}
}
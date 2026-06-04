package org.bahmni.module.referencedata.web.controller;

import org.bahmni.module.referencedata.labconcepts.contract.Department;
import org.bahmni.module.referencedata.labconcepts.mapper.DepartmentMapper;
import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.encounter.exception.ConceptNotFoundException;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/rest/v1/reference-data/department")
public class DepartmentController extends BaseRestController {
    private ConceptService conceptService;
    private final DepartmentMapper departmentMapper;

    @Autowired
    public DepartmentController(ConceptService conceptService) {
        departmentMapper = new DepartmentMapper();
        this.conceptService = conceptService;
    }

    @RequestMapping(value = "/{uuid}", method = RequestMethod.GET)
    @ResponseBody
    public Department getDepartment(@PathVariable("uuid") String uuid) {
        final Concept department = conceptService.getConceptByUuid(uuid);
        if (department == null) {
            throw new ConceptNotFoundException("No department concept found with uuid " + uuid);
        }
        return departmentMapper.map(department);
    }
}

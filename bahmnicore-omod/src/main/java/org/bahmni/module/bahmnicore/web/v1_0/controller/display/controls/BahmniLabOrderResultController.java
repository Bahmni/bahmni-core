package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;
import org.openmrs.module.bahmniemrapi.laborder.service.LabOrderResultsService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/labOrderResults")
public class BahmniLabOrderResultController extends BaseRestController{
    private PatientService patientService;
    private OrderDao orderDao;
    private LabOrderResultsService labOrderResultsService;

    @Autowired
    public BahmniLabOrderResultController(PatientService patientService,
                                          OrderDao orderDao,
                                          LabOrderResultsService labOrderResultsService) {
        this.patientService = patientService;
        this.orderDao = orderDao;
        this.labOrderResultsService = labOrderResultsService;
    }

    @RequestMapping(method = RequestMethod.GET, params = {"visitUuids"})
    @ResponseBody
    public LabOrderResults getForVisitUuids(
            @RequestParam(value = "visitUuids", required = true) String[] visitUuids) {
        List<Visit> visits = orderDao.getVisitsForUUids(visitUuids);
        return labOrderResultsService.getAll(patientFrom(visits), visits, Integer.MAX_VALUE);
    }

    @RequestMapping(method = RequestMethod.GET, params = {"patientUuid"})
    @ResponseBody
    public LabOrderResults getForPatient(
            @RequestParam(value = "patientUuid", required = true) String patientUuid,
            @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
            @RequestParam(value = "numberOfAccessions", required = false) Integer numberOfAccessions) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        List<Visit> visits = null;
        if (numberOfVisits != null) {
            visits = orderDao.getVisitsWithAllOrders(patient, "Order", true, numberOfVisits);
        }
        if (numberOfAccessions == null)
            numberOfAccessions = Integer.MAX_VALUE;

        return labOrderResultsService.getAll(patient, visits, numberOfAccessions);
    }

    @RequestMapping(method = RequestMethod.GET, params = {"patient"})
    @ResponseBody
    public LabOrderResults getResultsForPatient(
            @RequestParam(value = "patient", required = true) String patient,
            @RequestParam(value = "numberOfVisits", required = false) Integer numberOfVisits,
            @RequestParam(value = "numberOfAccessions", required = false) Integer numberOfAccessions) {
        return findOrderService().map(orderService -> {
                    List<Order> orders = orderService.getLabOrdersForPatient(patient, numberOfVisits);
                    List<Obs> obsList = orderService.getObsForOrders(orders);
                    List<LabOrderResult> results = labOrderResultsService.resultsForOrders(orders, obsList, Optional.ofNullable(numberOfAccessions).orElse(Integer.MAX_VALUE));
                    return new LabOrderResults(results);
                }).orElse(new LabOrderResults(Collections.emptyList()));
    }

    private Optional<OrderService> findOrderService() {
        return Context.getRegisteredComponents(OrderService.class).stream().findAny();
    }

    private Patient patientFrom(List<Visit> visits) {
        return visits.get(0).getPatient();
    }
}

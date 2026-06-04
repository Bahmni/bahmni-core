package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.contract.visit.VisitSummary;
import org.bahmni.module.bahmnicore.mapper.BahmniVisitSummaryMapper;
import org.bahmni.module.bahmnicore.service.BahmniVisitService;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.VisitAttribute;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

import static org.bahmni.module.bahmnicore.util.MiscUtils.setUuidsForObservations;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/visit")
public class BahmniVisitController extends BaseRestController {

    private static final String VISIT_STATUS_ATTRIBUTE_TYPE = "Visit Status";
    private static final String IPD_VISIT_STATUS = "IPD";
    private VisitService visitService;
    private BahmniVisitService bahmniVisitService;
    private BahmniVisitSummaryMapper bahmniVisitSummaryMapper;
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    public BahmniVisitController() {
    }

    @Autowired
    public BahmniVisitController(VisitService visitService, BahmniVisitService bahmniVisitService, BahmniEncounterTransactionService bahmniEncounterTransactionService) {
        this.visitService = visitService;
        this.bahmniVisitService = bahmniVisitService;
        this.bahmniEncounterTransactionService = bahmniEncounterTransactionService;
        this.bahmniVisitSummaryMapper = new BahmniVisitSummaryMapper();
    }

    @RequestMapping(method = RequestMethod.POST, value = "endVisit")
    @ResponseBody
    public VisitSummary endVisitNow(@RequestParam(value = "visitUuid") String visitUuid) {
        Visit visit = endVisit(visitUuid);
        return bahmniVisitSummaryMapper.map(visit, new ArrayList<Encounter>());
    }

    private Visit endVisit(String visitUuid) {
        Visit visit = visitService.getVisitByUuid(visitUuid);
        return visitService.endVisit(visit, null);
    }

    @RequestMapping(method = RequestMethod.GET, value = "summary")
    @ResponseBody
    public VisitSummary getVisitInfo(@RequestParam(value = "visitUuid") String visitUuid) {
        Visit visit = bahmniVisitService.getVisitSummary(visitUuid);
        if (visit != null) {
            List<Encounter> admitAndDischargeEncounters = null;
            for (VisitAttribute visitAttribute : visit.getAttributes()) {
                if (VISIT_STATUS_ATTRIBUTE_TYPE.equalsIgnoreCase(visitAttribute.getAttributeType().getName()) && IPD_VISIT_STATUS.equalsIgnoreCase(visitAttribute.getValueReference())) {
                    admitAndDischargeEncounters = bahmniVisitService.getAdmitAndDischargeEncounters(visit.getId());
                }
            }
            return bahmniVisitSummaryMapper.map(visit, admitAndDischargeEncounters);
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.POST, value = "endVisitAndCreateEncounter")
    @ResponseBody
    @Transactional
    public BahmniEncounterTransaction endVisitAndCreateNewEncounter(@RequestParam(value = "visitUuid") String visitUuid, @RequestBody BahmniEncounterTransaction bahmniEncounterTransaction) {
        endVisit(visitUuid);
        setUuidsForObservations(bahmniEncounterTransaction.getObservations());
        return bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
    }
}

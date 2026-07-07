package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bahmni.module.bahmnicore.web.v1_0.VisitClosedException;
import org.openmrs.Encounter;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.OrderEntryException;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterSearchParameters;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.BahmniEncounterTransactionMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.EmrEncounterService;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.bahmni.module.bahmnicore.util.MiscUtils.setUuidsForObservations;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/bahmniencounter")
public class BahmniEncounterController extends BaseRestController {
    private EncounterService encounterService;
    private EmrEncounterService emrEncounterService;
    private EncounterTransactionMapper encounterTransactionMapper;
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;
    private BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper;
    private static Logger logger = LogManager.getLogger(BahmniEncounterController.class);

    public BahmniEncounterController() {
    }

    @Autowired
    public BahmniEncounterController(EncounterService encounterService,
                                     EmrEncounterService emrEncounterService, EncounterTransactionMapper encounterTransactionMapper,
                                     BahmniEncounterTransactionService bahmniEncounterTransactionService,
                                     BahmniEncounterTransactionMapper bahmniEncounterTransactionMapper) {
        this.encounterService = encounterService;
        this.emrEncounterService = emrEncounterService;
        this.encounterTransactionMapper = encounterTransactionMapper;
        this.bahmniEncounterTransactionService = bahmniEncounterTransactionService;
        this.bahmniEncounterTransactionMapper = bahmniEncounterTransactionMapper;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/{uuid}")
    @ResponseBody
    public BahmniEncounterTransaction get(@PathVariable("uuid") String uuid, @RequestParam(value = "includeAll", required = false) Boolean includeAll) {
        EncounterTransaction encounterTransaction = emrEncounterService.getEncounterTransaction(uuid, includeAll);
        return bahmniEncounterTransactionMapper.map(encounterTransaction, includeAll);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/find")
    @ResponseBody
    public BahmniEncounterTransaction find(@RequestBody BahmniEncounterSearchParameters encounterSearchParameters) {
        EncounterTransaction encounterTransaction = bahmniEncounterTransactionService.find(encounterSearchParameters);

        if (encounterTransaction != null) {
            return bahmniEncounterTransactionMapper.map(encounterTransaction, encounterSearchParameters.getIncludeAll());
        } else {
            return bahmniEncounterTransactionMapper.map(new EncounterTransaction(), false);
        }
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{uuid}")
    @ResponseBody
    public void delete(@PathVariable("uuid") String uuid, @RequestParam(value = "reason", defaultValue = "web service call") String reason){
        String errorMessage = "Visit for this patient is closed. You cannot do an 'Undo Discharge' for the patient.";
        Visit visit = encounterService.getEncounterByUuid(uuid).getVisit();
        Date stopDate = visit.getStopDatetime();
        if(stopDate != null && stopDate.before(new Date())){
            throw new VisitClosedException(errorMessage);
        }
        else{
            BahmniEncounterTransaction bahmniEncounterTransaction = get(uuid,false);
            bahmniEncounterTransaction.setReason(reason);
            bahmniEncounterTransactionService.delete(bahmniEncounterTransaction);
        }
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    @Transactional
    public BahmniEncounterTransaction update(@RequestBody BahmniEncounterTransaction bahmniEncounterTransaction) {
        setUuidsForObservations(bahmniEncounterTransaction.getObservations());
        return bahmniEncounterTransactionService.save(bahmniEncounterTransaction);
    }

    public BahmniEncounterTransaction get(String encounterUuid) {
        Encounter encounter = encounterService.getEncounterByUuid(encounterUuid);
        boolean includeAll = false;
        EncounterTransaction encounterTransaction = encounterTransactionMapper.map(encounter, includeAll);
        return bahmniEncounterTransactionMapper.map(encounterTransaction, includeAll);
    }
    @ExceptionHandler(OrderEntryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleOrderEntryException(OrderEntryException ex) {
        Map<String, String> errorBody = new HashMap<>();
        errorBody.put("message", "[" + ex.getMessage() + "]");
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", errorBody);
        logger.warn("OrderEntryException: " + ex.getMessage());
        return new ResponseEntity<Object>(responseBody, HttpStatus.BAD_REQUEST);
    }

}

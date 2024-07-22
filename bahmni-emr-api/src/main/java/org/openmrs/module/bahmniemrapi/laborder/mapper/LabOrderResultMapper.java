package org.openmrs.module.bahmniemrapi.laborder.mapper;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
public class LabOrderResultMapper {
    private static final Log log = LogFactory.getLog(LabOrderResultMapper.class);
    private static final String EMPTY_STRING = "";
    public static final String LAB_RESULT = "LAB_RESULT";
    public static final String LAB_ABNORMAL = "LAB_ABNORMAL";
    public static final String LAB_MINNORMAL = "LAB_MINNORMAL";
    public static final String LAB_MAXNORMAL = "LAB_MAXNORMAL";
    public static final String LAB_NOTES = "LAB_NOTES";
    public static final String LABRESULTS_CONCEPT = "LABRESULTS_CONCEPT";
    private static final String REFERRED_OUT = "REFERRED_OUT";
    public static final String LAB_REPORT = "LAB_REPORT";
    private ConceptService conceptService;

    @Autowired
    public LabOrderResultMapper(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public Obs map(LabOrderResult labOrderResult, Order testOrder, Concept concept) {
        try {
            Date obsDate = labOrderResult.getResultDateTime();
            Obs topLevelObs = newObs(testOrder, obsDate, concept, null);
            Obs labObs = newObs(testOrder, obsDate, concept, null);
            topLevelObs.addGroupMember(labObs);
            if (isNotBlank(labOrderResult.getResult()) || isNotBlank(labOrderResult.getUploadedFileName())) {
                labObs.addGroupMember(newResultObs(testOrder, obsDate, concept, labOrderResult));
                if(BooleanUtils.isTrue(labOrderResult.getAbnormal())) {
                    labObs.addGroupMember(newObs(testOrder, obsDate, getConceptByName(LAB_ABNORMAL), labOrderResult.getAbnormal().toString()));
                }
                if (concept.isNumeric() && hasRange(labOrderResult)) {
                    labObs.addGroupMember(newObs(testOrder, obsDate, getConceptByName(LAB_MINNORMAL), labOrderResult.getMinNormal().toString()));
                    labObs.addGroupMember(newObs(testOrder, obsDate, getConceptByName(LAB_MAXNORMAL), labOrderResult.getMaxNormal().toString()));
                }
            }
            if (labOrderResult.getReferredOut() != null && labOrderResult.getReferredOut()) {
                labObs.addGroupMember(newObs(testOrder, obsDate, getConceptByName(REFERRED_OUT), labOrderResult.getReferredOut().toString()));
            }
            if (isNotBlank(labOrderResult.getNotes())) {
                labObs.addGroupMember(newObs(testOrder, obsDate, getConceptByName(LAB_NOTES), labOrderResult.getNotes()));
            }
            if (isNotBlank(labOrderResult.getUploadedFileName())) {
                labObs.addGroupMember(newObs(testOrder, obsDate, getConceptByName(LAB_REPORT), labOrderResult.getUploadedFileName()));
            }
            return topLevelObs;
        } catch (ParseException e) {
            throw new APIException(e);
        }
    }

    private Obs newResultObs(Order testOrder, Date obsDate, Concept concept, LabOrderResult labOrderResult) throws ParseException {
        Obs obs = new Obs();
        obs.setConcept(concept);
        obs.setOrder(testOrder);
        obs.setObsDatetime(obsDate);
        String accessionUuid = Optional.ofNullable(labOrderResult.getAccessionUuid()).orElseGet(() -> Optional.ofNullable(obs.getOrder()).map(Order::getAccessionNumber).orElse(EMPTY_STRING));
        if (concept.getDatatype().getHl7Abbreviation().equals("CWE")) {
            String resultUuid = labOrderResult.getResultUuid();
            Concept conceptAnswer = isEmpty(resultUuid) ? null : conceptService.getConceptByUuid(resultUuid);
                obs.setValueCoded(conceptAnswer);
            if (conceptAnswer == null) {
                log.error(String.format("Concept [%s] does not not have coded answer with ConceptUuid [%s] in OpenMRS, In Accession [%s]",
                        obs.getConcept().getName(), resultUuid, accessionUuid));
                return null;
            }
            return obs;
        }

        if (isEmpty(labOrderResult.getResult())) {
            return null;
        }
        obs.setValueAsString(labOrderResult.getResult());
        checkResultRangesForAbsolutes(obs, accessionUuid);
        return obs;
    }

    /**
     * This method just logs error if the results are out of absolute ranges. This will be errored out (ValidationException)
     * by openmrs by the {@link org.openmrs.validator.ObsValidator} during save
     */
    private void checkResultRangesForAbsolutes(Obs obs, String accessionUuid) {
        if (!obs.getConcept().isNumeric()) {
            return;
        }
        if (obs.getValueNumeric() != null) {
            ConceptNumeric cn = conceptService.getConceptNumeric(obs.getConcept().getId());
            if (cn.getHiAbsolute() != null && cn.getHiAbsolute() < obs.getValueNumeric()) {
                log.error(String.format("Test results for [%s] is beyond the absolute high range, in Accession [%s]", cn.getName(), accessionUuid));
            }
            if (cn.getLowAbsolute() != null && cn.getLowAbsolute() > obs.getValueNumeric()) {
                log.error(String.format("Test results for [%s] is beyond the absolute low range, in Accession [%s]", cn.getName(), accessionUuid));
            }
        }
    }

    private Concept getConceptByName(String conceptName) {
        return conceptService.getConceptByName(conceptName);
    }

    private Obs newObs(Order order, Date obsDate, Concept concept, String value) throws ParseException {
        Obs obs = new Obs();
        obs.setConcept(concept);
        obs.setOrder(order);
        obs.setObsDatetime(obsDate);
        if (isNotBlank(value)) {
            obs.setValueAsString(value);
        }
        return obs;
    }

    private boolean hasRange(LabOrderResult labOrderResult) {
        return labOrderResult.getMinNormal() != null && labOrderResult.getMaxNormal() != null;
    }
}

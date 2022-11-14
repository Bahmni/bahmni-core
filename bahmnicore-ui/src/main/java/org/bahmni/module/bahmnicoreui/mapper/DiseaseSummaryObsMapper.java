package org.bahmni.module.bahmnicoreui.mapper;


import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.bahmni.module.bahmnicoreui.constant.DiseaseSummaryConstants;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryMap;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.ETObsToBahmniObsMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiseaseSummaryObsMapper {

    public DiseaseSummaryMap map(Collection<BahmniObservation> bahmniObservations, String groupBy) {
        DiseaseSummaryMap diseaseSummaryMap = new DiseaseSummaryMap();
        bahmniObservations = extractGroupObservationFromParent(bahmniObservations);
        if (CollectionUtils.isNotEmpty(bahmniObservations)) {
            Map<String, List<BahmniObservation>> observationsByEncounter = groupObsByEncounterUuid(bahmniObservations);
            for (BahmniObservation bahmniObservation : bahmniObservations) {
                List<BahmniObservation> observationsFromConceptSet = new ArrayList<>();
                constructLeafObservationsFromConceptSet(bahmniObservation, observationsFromConceptSet);
                for (BahmniObservation leafObservation : observationsFromConceptSet) {
                    String startDateTime = getGroupByDate(leafObservation, groupBy);
                    String conceptName = leafObservation.getConcept().getShortName();
                    String observationValue = computeValueForLeafObservation(leafObservation, observationsByEncounter, !DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_OBS_DATETIME_AND_CONCEPT.equals(groupBy));
                    diseaseSummaryMap.put(startDateTime, conceptName, observationValue, leafObservation.isAbnormal(), false);
                }
            }
        }
        return diseaseSummaryMap;
    }

    private String getGroupByDate(BahmniObservation observation, String groupBy) {
        switch (StringUtils.defaultString(groupBy)) {
            case DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_ENCOUNTER: return DateFormatUtils.format(observation.getEncounterDateTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
            case DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_OBS_DATETIME:
            case DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_OBS_DATETIME_AND_CONCEPT:
                return DateFormatUtils.format(observation.getObservationDateTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
            default: return DateFormatUtils.format(observation.getVisitStartDateTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
        }
    }

    private List<BahmniObservation> extractGroupObservationFromParent(Collection<BahmniObservation> bahmniObservations){
        List<BahmniObservation> finalObservations = new ArrayList<>();
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            constructLeafObservationsFromConceptSet(bahmniObservation, finalObservations);
        }
        return finalObservations;
    }

    private Map<String, List<BahmniObservation>> groupObsByEncounterUuid(Collection<BahmniObservation> bahmniObservations) {
        Map<String,List<BahmniObservation>> result = new LinkedHashMap<>();
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            List<BahmniObservation> bahmniObservationsForEncounter = result.get(bahmniObservation.getEncounterUuid()) == null?new ArrayList<BahmniObservation>():result.get(bahmniObservation.getEncounterUuid());
            bahmniObservationsForEncounter.add(bahmniObservation);
            result.put(bahmniObservation.getEncounterUuid(), bahmniObservationsForEncounter);
        }
        return result;
    }

    private void constructLeafObservationsFromConceptSet(BahmniObservation bahmniObservation, List<BahmniObservation> observationsFromConceptSet) {
        if (bahmniObservation.getGroupMembers().size() > 0) {
            for (BahmniObservation groupMember : bahmniObservation.getGroupMembers())
                constructLeafObservationsFromConceptSet(groupMember, observationsFromConceptSet);
        } else {
            if (!ETObsToBahmniObsMapper.ABNORMAL_CONCEPT_CLASS.equals(bahmniObservation.getConcept().getConceptClass())) {
                observationsFromConceptSet.add(bahmniObservation);
            }
        }
    }

    private String computeValueForLeafObservation(BahmniObservation observation, Map<String, List<BahmniObservation>> observationsByEncounter, boolean allowMultiValue) {
        String observationValue = null;
        if (observationsByEncounter.containsKey(observation.getEncounterUuid())) {
            List<BahmniObservation> observationsInEncounter = observationsByEncounter.get(observation.getEncounterUuid());
            String multiSelectObsValue = "";
            for (BahmniObservation bahmniObservationInEncounter : observationsInEncounter) {
                if ( allowMultiValue && arePartOfMultiSelectObservation(observation,bahmniObservationInEncounter) ) {
                    multiSelectObsValue = multiSelectObsValue + "," + bahmniObservationInEncounter.getValueAsString();
                }
            }
            observationValue = StringUtils.isBlank(multiSelectObsValue)? observation.getValueAsString(): observation.getValueAsString() + multiSelectObsValue;
        }
        return observationValue;
    }

    private boolean arePartOfMultiSelectObservation(BahmniObservation observation,BahmniObservation observationInEncounter){
        return observation.getConcept().getName().equals(observationInEncounter.getConcept().getName())
                && StringUtils.equals(observation.getObsGroupUuid(),observationInEncounter.getObsGroupUuid())
                && !observation.getUuid().equals(observationInEncounter.getUuid());
    }

}

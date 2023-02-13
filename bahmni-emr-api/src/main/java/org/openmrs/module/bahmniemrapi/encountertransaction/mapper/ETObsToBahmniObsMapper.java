package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.parameters.AdditionalBahmniObservationFields;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.obs.ComplexData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class ETObsToBahmniObsMapper {

    public static final String CONCEPT_DETAILS_CONCEPT_CLASS = "Concept Details";
    public static final String ABNORMAL_CONCEPT_CLASS = "Abnormal";
    public static final String DURATION_CONCEPT_CLASS = "Duration";
    public static final String UNKNOWN_CONCEPT_CLASS = "Unknown" ;
    public static final String COMPLEX_DATATYPE = "Complex";
    private ConceptService conceptService;
    private ObsService obsService;

    List<BahmniComplexDataMapper> complexDataMappers = new ArrayList<>();

    @Autowired
    public ETObsToBahmniObsMapper(ConceptService conceptService, List<BahmniComplexDataMapper> complexDataMappers) {
        this.conceptService = conceptService;
        this.complexDataMappers = complexDataMappers;
    }

    public List<BahmniObservation> create(List<EncounterTransaction.Observation> allObservations, AdditionalBahmniObservationFields additionalBahmniObservationFields) {
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        for (EncounterTransaction.Observation observation : allObservations) {
            bahmniObservations.add(create(observation, additionalBahmniObservationFields));
        }
        return bahmniObservations;
    }

    public BahmniObservation create(EncounterTransaction.Observation observation, AdditionalBahmniObservationFields additionalBahmniObservationFields) {
        return map(observation, additionalBahmniObservationFields,
                Collections.singletonList(conceptService.getConceptByUuid(observation.getConceptUuid())),
                false);
    }

    protected BahmniObservation map(EncounterTransaction.Observation observation, AdditionalBahmniObservationFields additionalBahmniObservationFields, List<Concept> rootConcepts, boolean flatten) {

        BahmniObservation bahmniObservation= createBahmniObservation(observation,additionalBahmniObservationFields,rootConcepts);

        if (validateFormNameSpace(observation) && flatten) {
            handleFlattenedConceptDetails(observation,bahmniObservation);
        } else if (observation.getGroupMembers().size() > 0) {
            for (EncounterTransaction.Observation groupMember : observation.getGroupMembers()) {
                AdditionalBahmniObservationFields additionalFields = (AdditionalBahmniObservationFields) additionalBahmniObservationFields.clone();
                additionalFields.setObsGroupUuid(observation.getUuid());
                bahmniObservation.addGroupMember(map(groupMember, additionalFields, rootConcepts, flatten));
            }
        } else {
            bahmniObservation.setValue(observation.getValue());
            if (isComplexObs(bahmniObservation)) {
                bahmniObservation.setComplexData(getComplexObsValue(bahmniObservation));
            }

            bahmniObservation.setType(observation.getConcept().getDataType());
            bahmniObservation.setHiNormal(observation.getConcept().getHiNormal());
            bahmniObservation.setLowNormal(observation.getConcept().getLowNormal());
        }

        for (EncounterTransaction.Provider provider : additionalBahmniObservationFields.getProviders()) {
            bahmniObservation.addProvider(provider);
        }
        if (observation.getCreator() != null) {
            bahmniObservation.setCreatorName(observation.getCreator().getPersonName());
        }
        return bahmniObservation;
    }

    private boolean validateFormNameSpace(EncounterTransaction.Observation observation) {
        return observation.getFormNamespace() == null && CONCEPT_DETAILS_CONCEPT_CLASS.equals(observation.getConcept().getConceptClass());
    }

    private Serializable getComplexObsValue(BahmniObservation bahmniObservation) {
        if (complexDataMappers.isEmpty()) {
            return null;
        }
        Obs obs = getObsService().getObs(getObsService().getObsByUuid(bahmniObservation.getUuid()).getId());
        ComplexData complexData = obs.getComplexData();

        BahmniComplexDataMapper dataMapper = null;
        for (BahmniComplexDataMapper complexDataMapper : complexDataMappers) {
            if (complexDataMapper.canHandle(obs.getConcept(), complexData)) {
                dataMapper = complexDataMapper;
                break;
            }
        }

        return dataMapper!=null ? dataMapper.map(complexData) : complexData;
    }

    private ObsService getObsService() {
        if (this.obsService == null) {
            this.obsService = Context.getObsService();
        }
        return obsService;
    }

    private boolean isComplexObs(BahmniObservation bahmniObservation) {
        String conceptDataType = bahmniObservation.getConcept().getDataType();
        if (conceptDataType != null && !conceptDataType.isEmpty()) {
            return conceptDataType.equalsIgnoreCase(COMPLEX_DATATYPE);
        }
        return false;
    }

    private void setValueAndType(BahmniObservation bahmniObservation, EncounterTransaction.Observation member) {
        if(!bahmniObservation.isUnknown()) {
            bahmniObservation.setValue(member.getValue());
            bahmniObservation.setType(member.getConcept().getDataType());
        }
    }

    private void handleUnknownConceptClass(BahmniObservation bahmniObservation, EncounterTransaction.Observation etObs) {
        Object unknownObsValue = etObs.getValue();
        if(!(unknownObsValue instanceof Boolean) && unknownObsValue == null){
            return;
        }

        bahmniObservation.setUnknown((Boolean)unknownObsValue);
        if((Boolean)unknownObsValue){
            bahmniObservation.setValue(getConceptName(etObs));
        }
    }

    private String getConceptName(EncounterTransaction.Observation etObs) {
        return etObs.getConcept().getShortName() != null ? etObs.getConcept().getShortName() : etObs.getConcept().getName();
    }
    private void handleAbnormalConceptClass(BahmniObservation bahmniObservation, EncounterTransaction.Observation etObs) {
        if (etObs.getValue() instanceof Boolean) {
            bahmniObservation.setAbnormal((Boolean) etObs.getValue());
        } else {
            if (etObs.getValue() != null) {
                bahmniObservation.setAbnormal(Boolean.parseBoolean(((EncounterTransaction.Concept) etObs.getValue()).getName()));
            }
        }
    }

    private void handleFlattenedConceptDetails(EncounterTransaction.Observation observation, BahmniObservation bahmniObservation) {
        setHiNormalAndLowNormalForNumericUnknownObs(observation, bahmniObservation);
        for (EncounterTransaction.Observation member : observation.getGroupMembers()) {
            if (member.getVoided()) {
                continue;
            }
            if (member.getConcept().getConceptClass().equals(ABNORMAL_CONCEPT_CLASS)) {
                handleAbnormalConceptClass(bahmniObservation, member);
            } else if (member.getConcept().getConceptClass().equals(UNKNOWN_CONCEPT_CLASS)) {
                handleUnknownConceptClass(bahmniObservation, member);
            } else if (member.getConcept().getConceptClass().equals(DURATION_CONCEPT_CLASS)) {
                bahmniObservation.setDuration(new Double(member.getValue().toString()).longValue());
            } else {
                setValueAndType(bahmniObservation, member);
                bahmniObservation.getConcept().setUnits(member.getConcept().getUnits());
                bahmniObservation.setHiNormal(member.getConcept().getHiNormal());
                bahmniObservation.setLowNormal(member.getConcept().getLowNormal());
            }
        }
    }

    private void setHiNormalAndLowNormalForNumericUnknownObs(EncounterTransaction.Observation observation, BahmniObservation bahmniObservation) {
        if (observation.getGroupMembers().size() == 1 && observation.getGroupMembers().get(0).getConcept().getConceptClass().equals(UNKNOWN_CONCEPT_CLASS)){
            Concept conceptDetailsConcept = conceptService.getConceptByUuid(observation.getConceptUuid());

            Concept primaryNumericConcept = conceptDetailsConcept.getSetMembers().get(0);
            if (primaryNumericConcept.isNumeric()){
                ConceptNumeric conceptNumeric = conceptService.getConceptNumeric(primaryNumericConcept.getId());
                bahmniObservation.setHiNormal(conceptNumeric.getHiNormal());
                bahmniObservation.setLowNormal(conceptNumeric.getLowNormal());
            }
        }
    }

    private BahmniObservation createBahmniObservation(EncounterTransaction.Observation observation, AdditionalBahmniObservationFields additionalBahmniObservationFields, List<Concept> rootConcepts) {
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setEncounterTransactionObservation(observation);
        bahmniObservation.setEncounterDateTime(additionalBahmniObservationFields.getEncounterDateTime());
        bahmniObservation.setVisitStartDateTime(additionalBahmniObservationFields.getVisitDateTime());
        bahmniObservation.setConceptSortWeight(ConceptSortWeightUtil.getSortWeightFor(bahmniObservation.getConcept().getName(), rootConcepts));
        bahmniObservation.setEncounterUuid(additionalBahmniObservationFields.getEncounterUuid());
        bahmniObservation.setObsGroupUuid(additionalBahmniObservationFields.getObsGroupUuid());
        bahmniObservation.setUnknown(false);
        bahmniObservation.setEncounterTypeName(additionalBahmniObservationFields.getEncounterTypeName());
        return bahmniObservation;
    }

}

package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Concept;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniProviderMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.parameters.AdditionalBahmniObservationFields;
import org.openmrs.module.emrapi.encounter.ObservationMapper;
import org.openmrs.module.emrapi.encounter.matcher.ObservationTypeMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Component(value = "omrsObsToBahmniObsMapper")
public class OMRSObsToBahmniObsMapper {
    private ETObsToBahmniObsMapper etObsToBahmniObsMapper;
    private ObservationTypeMatcher observationTypeMatcher;
    private BahmniProviderMapper bahmniProviderMapper = new BahmniProviderMapper();
    private ObservationMapper observationMapper;

    @Autowired
    public OMRSObsToBahmniObsMapper(ETObsToBahmniObsMapper etObsToBahmniObsMapper, ObservationTypeMatcher observationTypeMatcher, ObservationMapper observationMapper) {
        this.etObsToBahmniObsMapper = etObsToBahmniObsMapper;
        this.observationTypeMatcher = observationTypeMatcher;
        this.observationMapper = observationMapper;
    }

    public Collection<BahmniObservation> map(List<Obs> obsList, Collection<Concept> rootConcepts) {
        Collection<BahmniObservation> bahmniObservations = new ArrayList<>();
        for (Obs obs : obsList) {
            if(observationTypeMatcher.getObservationType(obs).equals(ObservationTypeMatcher.ObservationType.OBSERVATION)){
                BahmniObservation bahmniObservation =map(obs);
                if(CollectionUtils.isNotEmpty(rootConcepts )){
                    bahmniObservation.setConceptSortWeight(ConceptSortWeightUtil.getSortWeightFor(bahmniObservation.getConcept().getName(), rootConcepts));
                }
                bahmniObservations.add(bahmniObservation);
            }
        }
        return bahmniObservations;
    }

    public BahmniObservation map(Obs obs) {
        if(obs == null)
             return null;
        String obsGroupUuid = obs.getObsGroup() == null? null : obs.getObsGroup().getUuid();
        AdditionalBahmniObservationFields additionalBahmniObservationFields =
                new AdditionalBahmniObservationFields(
                        obs.getEncounter().getUuid(),
                        obs.getEncounter().getEncounterDatetime(),
                        obs.getEncounter().getVisit().getStartDatetime(),
                        obsGroupUuid);
        if(obs.getEncounter().getEncounterType()!=null) {
            additionalBahmniObservationFields.setEncounterTypeName(obs.getEncounter().getEncounterType().getName());
        }
        for (EncounterProvider encounterProvider : obs.getEncounter().getEncounterProviders()) {
            additionalBahmniObservationFields.addProvider(bahmniProviderMapper.map(encounterProvider.getProvider()));
        }
        return etObsToBahmniObsMapper.map(observationMapper.map(obs), additionalBahmniObservationFields, Collections.singletonList(obs.getConcept()), true);
    }
}

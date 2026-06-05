package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.bahmni.module.obsrelationship.api.ObsRelationService;
import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.EncounterProviderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ObsRelationshipMapper {
    private ObsRelationService obsRelationService;
    private EncounterProviderMapper encounterProviderMapper;
    private OMRSObsToBahmniObsMapper OMRSObsToBahmniObsMapper;

    @Autowired
    public ObsRelationshipMapper(ObsRelationService obsRelationService,
                                 EncounterProviderMapper encounterProviderMapper,
                                 OMRSObsToBahmniObsMapper OMRSObsToBahmniObsMapper) {
        this.obsRelationService = obsRelationService;
        this.encounterProviderMapper = encounterProviderMapper;
        this.OMRSObsToBahmniObsMapper = OMRSObsToBahmniObsMapper;
    }

    public List<BahmniObservation> map(List<BahmniObservation> bahmniObservations, String encounterUuid) {
        List<ObsRelationship> obsRelationshipsInEncounter = obsRelationService.getRelationsWhereSourceObsInEncounter(encounterUuid);
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            for (ObsRelationship obsRelationship : obsRelationshipsInEncounter) {
                if (bahmniObservation.isSameAs(obsRelationship.getSourceObs())) {
                    org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship targetObsRelation =
                            new org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship();
                    targetObsRelation.setRelationshipType(obsRelationship.getObsRelationshipType().getName());
                    targetObsRelation.setUuid(obsRelationship.getUuid());
                    targetObsRelation.setTargetObs(OMRSObsToBahmniObsMapper.map(obsRelationship.getTargetObs(), null));
                    bahmniObservation.setTargetObsRelation(targetObsRelation);
//                    bahmniObservation.setProviders(providers);
                }
            }
        }
        return bahmniObservations;
    }

    public List<BahmniObservation> map(List<ObsRelationship> obsRelationships) {
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        for (ObsRelationship obsRelationship : obsRelationships) {

            BahmniObservation sourceObservation = OMRSObsToBahmniObsMapper.map(obsRelationship.getSourceObs(), null);
            BahmniObservation targetObservation = OMRSObsToBahmniObsMapper.map(obsRelationship.getTargetObs(), null);
            sourceObservation.setProviders(encounterProviderMapper.convert(obsRelationship.getSourceObs().getEncounter().getEncounterProviders()));

            org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship targetObsRelation =
                    new org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship(targetObservation, obsRelationship.getUuid(), obsRelationship.getObsRelationshipType().getName());
            sourceObservation.setTargetObsRelation(targetObsRelation);
            bahmniObservations.add(sourceObservation);

        }
        return bahmniObservations;
    }
}

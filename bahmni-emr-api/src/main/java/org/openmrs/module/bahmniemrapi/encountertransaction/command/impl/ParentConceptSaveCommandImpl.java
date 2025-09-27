package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.springframework.beans.BeansException;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ParentConceptSaveCommandImpl implements EncounterDataPreSaveCommand{
    @Override
    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        Collection<BahmniObservation> bahmniObservations = bahmniEncounterTransaction.getObservations();

        for(BahmniObservation bahmniObservation : bahmniObservations){
            String parentConceptUuid = bahmniObservation.getConceptUuid();
            bahmniObservation.setParentConceptUuid(parentConceptUuid);
            updateChildren(bahmniObservation);
        }

        return bahmniEncounterTransaction;
    }

    private void updateChildren(BahmniObservation parentObs) {
        Collection<BahmniObservation> childrenObs = parentObs.getGroupMembers();

        for(BahmniObservation observation: childrenObs){
            observation.setParentConceptUuid(parentObs.getParentConceptUuid());
            updateChildren(observation);
        }
    }
}

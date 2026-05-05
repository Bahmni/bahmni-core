package org.bahmni.module.bahmnicore.service;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchResponse;

public interface BahmniEncounterMatchDecisionService {

    EncounterMatchResponse decideMatch(EncounterMatchRequest request);
}

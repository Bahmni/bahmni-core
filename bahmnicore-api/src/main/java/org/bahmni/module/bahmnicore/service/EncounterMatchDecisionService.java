package org.bahmni.module.bahmnicore.service;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.EncounterMatchResponse;

public interface EncounterMatchDecisionService {

    EncounterMatchResponse decideMatch(EncounterMatchRequest request);
}

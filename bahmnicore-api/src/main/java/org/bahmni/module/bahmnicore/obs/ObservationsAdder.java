package org.bahmni.module.bahmnicore.obs;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

public interface ObservationsAdder {

    void addObservations(Collection<BahmniObservation> observations, List<String> conceptNames) throws ParseException;
}

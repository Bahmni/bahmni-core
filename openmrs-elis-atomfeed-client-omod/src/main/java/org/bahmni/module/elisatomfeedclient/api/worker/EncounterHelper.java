package org.bahmni.module.elisatomfeedclient.api.worker;

import org.openmrs.Encounter;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EncounterHelper {
    private EncounterService encounterService;

    public EncounterHelper(EncounterService encounterService) {
        this.encounterService = encounterService;
    }

    public Set<Encounter> getEncountersForAccession(String accessionUuid, EncounterType encounterType, Visit visit) {
        List<Encounter> encounters = filterEncountersByType(visit.getEncounters(),encounterType);
        Set<Encounter> matchedEncounters = new HashSet<>();
        if (encounters != null && !encounters.isEmpty()) {
            for (Encounter encounter : encounters) {
                if (encounterContainsObsPointingToAccession(encounter, accessionUuid)) {
                    matchedEncounters.add(encounter);
                }
            }
        }
        return matchedEncounters;
    }

    private boolean encounterContainsObsPointingToAccession(Encounter encounter, String accessionUuid) {
        Set<Obs> observations = encounter.getObs();
        for (Obs obs : observations) {
            if(obs.getValueText().equals(accessionUuid) &&
                    obs.getConcept().getName().getName().equals(OpenElisAccessionEventWorker.ACCESSION_UUID_CONCEPT)){
                return true;
            }
        }
        return false;
    }

    public Encounter createNewEncounter(Visit visit, EncounterType encounterType, Date encounterDate, Patient patient, Provider provider, Location location) {
        Encounter encounter = new Encounter();
        encounter.setPatient(patient);
        EncounterRole encounterRole = encounterService.getEncounterRoleByUuid(EncounterRole.UNKNOWN_ENCOUNTER_ROLE_UUID);
        encounter.addProvider(encounterRole, provider);
        encounter.setEncounterType(encounterType);
        encounter.setEncounterDatetime(encounterDate);
        encounter.setVisit(visit);
        encounter.setLocation(location);
        return encounter;

    }

    public boolean hasObservationWithText(String observationText, Encounter encounter) {
        Set<Obs> observations = encounter.getObs();
        if(!observations.isEmpty()){
            for (Obs observation : observations) {
                if(observation.getValueText().equals(observationText)){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasSameEncounterType(EncounterType encounterType, Encounter encounter) {
        return encounter.getEncounterType().getUuid().equals(encounterType.getUuid());
    }

    public boolean hasSameProvider(Provider provider, Encounter encounter) {
        if (encounter.getEncounterProviders().size() > 0) {
            return encounter.getEncounterProviders().iterator().next().getProvider().getUuid().equals(provider.getUuid());
        }
        return false;
    }

    public Encounter getEncounterByProviderAndEncounterType(Provider provider, EncounterType encounterType, Set<Encounter> encounters) {
        for (Encounter encounter : encounters) {
            if (hasSameEncounterType(encounterType, encounter) && hasSameProvider(provider, encounter)) {
                return encounter;
            }
        }
        return null;
    }

    public Encounter findOrInitializeEncounter(Visit visit, Provider testProvider, EncounterType encounterType, Date encounterDate, Location location) {
        Encounter encounter = getEncounterByProviderAndEncounterType(testProvider, encounterType, visit.getEncounters());
        if (encounter == null) {
            encounter = createNewEncounter(visit, encounterType,  encounterDate, visit.getPatient(), testProvider, location);
        }
        return encounter;
    }

    public List<Encounter> filterEncountersByType(Set<Encounter> encounters,EncounterType encounterType){
        List<Encounter> matchedEncounters = new ArrayList<>();
        for(Encounter encounter: encounters){
            if(hasSameEncounterType(encounterType,encounter)){
                matchedEncounters.add(encounter);
            }
        }
        return matchedEncounters;
    }
}

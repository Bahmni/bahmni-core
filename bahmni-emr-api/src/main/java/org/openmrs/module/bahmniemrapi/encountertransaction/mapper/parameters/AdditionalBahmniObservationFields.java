package org.openmrs.module.bahmniemrapi.encountertransaction.mapper.parameters;

import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class AdditionalBahmniObservationFields implements Cloneable {
    private String encounterUuid;
    private Date encounterDateTime;
    private Date visitDateTime;
    private Set<EncounterTransaction.Provider> providers = new HashSet<>();
    private String obsGroupUuid;
    private String encounterTypeName;

    public AdditionalBahmniObservationFields(String encounterUuid, Date encounterDateTime, Date visitDateTime,String obsGroupUuid) {
        this.encounterUuid = encounterUuid;
        this.encounterDateTime = encounterDateTime;
        this.visitDateTime = visitDateTime;
        this.obsGroupUuid = obsGroupUuid;
    }

    public String getEncounterTypeName() {
        return encounterTypeName;
    }

    public void setEncounterTypeName(String encounterTypeName) {
        this.encounterTypeName = encounterTypeName;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public Date getVisitDateTime() {
        return visitDateTime;
    }

    public void setVisitDateTime(Date visitDateTime) {
        this.visitDateTime = visitDateTime;
    }

    public Set<EncounterTransaction.Provider> getProviders() {
        return providers;
    }

    public void setProviders(Set<EncounterTransaction.Provider> providers) {
        this.providers = providers;
    }


    public void addProvider(EncounterTransaction.Provider provider) {
        this.providers.add(provider);
    }

    public String getObsGroupUuid() {
        return obsGroupUuid;
    }

    public void setObsGroupUuid(String obsGroupUuid) {
        this.obsGroupUuid = obsGroupUuid;
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("unable to clone "+this.getClass().getName(),e);
        }
    }
}

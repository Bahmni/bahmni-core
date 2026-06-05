package org.bahmni.module.bahmnicore.forms2.contract;

import org.bahmni.module.bahmnicore.model.Provider;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class FormDetails {

    private String formType;
    private String formName;
    private int formVersion;
    private String visitUuid;
    private Date visitStartDateTime;
    private String encounterUuid;
    private Date encounterDateTime;
    private Set<Provider> providers;

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public int getFormVersion() {
        return formVersion;
    }

    public void setFormVersion(int formVersion) {
        this.formVersion = formVersion;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public Date getVisitStartDateTime() {
        return visitStartDateTime;
    }

    public void setVisitStartDateTime(Date visitStartDateTime) {
        this.visitStartDateTime = visitStartDateTime;
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

    public Set<Provider> getProviders() {
        return providers;
    }

    public void setProviders(Set<Provider> providers) {
        this.providers = providers;
    }

    public void addProvider(String providerName, String providerUuid) {
        if (providers == null) {
            this.providers = new HashSet<>();
        }
        Provider provider = new Provider();
        provider.setProviderName(providerName);
        provider.setUuid(providerUuid);

        this.providers.add(provider);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof FormDetails)) {
            return false;
        }
        FormDetails formDetails = (FormDetails) obj;
        return this.formName.equals(formDetails.getFormName())
                && this.formVersion == formDetails.formVersion
                && this.encounterUuid.equals(formDetails.encounterUuid);
    }

    @Override
    public int hashCode() {
        int result = this.formName != null ? this.formName.hashCode() : 0;
        result += 31 * result + this.formVersion;
        result += 31 * result + (this.encounterUuid != null ? this.encounterUuid.hashCode() : 0);
        return result;
    }
}

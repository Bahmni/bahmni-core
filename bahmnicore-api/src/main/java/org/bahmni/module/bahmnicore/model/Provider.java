package org.bahmni.module.bahmnicore.model;

import java.util.Objects;

public class Provider {
    private String providerName;
    private String uuid;

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Provider provider = (Provider) o;
        return Objects.equals(providerName, provider.providerName) &&
                Objects.equals(uuid, provider.uuid);
    }

    @Override
    public int hashCode() {

        return Objects.hash(providerName, uuid);
    }
}


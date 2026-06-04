package org.bahmni.module.referencedata.labconcepts.contract;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotNull;

public class Drug {
    private String uuid;
    @NotNull
    private String name;
    @NotNull
    private String genericName;
    private Boolean combination;
    private String strength;
    private String dosageForm;
    private String minimumDose;
    private String maximumDose;
    private String shortName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public void setCombination(Boolean combination) {
        this.combination = combination;
    }

    public Boolean isCombination() {
        return BooleanUtils.toBoolean(combination);
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getStrength() {
        return strength;
    }

    public void setDosageForm(String dosageForm) {
        this.dosageForm = dosageForm;
    }

    public String getDosageForm() {
        return dosageForm;
    }

    public void setMinimumDose(String minimumDose) {
        this.minimumDose = minimumDose;
    }

    public String getMinimumDose() {
        return minimumDose;
    }

    public void setMaximumDose(String maximumDose) {
        this.maximumDose = maximumDose;
    }

    public String getMaximumDose() {
        return maximumDose;
    }


    public Double doubleMaximumDose() {
        return StringUtils.isBlank(maximumDose) ? null : Double.valueOf(maximumDose);
    }

    public Double doubleMinimumDose() {
        return StringUtils.isBlank(minimumDose) ? null : Double.valueOf(minimumDose);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.csv.models;

import org.apache.commons.lang3.BooleanUtils;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;

public class DrugRow extends CSVEntity {
    @CSVHeader(name = "uuid", optional = true)
    private String uuid;

    @CSVHeader(name = "Name")
    private String name;

    @CSVHeader(name = "Generic Name")
    private String genericName;

    @CSVHeader(name = "Combination", optional = true)
    private String combination;

    @CSVHeader(name = "Strength", optional = true)
    private String strength;

    @CSVHeader(name = "Dosage Form")
    private String dosageForm;

    @CSVHeader(name = "Minimum Dose", optional = true)
    private String minimumDose;

    @CSVHeader(name = "Maximum Dose", optional = true)
    private String maximumDose;

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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean getCombination() {
        return BooleanUtils.toBoolean(combination);
    }

    public void setCombination(String combination) {
        this.combination = combination;
    }
}

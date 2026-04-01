/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.contract.drugorder;

import org.openmrs.OrderFrequency;

public class OrderFrequencyData {

    private String uuid;
    private Double frequencyPerDay;
    private String name;

    public OrderFrequencyData(OrderFrequency orderFrequency) {
        this.setUuid(orderFrequency.getUuid());
        this.setFrequencyPerDay(orderFrequency.getFrequencyPerDay());
        this.setName(orderFrequency.getName());
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setFrequencyPerDay(Double frequencyPerDay) {
        this.frequencyPerDay = frequencyPerDay;
    }

    public Double getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.bahmni.module.bahmnicore.mapper.builder;

import org.bahmni.module.bahmnicore.model.BahmniFeedDrugOrder;

import java.util.UUID;

public class BahmniDrugOrderBuilder {

    private final BahmniFeedDrugOrder bahmniDrugOrder;

    public BahmniDrugOrderBuilder() {
        bahmniDrugOrder = new BahmniFeedDrugOrder();
        bahmniDrugOrder.setDosage(2.5);
        bahmniDrugOrder.setProductUuid(UUID.randomUUID().toString());
        bahmniDrugOrder.setQuantity(3.0);
        bahmniDrugOrder.setUnit("ml");
    }

    public BahmniDrugOrderBuilder withProductUuid(String productUuid) {
        bahmniDrugOrder.setProductUuid(productUuid);
        return this;
    }

    public BahmniFeedDrugOrder build() {
        return bahmniDrugOrder;
    }

    public BahmniDrugOrderBuilder withNumberOfDaysAndDosage(int numberOfDays, Double dosage) {
        bahmniDrugOrder.setDosage(dosage);
        bahmniDrugOrder.setNumberOfDays(numberOfDays);
        bahmniDrugOrder.setQuantity(bahmniDrugOrder.getDosage() * numberOfDays);
        return this;
    }

    public BahmniDrugOrderBuilder withNumberOfDaysAndDosage(int numberOfDays, int dosage) {
        return withNumberOfDaysAndDosage(numberOfDays, (double)dosage);
    }
}
/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.drugorder.dosinginstructions;

import org.openmrs.DosingInstructions;
import org.openmrs.DrugOrder;
import org.openmrs.api.APIException;
import org.openmrs.module.bahmniemrapi.drugorder.DrugOrderUtil;
import org.springframework.validation.Errors;

import java.util.Date;
import java.util.Locale;

public class FlexibleDosingInstructions implements DosingInstructions {

    public DrugOrderUtil drugOrderUtil;

    @Override
    public String getDosingInstructionsAsString(Locale locale) {
        return null;
    }

    @Override
    public void setDosingInstructions(DrugOrder order) {
        order.setDosingType(this.getClass());
    }


    @Override
    public DosingInstructions getDosingInstructions(DrugOrder order) {
        if (!order.getDosingType().equals(this.getClass())) {
            throw new APIException("Dosing type of drug order is mismatched. Expected:" + this.getClass() + " but received:"
                    + order.getDosingType());
        }
        return new FlexibleDosingInstructions();
    }

    @Override
    public void validate(DrugOrder order, Errors errors) {

    }

    @Override
    public Date getAutoExpireDate(DrugOrder drugOrder) {
        return DrugOrderUtil.calculateAutoExpireDate(drugOrder.getDuration(), drugOrder.getDurationUnits(), drugOrder.getNumRefills(), drugOrder.getEffectiveStartDate(), drugOrder.getFrequency());
    }

}

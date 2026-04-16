/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.drugorder;

import org.openmrs.Concept;
import org.openmrs.Duration;
import org.openmrs.OrderFrequency;

import java.util.Date;

import static org.apache.commons.lang3.time.DateUtils.addSeconds;

public class DrugOrderUtil {
    public static Date calculateAutoExpireDate(Integer orderDuration, Concept durationUnits, Integer numRefills, Date effectiveStartDate, OrderFrequency frequency) {
        if (orderDuration == null || durationUnits == null) {
            return null;
        }
        if (numRefills != null && numRefills > 0) {
            return null;
        }
        String durationCode = Duration.getCode(durationUnits);
        if (durationCode == null) {
            return null;
        }
        Duration duration = new Duration(orderDuration, durationCode);
        return aSecondBefore(duration.addToDate(effectiveStartDate, frequency));
    }

    public static Date aSecondBefore(Date date) {
        return addSeconds(date, -1);
    }
    public static Date aSecondAfter(Date date) {
        return addSeconds(date, 1);
    }

}

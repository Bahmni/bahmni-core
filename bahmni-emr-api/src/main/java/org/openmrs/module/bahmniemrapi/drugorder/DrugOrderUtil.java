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

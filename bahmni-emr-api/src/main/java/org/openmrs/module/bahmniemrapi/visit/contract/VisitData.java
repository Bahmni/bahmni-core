package org.openmrs.module.bahmniemrapi.visit.contract;


import org.openmrs.Visit;

import java.util.Date;

public class VisitData {
    private String uuid;
    private Date startDateTime;

    public VisitData(Visit visit) {
        this.uuid = visit.getUuid();
        this.startDateTime = visit.getStartDatetime();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }
}

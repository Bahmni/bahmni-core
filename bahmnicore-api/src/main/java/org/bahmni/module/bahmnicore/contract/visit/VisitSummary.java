package org.bahmni.module.bahmnicore.contract.visit;

import java.util.Date;

public class VisitSummary {
    private String uuid;
    private Date startDateTime;
    private Date stopDateTime;
    private String visitType;
    private IPDDetails admissionDetails;
    private IPDDetails dischargeDetails;


    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getStopDateTime() {
        return stopDateTime;
    }

    public void setStopDateTime(Date stopDateTime) {
        this.stopDateTime = stopDateTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public IPDDetails getAdmissionDetails() {
        return admissionDetails;
    }

    public void setAdmissionDetails(IPDDetails admissionDetails) {
        this.admissionDetails = admissionDetails;
    }

    public IPDDetails getDischargeDetails() {
        return dischargeDetails;
    }

    public void setDischargeDetails(IPDDetails dischargeDetails) {
        this.dischargeDetails = dischargeDetails;
    }

    public static class IPDDetails {
        private String uuid;
        private Date date;
        private String provider;
        private String notes;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }
    }
}
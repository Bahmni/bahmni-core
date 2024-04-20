package org.bahmni.module.bahmnicore.contract.SMS;

public class PrescriptionSMS {
    private String visitUuid;
    private String locale = "en";

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}
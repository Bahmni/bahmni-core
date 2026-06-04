package org.bahmni.jss.registration;

public class RegistrationNumber {
    private String centerCode;
    private String id;

    public RegistrationNumber(String centerCode, String id) {
        this.centerCode = centerCode;
        this.id = id;
    }

    public String getCenterCode() {
        return centerCode;
    }

    public String getId() {
        return id;
    }
}
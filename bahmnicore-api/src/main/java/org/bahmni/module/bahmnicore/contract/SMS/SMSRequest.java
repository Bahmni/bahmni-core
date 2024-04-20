package org.bahmni.module.bahmnicore.contract.SMS;

import org.codehaus.jackson.annotate.JsonProperty;

public class SMSRequest {
    private String phoneNumber;
    private String message;

    @JsonProperty
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @JsonProperty
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
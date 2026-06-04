package org.bahmni.module.elisatomfeedclient.api.domain;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.joda.time.DateTime;

import java.util.Date;

public class OpenElisTestDetail {
    private String testName;
    private String testUnitOfMeasurement;
    private String testUuid;
    private String panelUuid;
    private Double minNormal;
    private Double maxNormal;
    private String result;
    private String resultUuid;
    private String notes;
    private String resultType;
    private String providerUuid;
    private String dateTime;
    private String status;
    private Boolean abnormal;
    private String uploadedFileName;

    public OpenElisTestDetail() {
    }

    public OpenElisTestDetail(String testName, String testUnitOfMeasurement, String testUuid, String panelUuid, Double minNormal, Double maxNormal, String result, String resultUuid, String notes, String resultType, String providerUuid, String dateTime, String status, Boolean abnormal, String uploadedFileName) {
        this.testName = testName;
        this.testUnitOfMeasurement = testUnitOfMeasurement;
        this.testUuid = testUuid;
        this.panelUuid = panelUuid;
        this.minNormal = minNormal;
        this.maxNormal = maxNormal;
        this.result = result;
        this.resultUuid = resultUuid;
        this.notes = notes;
        this.resultType = resultType;
        this.providerUuid = providerUuid;
        this.dateTime = dateTime;
        this.status = status;
        this.abnormal = abnormal;
        this.uploadedFileName = uploadedFileName;
    }

    @JsonIgnore
    public boolean isCancelled() {
        return "Canceled".equals(status);
    }

    public Date fetchDate() {
        return  dateTime == null ? null : DateTime.parse(dateTime).toDate();
    }

    @JsonIgnore
    public boolean isReferredOut() {
        return status != null && (status.equalsIgnoreCase("referred out") || status.equalsIgnoreCase("Finalized RO"));
    }

    public String getTestName() {

        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestUnitOfMeasurement() {
        return testUnitOfMeasurement;
    }

    public void setTestUnitOfMeasurement(String testUnitOfMeasurement) {
        this.testUnitOfMeasurement = testUnitOfMeasurement;
    }

    public String getTestUuid() {
        return testUuid;
    }

    public void setTestUuid(String testUuid) {
        this.testUuid = testUuid;
    }

    public String getPanelUuid() {
        return panelUuid;
    }

    public void setPanelUuid(String panelUuid) {
        this.panelUuid = panelUuid;
    }

    public Double getMinNormal() {
        return minNormal;
    }

    public void setMinNormal(Double minNormal) {
        this.minNormal = minNormal;
    }

    public Double getMaxNormal() {
        return maxNormal;
    }

    public void setMaxNormal(Double maxNormal) {
        this.maxNormal = maxNormal;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultUuid() {
        return resultUuid;
    }

    public void setResultUuid(String resultUuid) {
        this.resultUuid = resultUuid;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getResultType() {
        return resultType;
    }

    public void setResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(Boolean abnormal) {
        this.abnormal = abnormal;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }
}

package org.openmrs.module.bahmniemrapi.laborder.contract;

import org.openmrs.module.bahmniemrapi.accessionnote.contract.AccessionNote;

import java.util.Date;
import java.util.List;

public class LabOrderResult {
    private String orderUuid;
    private String action;
    private String accessionUuid;
    private Date accessionDateTime;
    private Date visitStartTime;
    private List<AccessionNote> accessionNotes;
    private String testName;
    private String testUnitOfMeasurement;
    private String testUuid;
    private String panelUuid;
    private String panelName;
    private Double minNormal;
    private Double maxNormal;
    private String resultUuid;
    private String result;
    private String notes;
    private Boolean abnormal;
    private String provider;
    private Boolean referredOut;
    private Date resultDateTime;
    private String uploadedFileName;
    private String preferredTestName;
    private String preferredPanelName;

    public LabOrderResult() {
    }

    public LabOrderResult(String orderUuid, String action, String accessionUuid, Date accessionDateTime, String testName, String testUnitOfMeasurement, Double minNormal, Double maxNormal, String result, Boolean abnormal, Boolean referredOut, String uploadedFileName, List<AccessionNote> accessionNotes) {
        this.orderUuid = orderUuid;
        this.action = action;
        this.accessionUuid = accessionUuid;
        this.testName = testName;
        this.testUnitOfMeasurement = testUnitOfMeasurement;
        this.minNormal = minNormal;
        this.maxNormal = maxNormal;
        this.accessionDateTime = accessionDateTime;
        this.result = result;
        this.abnormal = abnormal;
        this.referredOut = referredOut;
        this.uploadedFileName = uploadedFileName;
        this.accessionNotes = accessionNotes;
    }

    public String getOrderUuid() {
        return orderUuid;
    }

    public void setOrderUuid(String orderUuid) {
        this.orderUuid = orderUuid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAccessionUuid() {
        return accessionUuid;
    }

    public void setAccessionUuid(String accessionUuid) {
        this.accessionUuid = accessionUuid;
    }

    public Date getAccessionDateTime() {
        return accessionDateTime;
    }

    public void setAccessionDateTime(Date accessionDateTime) {
        this.accessionDateTime = accessionDateTime;
    }

    public Date getVisitStartTime() {
        return visitStartTime;
    }

    public void setVisitStartTime(Date visitStartTime) {
        this.visitStartTime = visitStartTime;
    }

    public List<AccessionNote> getAccessionNotes() {
        return accessionNotes;
    }

    public void setAccessionNotes(List<AccessionNote> accessionNotes) {
        this.accessionNotes = accessionNotes;
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

    public String getPanelName() {
        return panelName;
    }

    public void setPanelName(String panelName) {
        this.panelName = panelName;
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

    public String getResultUuid() {
        return resultUuid;
    }

    public void setResultUuid(String resultUuid) {
        this.resultUuid = resultUuid;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(Boolean abnormal) {
        this.abnormal = abnormal;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Boolean getReferredOut() {
        return referredOut;
    }

    public void setReferredOut(Boolean referredOut) {
        this.referredOut = referredOut;
    }

    public Date getResultDateTime() {
        return resultDateTime;
    }

    public void setResultDateTime(Date resultDateTime) {
        this.resultDateTime = resultDateTime;
    }

    public String getUploadedFileName() {
        return uploadedFileName;
    }

    public void setUploadedFileName(String uploadedFileName) {
        this.uploadedFileName = uploadedFileName;
    }

    public String getPreferredTestName() {
        return preferredTestName;
    }

    public void setPreferredTestName(String preferredTestName) {
        this.preferredTestName = preferredTestName;
    }

    public String getPreferredPanelName() {
        return preferredPanelName;
    }

    public void setPreferredPanelName(String preferredPanelName) {
        this.preferredPanelName = preferredPanelName;
    }

    public static class Builder {

        private String orderUuid;
        private String action;
        private String accessionUuid;
        private String testName;
        private String testUnitOfMeasurement;
        private Double minNormal;
        private Double maxNormal;
        private Date accessionDateTime;
        private List<AccessionNote> accessionNotes;
        private String uploadedFileName;
        private Boolean referredOut;
        private Boolean abnormal;
        private String result;
        private String preferredTestName;
        private String notes;
        private Date resultDateTime;
        private String testUuid;
        private String providerName;
        private Date vsiitStartTime;
        private String panelName;
        private String panelUuid;
        private String preferredPanelName;

        public Builder order(String orderUuid) {
            this.orderUuid = orderUuid;
            return this;
        }

        public Builder action(String action) {
            this.action = action;
            return this;
        }
        public Builder accession(String accessionUuid) {
            this.accessionUuid = accessionUuid;
            return this;
        }

        public Builder testName(String testName) {
            this.testName = testName;
            return this;
        }

        public Builder uom(String uom) {
            this.testUnitOfMeasurement = uom;
            return this;
        }

        public Builder minNormal(Double minNormal) {
            this.minNormal = minNormal;
            return this;
        }

        public Builder maxNormal(Double maxNormal) {
            this.maxNormal = maxNormal;
            return this;
        }

        public Builder accessionDateTime(Date accessionDateTime) {
            this.accessionDateTime = accessionDateTime;
            return this;
        }

        public Builder result(String result) {
            this.result = result;
            return this;
        }

        public Builder abnormal(Boolean abnormal) {
            this.abnormal = abnormal;
            return this;
        }

        public Builder referredOut(Boolean referredOut) {
            this.referredOut = referredOut;
            return this;
        }

        public Builder uploadedFileName(String uploadedFileName) {
            this.uploadedFileName = uploadedFileName;
            return this;
        }

        public Builder accessionNotes(List<AccessionNote> accessionNotes) {
            this.accessionNotes = accessionNotes;
            return this;
        }

        public LabOrderResult build() {
            LabOrderResult labOrderResult = new LabOrderResult();
            labOrderResult.setOrderUuid(orderUuid);
            labOrderResult.setAction(action);
            labOrderResult.setTestName(testName);
            labOrderResult.setTestUuid(testUuid);
            labOrderResult.setResult(result);
            labOrderResult.setTestUnitOfMeasurement(testUnitOfMeasurement);
            labOrderResult.setAccessionUuid(accessionUuid);
            labOrderResult.setAccessionDateTime(accessionDateTime);
            labOrderResult.setAccessionNotes(accessionNotes);
            labOrderResult.setMinNormal(minNormal);
            labOrderResult.setMaxNormal(maxNormal);
            labOrderResult.setAbnormal(abnormal);
            labOrderResult.setReferredOut(referredOut);
            labOrderResult.setUploadedFileName(uploadedFileName);
            labOrderResult.setPreferredTestName(preferredTestName);
            labOrderResult.setNotes(notes);
            labOrderResult.setResultDateTime(resultDateTime);
            labOrderResult.setProvider(providerName);
            labOrderResult.setVisitStartTime(vsiitStartTime);
            labOrderResult.setPanelName(panelName);
            labOrderResult.setPreferredPanelName(preferredPanelName);
            labOrderResult.setPanelUuid(panelUuid);
            return labOrderResult;
        }

        public Builder preferredTestName(String preferredTestName) {
            this.preferredTestName = preferredTestName;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder resultDateTime(Date resultDateTime) {
            this.resultDateTime = resultDateTime;
            return this;
        }

        public Builder testUuid(String testUuid) {
            this.testUuid = testUuid;
            return this;
        }

        public Builder provider(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder visitStartTime(Date vsiitStartTime) {
            this.vsiitStartTime = vsiitStartTime;
            return this;
        }

        public Builder panelName(String panelName) {
            this.panelName = panelName;
            return this;
        }

        public Builder panelUuid(String panelUuid) {
            this.panelUuid = panelUuid;
            return this;
        }

        public Builder preferredPanelName(String preferredPanelName) {
            this.preferredPanelName = preferredPanelName;
            return this;
        }
    }
}

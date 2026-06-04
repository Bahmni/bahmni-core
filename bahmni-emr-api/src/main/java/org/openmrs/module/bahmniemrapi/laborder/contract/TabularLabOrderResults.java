package org.openmrs.module.bahmniemrapi.laborder.contract;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TabularLabOrderResults {
    private List<DateLabel> dates = new ArrayList<>();
    private List<TestOrderLabel> orders = new ArrayList<>();
    private List<CoordinateValue> values = new ArrayList<>();

    @JsonCreator
    public TabularLabOrderResults(@JsonProperty("dates")List<DateLabel> dates,
                                  @JsonProperty("orders")List<TestOrderLabel> orders,
                                  @JsonProperty("values")List<CoordinateValue> values) {
        this.dates = dates;
        this.orders = orders;
        this.values = values;
    }

    public static class DateLabel {
        private Integer index;
        private String date;

        @JsonCreator
        public DateLabel(@JsonProperty("index")Integer index,
                         @JsonProperty("date")String date) {
            this.index = index;
            this.date = date;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    public static class TestOrderLabel {
        private Integer index;
        private String testName;
        private Double minNormal;
        private Double maxNormal;
        private String testUnitOfMeasurement;
        private String panelName;

        @JsonCreator
        public TestOrderLabel(@JsonProperty("index")Integer index,
                              @JsonProperty("testName")String testName,
                              @JsonProperty("minNormal")Double minNormal,
                              @JsonProperty("maxNormal")Double maxNormal,
                              @JsonProperty("testUnitOfMeasurement")String testUnitOfMeasurement,
                              @JsonProperty("panelName")String panelName) {
            this.index = index;
            this.testName = testName;
            this.minNormal = minNormal;
            this.maxNormal = maxNormal;
            this.testUnitOfMeasurement = testUnitOfMeasurement;
            this.panelName = panelName;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
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

        public String getTestUnitOfMeasurement() {
            return testUnitOfMeasurement;
        }

        public void setTestUnitOfMeasurement(String testUnitOfMeasurement) {
            this.testUnitOfMeasurement = testUnitOfMeasurement;
        }

        public String getPanelName() {
            return panelName;
        }

        public void setPanelName(String panelName) {
            this.panelName = panelName;
        }
    }

    public static class CoordinateValue {
        private Date accessionDateTime;
        private Integer dateIndex;
        private Integer testOrderIndex;
        private String result;
        private Boolean abnormal;
        private Boolean referredOut;
        private String uploadedFileName;

        public Date getAccessionDateTime() {
            return accessionDateTime;
        }

        public void setAccessionDateTime(Date accessionDateTime) {
            this.accessionDateTime = accessionDateTime;
        }

        public Integer getDateIndex() {
            return dateIndex;
        }

        public void setDateIndex(Integer dateIndex) {
            this.dateIndex = dateIndex;
        }

        public Integer getTestOrderIndex() {
            return testOrderIndex;
        }

        public void setTestOrderIndex(Integer testOrderIndex) {
            this.testOrderIndex = testOrderIndex;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public Boolean getAbnormal() {
            return abnormal;
        }

        public void setAbnormal(Boolean abnormal) {
            this.abnormal = abnormal;
        }

        public Boolean getReferredOut() {
            return referredOut;
        }

        public void setReferredOut(Boolean referredOut) {
            this.referredOut = referredOut;
        }

        public String getUploadedFileName() {
            return uploadedFileName;
        }

        public void setUploadedFileName(String uploadedFileName) {
            this.uploadedFileName = uploadedFileName;
        }
    }

    public List<DateLabel> getDates() {
        return dates;
    }

    public void setDates(List<DateLabel> dates) {
        this.dates = dates;
    }

    public List<TestOrderLabel> getOrders() {
        return orders;
    }

    public void setOrders(List<TestOrderLabel> orders) {
        this.orders = orders;
    }

    public List<CoordinateValue> getValues() {
        return values;
    }

    public void setValues(List<CoordinateValue> values) {
        this.values = values;
    }
}

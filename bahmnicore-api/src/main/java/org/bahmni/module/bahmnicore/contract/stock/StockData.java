package org.bahmni.module.bahmnicore.contract.stock;

public class StockData {

    private String stockLocationName;

    private Double availableQuantity;

    private Double onHandQuantity;

    private String unit;

    private String batchNumber;

    private String expiryDate;

    public String getStockLocationName() {
        return stockLocationName;
    }

    public void setStockLocationName(String stockLocationName) {
        this.stockLocationName = stockLocationName;
    }

    public Double getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Double availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Double getOnHandQuantity() {
        return onHandQuantity;
    }

    public void setOnHandQuantity(Double onHandQuantity) {
        this.onHandQuantity = onHandQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }
}

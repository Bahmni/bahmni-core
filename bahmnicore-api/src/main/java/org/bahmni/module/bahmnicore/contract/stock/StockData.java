package org.bahmni.module.bahmnicore.contract.stock;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StockData {

    @JsonProperty("stock_location_name")
    private String stockLocationName;

    @JsonProperty("available_quantity")
    private Double availableQuantity;

    @JsonProperty("on_hand_quantity")
    private Double onHandQuantity;

    @JsonProperty("unit")
    private String unit;

    @JsonProperty("batch_number")
    private String batchNumber;

    @JsonProperty("expiry_date")
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

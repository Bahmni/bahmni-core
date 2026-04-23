package org.bahmni.module.bahmnicore.contract.stock;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AvailableStockResponse {

    private int count;
    private List<StockData> data;

    public AvailableStockResponse() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<StockData> getData() {
        return data;
    }

    public void setData(List<StockData> data) {
        this.data = data;
    }
}

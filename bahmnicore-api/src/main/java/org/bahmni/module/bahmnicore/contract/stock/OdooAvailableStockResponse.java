package org.bahmni.module.bahmnicore.contract.stock;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OdooAvailableStockResponse {

    private int count;
    private List<OdooStockData> data;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<OdooStockData> getData() {
        return data;
    }

    public void setData(List<OdooStockData> data) {
        this.data = data;
    }
}

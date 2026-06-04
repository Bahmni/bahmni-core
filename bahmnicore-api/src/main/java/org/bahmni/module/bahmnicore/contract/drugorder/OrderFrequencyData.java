package org.bahmni.module.bahmnicore.contract.drugorder;

import org.openmrs.OrderFrequency;

public class OrderFrequencyData {

    private String uuid;
    private Double frequencyPerDay;
    private String name;

    public OrderFrequencyData(OrderFrequency orderFrequency) {
        this.setUuid(orderFrequency.getUuid());
        this.setFrequencyPerDay(orderFrequency.getFrequencyPerDay());
        this.setName(orderFrequency.getName());
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setFrequencyPerDay(Double frequencyPerDay) {
        this.frequencyPerDay = frequencyPerDay;
    }

    public Double getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

package org.bahmni.module.bahmnicore.contract.encounter.data;

import java.util.Date;

public class PersonObservationData {
    private String conceptName;
    private Double value;
    private Date observationDate;
    private boolean numeric;
    private String units;

    public PersonObservationData(String conceptName, Double value, Date observationDate, boolean numeric, String units) {
        this.conceptName = conceptName;
        this.value = value;
        this.observationDate = observationDate;
        this.numeric = numeric;
        this.units = units;
    }

    public boolean isNumeric() {
        return numeric;
    }

    public void setNumeric(boolean numeric) {
        this.numeric = numeric;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public PersonObservationData() {
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public Date getObservationDate() {
        return observationDate;
    }

    public void setObservationDate(Date observationDate) {
        this.observationDate = observationDate;
    }
}
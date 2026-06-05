package org.openmrs.module.bahmniemrapi.drugorder.contract;

public class BahmniOrderAttribute {
    public static final String ORDER_ATTRIBUTES_CONCEPT_SET_NAME = "Order Attributes";
    private String name;
    private String value;
    private String obsUuid;
    private String conceptUuid;
    private String encounterUuid;

    public BahmniOrderAttribute() {
    }

    public BahmniOrderAttribute(String name, String value, String obsUuid, String conceptUuid, String encounterUuid) {
        this.name = name;
        this.value = value;
        this.obsUuid = obsUuid;
        this.conceptUuid = conceptUuid;
        this.encounterUuid = encounterUuid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getObsUuid() {
        return obsUuid;
    }

    public void setObsUuid(String obsUuid) {
        this.obsUuid = obsUuid;
    }

    public String getConceptUuid() {
        return conceptUuid;
    }

    public void setConceptUuid(String conceptUuid) {
        this.conceptUuid = conceptUuid;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }
}

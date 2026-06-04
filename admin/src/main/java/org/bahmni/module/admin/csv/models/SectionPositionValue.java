package org.bahmni.module.admin.csv.models;

public class SectionPositionValue {

    private String value;
    private String sectionIndex;
    private int valueIndex;

    private int multiSelectIndex;
    private int addmoreIndex;

    public SectionPositionValue(String value, String sectionIndex, int valueIndex, int multiSelectIndex, int addmoreIndex) {
        this.value = value;
        this.sectionIndex = sectionIndex;
        this.valueIndex = valueIndex;
        this.multiSelectIndex = multiSelectIndex;
        this.addmoreIndex = addmoreIndex;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSectionIndex() {
        return sectionIndex;
    }

    public void setSectionIndex(String sectionIndex) {
        this.sectionIndex = sectionIndex;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    public int getMultiSelectIndex() {
        return multiSelectIndex;
    }

    public void setMultiSelectIndex(int multiSelectIndex) {
        this.multiSelectIndex = multiSelectIndex;
    }

    public int getAddmoreIndex() {
        return addmoreIndex;
    }

    public void setAddmoreIndex(int addmoreIndex) {
        this.addmoreIndex = addmoreIndex;
    }

}

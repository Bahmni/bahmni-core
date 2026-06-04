package org.bahmni.module.bahmnicore.forms2.contract;

public enum FormType {

    FORMS1("v1"), FORMS2("v2");

    private final String type;

    FormType(String type) {
        this.type = type;
    }


    public static FormType valueOfType(String value) {
        for(FormType v : values()) {
            if (v.type.equalsIgnoreCase(value)) return v;
        }
        throw new IllegalArgumentException();
    }

    public String getType() {
        return this.type;
    }


}

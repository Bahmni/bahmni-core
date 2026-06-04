package org.bahmni.module.bahmnicore.model.searchParams;

public class AdditionalSearchParam {

    private String additionalSearchHandler;
    private String tests;

    public AdditionalSearchParam(String additionalSearchHandler, String tests) {
        this.additionalSearchHandler = additionalSearchHandler;
        this.tests = tests;
    }

    public AdditionalSearchParam() {
    }

    public String getAdditionalSearchHandler() {
        return additionalSearchHandler;
    }

    public void setAdditionalSearchHandler(String additionalSearchHandler) {
        this.additionalSearchHandler = additionalSearchHandler;
    }

    public String getTests(){
        return tests;
    }

    public void setTests(String tests){
        this.tests = tests;
    }
}



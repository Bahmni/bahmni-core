package org.bahmni.module.bahmnicore.contract.diseasetemplate;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DiseaseTemplateConfig {
    private String templateName;
    private List<String> showOnly;

    public DiseaseTemplateConfig(String templateName, List<String> showOnly) {
        this.templateName = templateName;
        this.showOnly = showOnly;
    }


    public DiseaseTemplateConfig() {
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public List<String> getShowOnly() {
        return showOnly;
    }

    public void setShowOnly(List<String> showOnly) {
        this.showOnly = showOnly;
    }
}

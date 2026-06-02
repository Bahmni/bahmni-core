/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.contract.diseasetemplate;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.List;

public class DiseaseTemplatesConfig {
    private List<DiseaseTemplateConfig> diseaseTemplateConfigList;
    private String patientUuid;
    private Date startDate;
    private Date endDate;

    public List<DiseaseTemplateConfig> getDiseaseTemplateConfigList() {
        return diseaseTemplateConfigList;
    }

    public void setDiseaseTemplateConfigList(List<DiseaseTemplateConfig> diseaseTemplateConfigList) {
        this.diseaseTemplateConfigList = diseaseTemplateConfigList;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    @JsonProperty("endDate")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @JsonProperty("startDate")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;
import org.openmrs.Drug;

public class DrugMetaDataMapper {

    public org.openmrs.Drug map(DrugMetaData drugMetaData) {
        Drug drug = null;

        if (drugMetaData.getExistingDrug() != null) {
            drug = drugMetaData.getExistingDrug();
        } else {
            drug = new Drug();
        }

        drug.setDosageForm(drugMetaData.getDosageForm());
        drug.setConcept(drugMetaData.getDrugConcept());
        return drug;
    }
}

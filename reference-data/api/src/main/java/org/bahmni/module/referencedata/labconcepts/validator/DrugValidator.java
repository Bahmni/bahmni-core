/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.validator;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.module.referencedata.labconcepts.contract.Drug;
import org.bahmni.module.referencedata.labconcepts.model.DrugMetaData;

import java.util.ArrayList;
import java.util.List;

public class DrugValidator extends Validator{

    private final DrugMetaDataValidator drugMetaDataValidator;

    public DrugValidator() {
        drugMetaDataValidator = new DrugMetaDataValidator();
    }

    public void validate(Drug drug, DrugMetaData drugMetaData) {
        drugMetaDataValidator.validate(drugMetaData);
        List<String> errors = new ArrayList<>();
        if(StringUtils.isNotBlank(drug.getUuid()) && drugMetaData.getExistingDrug() == null){
            errors.add("Drug with provided Uuid does not exist");
        }
        if (StringUtils.isBlank(drug.getName())){
            errors.add("Drug name is mandatory");
        }
        if (StringUtils.isBlank(drug.getGenericName())){
            errors.add("Drug generic name is mandatory");
        }
        throwExceptionIfExists(errors);
    }
}

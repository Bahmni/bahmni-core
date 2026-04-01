/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang.StringUtils;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.concepts.mapper.DrugMapper;
import org.bahmni.module.admin.csv.models.DrugRow;
import org.bahmni.module.referencedata.labconcepts.contract.Drug;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataDrugService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DrugPersister implements EntityPersister<DrugRow> {
    @Autowired
    private ReferenceDataDrugService referenceDataDrugService;

    @Override
    public Messages validate(DrugRow drugRow) {
        Messages messages = new Messages();
        if (StringUtils.isEmpty(drugRow.getName())) {
            messages.add("Drug name not specified\n");
        }
        if (StringUtils.isEmpty(drugRow.getGenericName())) {
            messages.add("Drug generic name not specified\n");
        }
        return messages;
    }

    @Override
    public Messages persist(DrugRow drugRow) {
        Drug drug = new DrugMapper().map(drugRow);
        referenceDataDrugService.saveDrug(drug);
        return new Messages();
    }
}
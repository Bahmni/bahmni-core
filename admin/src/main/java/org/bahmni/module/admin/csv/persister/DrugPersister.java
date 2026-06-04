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
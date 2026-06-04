package org.bahmni.module.bahmnicore.extensions;

import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;

public class MonthCalculationExtension extends BaseTableExtension<TreatmentRegimen> {

    @Override
    public void update(TreatmentRegimen treatmentRegimen, String patientUuid, String patientProgramUuid) {
    // Do nothing
    }
}

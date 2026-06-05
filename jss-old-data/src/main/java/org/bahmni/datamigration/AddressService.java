package org.bahmni.datamigration;

import org.apache.commons.lang.StringUtils;

public class AddressService {
    private MasterTehsils masterTehsils;
    private AmbiguousTehsils ambiguousTehsils;
    private CorrectedTehsils correctedTehsils;

    public AddressService(MasterTehsils masterTehsils, AmbiguousTehsils ambiguousTehsils, CorrectedTehsils correctedTehsils) {
        this.masterTehsils = masterTehsils;
        this.ambiguousTehsils = ambiguousTehsils;
        this.correctedTehsils = correctedTehsils;
    }

    public FullyQualifiedTehsil getTehsilFor(FullyQualifiedTehsil tehsilFromPatientRecord) {
        String tehsil = tehsilFromPatientRecord.getTehsil();
        if (StringUtils.isBlank(tehsil)) return tehsilFromPatientRecord;

        String correctedTehsil = correctedTehsils.correctedTehsil(tehsil);
        if (correctedTehsil == null)
            return tehsilFromPatientRecord;

        if (StringUtils.isBlank(correctedTehsil))
            return new FullyQualifiedTehsil("", tehsilFromPatientRecord.getDistrict(), tehsilFromPatientRecord.getState());

        FullyQualifiedTehsil matchingMasterTehsil = masterTehsils.getFullyQualifiedTehsil(correctedTehsil);
        if (matchingMasterTehsil == null)
            return new FullyQualifiedTehsil(correctedTehsil, tehsilFromPatientRecord.getDistrict(), tehsilFromPatientRecord.getState());

        if (ambiguousTehsils.contains(matchingMasterTehsil.getTehsil())) {
            return new FullyQualifiedTehsil(matchingMasterTehsil.getTehsil(),
                    tehsilFromPatientRecord.getDistrict(), tehsilFromPatientRecord.getState());
        }
        return matchingMasterTehsil;
    }
}
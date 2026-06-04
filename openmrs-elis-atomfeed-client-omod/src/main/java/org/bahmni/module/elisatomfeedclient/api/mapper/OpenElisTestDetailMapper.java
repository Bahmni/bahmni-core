package org.bahmni.module.elisatomfeedclient.api.mapper;

import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;
import org.joda.time.DateTime;
import org.openmrs.Concept;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;

public class OpenElisTestDetailMapper {

    public LabOrderResult map(OpenElisTestDetail testDetail, Concept concept) {
        LabOrderResult labOrderResult = new LabOrderResult();
        labOrderResult.setPanelUuid(testDetail.getPanelUuid());
        labOrderResult.setTestUuid(testDetail.getTestUuid());
        labOrderResult.setTestName(testDetail.getTestName());
        labOrderResult.setResultUuid(testDetail.getResultUuid());
        labOrderResult.setResult(getValue(testDetail.getResult(), concept));
        labOrderResult.setResultDateTime(DateTime.parse(testDetail.getDateTime()).toDate());
        labOrderResult.setTestUnitOfMeasurement(testDetail.getTestUnitOfMeasurement());
        labOrderResult.setReferredOut(testDetail.isReferredOut());
        labOrderResult.setAbnormal(testDetail.getAbnormal());
        labOrderResult.setMinNormal(testDetail.getMinNormal());
        labOrderResult.setMaxNormal(testDetail.getMaxNormal());
        labOrderResult.setAccessionDateTime(DateTime.parse(testDetail.getDateTime()).toDate());
        labOrderResult.setUploadedFileName(testDetail.getUploadedFileName());
        labOrderResult.setNotes(testDetail.getNotes());
        return labOrderResult;
    }

    private String getValue(String value, Concept concept) {
        if (value == null || value.isEmpty()) return null;
        if(concept.isNumeric()) {
            return getNumericResultValue(value);
        }
        return value;
    }

    private String getNumericResultValue(String value) {
        try {
            return Double.valueOf(value).toString();
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

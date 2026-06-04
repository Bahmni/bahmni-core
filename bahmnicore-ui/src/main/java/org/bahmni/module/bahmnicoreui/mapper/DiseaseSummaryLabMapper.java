package org.bahmni.module.bahmnicoreui.mapper;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.bahmni.module.bahmnicoreui.constant.DiseaseSummaryConstants;
import org.bahmni.module.bahmnicoreui.contract.DiseaseSummaryMap;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;

import java.util.List;

public class DiseaseSummaryLabMapper {

    public DiseaseSummaryMap map(List<LabOrderResult> labOrderResults, String groupBy) {
        DiseaseSummaryMap diseaseSummaryMap = new DiseaseSummaryMap();
        for (LabOrderResult labOrderResult : labOrderResults) {
            String startDateTime = (DiseaseSummaryConstants.RESULT_TABLE_GROUP_BY_ENCOUNTER.equals(groupBy)) ?
                    DateFormatUtils.format(labOrderResult.getAccessionDateTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern()) : DateFormatUtils.format(labOrderResult.getVisitStartTime(), DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
            String conceptName = labOrderResult.getTestName();
            if (conceptName != null) {
                diseaseSummaryMap.put(startDateTime, conceptName, labOrderResult.getResult(), labOrderResult.getAbnormal(), true);
            }
        }
        return diseaseSummaryMap;
    }
}

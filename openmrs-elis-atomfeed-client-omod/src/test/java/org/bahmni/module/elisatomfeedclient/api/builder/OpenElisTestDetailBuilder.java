/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.elisatomfeedclient.api.builder;

import org.bahmni.module.elisatomfeedclient.api.domain.OpenElisTestDetail;

public class OpenElisTestDetailBuilder {

    private final OpenElisTestDetail testDetail;

    public OpenElisTestDetailBuilder() {
        testDetail = new OpenElisTestDetail();
        testDetail.setTestUuid("Test123");
    }

    public OpenElisTestDetail build() {
        return testDetail;
    }

    public OpenElisTestDetailBuilder withTestUuid(String testUuid) {
        testDetail.setTestUuid(testUuid);
        return this;
    }

    public OpenElisTestDetailBuilder withStatus(String status) {
        testDetail.setStatus(status);
        return this;
    }

    public OpenElisTestDetailBuilder withPanelUuid(String panelUuid) {
        testDetail.setPanelUuid(panelUuid);
        return this;
    }

    public OpenElisTestDetailBuilder withResult(String result) {
        testDetail.setResult(result);
        return this;
    }

    public OpenElisTestDetailBuilder withProviderUuid(String providerUuid) {
        testDetail.setProviderUuid(providerUuid);
        return this;
    }

    public OpenElisTestDetailBuilder withDateTime(String dateTime) {
        testDetail.setDateTime(dateTime);
        return this;
    }

    public OpenElisTestDetailBuilder withMinNormal(String minNormalValue) {
        testDetail.setMinNormal(Double.valueOf(minNormalValue));
        return this;
    }

    public OpenElisTestDetailBuilder withMaxNormal(String maxNormalValue) {
        testDetail.setMaxNormal(Double.valueOf(maxNormalValue));
        return this;
    }

    public OpenElisTestDetailBuilder withAbnormal(String value) {
        testDetail.setAbnormal(Boolean.valueOf(value));
        return this;
    }

    public OpenElisTestDetailBuilder withResultType(String resultType) {
        testDetail.setResultType(resultType);
        return this;
    }

    public OpenElisTestDetailBuilder withUploadedFileName(String uploadedFileName) {
        testDetail.setUploadedFileName(uploadedFileName);
        return this;
    }
}

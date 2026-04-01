/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

public class BahmniDiagnosisRequestBuilder {
    private BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();

    public BahmniDiagnosisRequestBuilder withCodedAnswer(EncounterTransaction.Concept concept) {
        bahmniDiagnosisRequest.setCodedAnswer(concept);
        return this;
    }

    public BahmniDiagnosisRequest build() {
        return bahmniDiagnosisRequest;
    }

    public BahmniDiagnosisRequestBuilder withOrder(String order) {
        this.bahmniDiagnosisRequest.setOrder(order);
        return this;
    }

    public BahmniDiagnosisRequestBuilder withCertainty(String certainty) {
        this.bahmniDiagnosisRequest.setCertainty(certainty);
        return this;
    }

    public BahmniDiagnosisRequestBuilder withStatus(EncounterTransaction.Concept statusConcept) {
        this.bahmniDiagnosisRequest.setDiagnosisStatusConcept(
                statusConcept
        );
        return this;
    }
}

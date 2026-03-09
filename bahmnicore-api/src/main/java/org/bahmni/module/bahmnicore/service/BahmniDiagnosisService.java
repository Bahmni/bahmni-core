/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service;

import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

public interface BahmniDiagnosisService {
    void delete(String diagnosisObservationUuid);
    List<BahmniDiagnosisRequest> getBahmniDiagnosisByPatientAndVisit(String patientUuid,String visitUuid);
    List<BahmniDiagnosisRequest> getBahmniDiagnosisByPatientAndDate(String patientUuid, String date) throws ParseException;

    boolean isExternalTerminologyServerLookupNeeded();

    Collection<Concept> getDiagnosisSets();

    List<ConceptSource> getConceptSourcesForDiagnosisSearch();
}

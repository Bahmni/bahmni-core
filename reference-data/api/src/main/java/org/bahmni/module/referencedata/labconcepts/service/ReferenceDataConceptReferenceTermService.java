/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.service;

import org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm;
import org.openmrs.ConceptMap;

public interface ReferenceDataConceptReferenceTermService {
    public org.openmrs.ConceptReferenceTerm getConceptReferenceTerm(String referenceTermCode, String referenceTermSource);

    public ConceptMap getConceptMap(org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm conceptReferenceTermData);

    public org.openmrs.ConceptReferenceTerm saveOrUpdate(ConceptReferenceTerm conceptReferenceTerm);
}

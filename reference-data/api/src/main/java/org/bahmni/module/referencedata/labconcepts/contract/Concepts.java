/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.contract;

import java.util.List;

public class Concepts {
    private List<Concept> conceptList;
    private List<ConceptSet> conceptSetList;

    public List<Concept> getConceptList() {
        return conceptList;
    }

    public void setConceptList(List<Concept> conceptList) {
        this.conceptList = conceptList;
    }

    public List<ConceptSet> getConceptSetList() {
        return conceptSetList;
    }

    public void setConceptSetList(List<ConceptSet> conceptSetList) {
        this.conceptSetList = conceptSetList;
    }
}

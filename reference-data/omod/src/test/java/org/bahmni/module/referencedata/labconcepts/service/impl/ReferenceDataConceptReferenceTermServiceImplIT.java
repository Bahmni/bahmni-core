package org.bahmni.module.referencedata.labconcepts.service.impl;

import org.bahmni.module.referencedata.BaseIntegrationTest;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptReferenceTermService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.ConceptReferenceTerm;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ReferenceDataConceptReferenceTermServiceImplIT extends BaseIntegrationTest {

    @Autowired
    private ReferenceDataConceptReferenceTermService referenceDataConceptReferenceTermService;

    @Before
    public void setUp() throws Exception {
        executeDataSet("referenceTermSetup.xml");
    }

    @Test
    public void shouldGetConceptMapping() throws Exception {
        ConceptReferenceTerm referenceTerm = referenceDataConceptReferenceTermService.getConceptReferenceTerm("New Code", "org.openmrs.module.emrapi");
        assertNotNull(referenceTerm);
        assertEquals("New Code", referenceTerm.getCode());
        assertEquals("org.openmrs.module.emrapi", referenceTerm.getConceptSource().getName());
    }
}
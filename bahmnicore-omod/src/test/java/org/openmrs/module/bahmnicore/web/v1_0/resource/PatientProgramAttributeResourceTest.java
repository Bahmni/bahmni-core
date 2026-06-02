/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.PatientProgramAttribute;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Ignore;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.impl.BaseDelegatingResourceTest;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_2.PatientProgramAttributeResource2_2;

@Ignore
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class PatientProgramAttributeResourceTest extends BaseDelegatingResourceTest<PatientProgramAttributeResource2_2, PatientProgramAttribute> {

    @Before
    public void before() throws Exception {
        executeDataSet("programEnrollmentDataSet.xml");
    }

    @Override
    public PatientProgramAttribute newObject() {
        return Context.getService(BahmniProgramWorkflowService.class).getPatientProgramAttributeByUuid(getUuidProperty());
    }

    @Override
    public void validateDefaultRepresentation() throws Exception {
        super.validateDefaultRepresentation();
        assertPropEquals("value", getObject().getValue());
        assertPropPresent("attributeType");
        assertPropEquals("voided", getObject().getVoided());
    }

    @Override
    public void validateFullRepresentation() throws Exception {
        super.validateFullRepresentation();
        assertPropEquals("value", getObject().getValue());
        assertPropPresent("attributeType");
        assertPropEquals("voided", getObject().getVoided());
        assertPropPresent("auditInfo");
    }

    @Override
    public String getDisplayProperty() {
        return "stage: Stage1";
    }

    @Override
    public String getUuidProperty() {
        return RestConstants.PATIENT_PROGRAM_ATTRIBUTE_UUID;
    }
}

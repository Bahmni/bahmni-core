package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

public class BacteriologySpecimenSearchHandlerIT extends BahmniMainResourceControllerTest {
    @Override
    public String getURI() {
        return "specimen";
    }

    @Override
    public String getUuid() {
        return null;
    }

    @Override
    public long getAllCount() {
        return 0;
    }

    @Before
    public void setUp() throws Exception {
        executeDataSet("search/bacteriologySpecimen/baseBacteriologyData.xml");
        executeDataSet("search/bacteriologySpecimen/existingSpecimenObs.xml");
        executeDataSet("search/bacteriologySpecimen/programEpisodeMapping.xml");
    }

    @Test
    public void shouldReturnNoSpecimenForGivenProgramEnrollmentUuidWhenNoDataExist() throws Exception{
        String patientProgramUuid = "patient_program_uuid-not-there";

        MockHttpServletRequest request = request(RequestMethod.GET, getURI());
        request.addParameter("s", "byPatientProgram");
        request.addParameter("patientProgramUuid", patientProgramUuid);
        request.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject object = deserialize(handle(request));

        List results = object.get("results");
        Assert.assertEquals(0, results.size());
    }

    @Test
    public void shouldReturnAllSpecimenForGivenProgramEnrollmentUuid() throws Exception {
        String patientProgramUuid = "2edf272c-bf05-4208-9f93-2fa213ed0415";//standardTestDataset.xml

        MockHttpServletRequest request = request(RequestMethod.GET, getURI());
        request.addParameter("s", "byPatientProgram");
        request.addParameter("patientProgramUuid", patientProgramUuid);
        request.addParameter("v", RestConstants.REPRESENTATION_DEFAULT);

        SimpleObject object = deserialize(handle(request));

        List results = object.get("results");
        Assert.assertEquals(1, results.size());
    }
}

package org.bahmni.module.bahmnicore.obs.handler;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class SurgicalBlockObsHandlerTest {

    private SurgicalBlockObsHandler handler;
    private Obs obs;

    @Before
    public void setUp() {
        handler = new SurgicalBlockObsHandler();
        obs = new Obs();
        obs.setConcept(new Concept());
    }

    @Test
    public void saveObs_shouldNullComplexDataAndPreserveValueComplex() {
        obs.setValueComplex("surgical-appointment-uuid");
        obs.setComplexData(new ComplexData("title", new Object()));

        Obs result = handler.saveObs(obs);

        assertNull(result.getComplexData());
        assertEquals("surgical-appointment-uuid", result.getValueComplex());
    }

    @Test
    public void getObs_shouldSetComplexDataFromValueComplexWhenPresent() {
        obs.setValueComplex("surgical-appointment-uuid");

        Obs result = handler.getObs(obs, ComplexObsHandler.RAW_VIEW);

        assertNotNull(result.getComplexData());
        assertEquals("surgical-appointment-uuid", result.getComplexData().getTitle());
        assertEquals("surgical-appointment-uuid", result.getComplexData().getData());
    }

    @Test
    public void getObs_shouldNotSetComplexDataWhenValueComplexIsNull() {
        obs.setValueComplex(null);

        Obs result = handler.getObs(obs, ComplexObsHandler.RAW_VIEW);

        assertNull(result.getComplexData());
    }

    @Test
    public void getObs_shouldNotSetComplexDataWhenValueComplexIsEmpty() {
        obs.setValueComplex("");

        Obs result = handler.getObs(obs, ComplexObsHandler.RAW_VIEW);

        assertNull(result.getComplexData());
    }

    @Test
    public void getSupportedViews_shouldReturnAllFourViews() {
        String[] views = handler.getSupportedViews();

        assertEquals(4, views.length);
        assertEquals(ComplexObsHandler.RAW_VIEW, views[0]);
        assertEquals(ComplexObsHandler.URI_VIEW, views[1]);
        assertEquals(ComplexObsHandler.HTML_VIEW, views[2]);
        assertEquals(ComplexObsHandler.TEXT_VIEW, views[3]);
    }
}

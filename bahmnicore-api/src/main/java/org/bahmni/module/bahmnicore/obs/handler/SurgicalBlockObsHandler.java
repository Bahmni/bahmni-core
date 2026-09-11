package org.bahmni.module.bahmnicore.obs.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.AbstractHandler;
import org.springframework.stereotype.Component;

@Component
public class SurgicalBlockObsHandler extends AbstractHandler implements ComplexObsHandler {

    public static final Log log = LogFactory.getLog(SurgicalBlockObsHandler.class);

    private static final String[] supportedViews = new String[] {
            ComplexObsHandler.RAW_VIEW, ComplexObsHandler.URI_VIEW,
            ComplexObsHandler.HTML_VIEW, ComplexObsHandler.TEXT_VIEW };

    @Override
    public Obs saveObs(Obs obs) throws APIException {
        obs.setComplexData(null);
        obs.setValueComplex(obs.getValueComplex());
        return obs;
    }

    @Override
    public Obs getObs(Obs obs, String view) {
        try {
            String surgicalAppointmentUuid = obs.getValueComplex();
            if (surgicalAppointmentUuid != null && !surgicalAppointmentUuid.isEmpty()) {
                obs.setComplexData(new ComplexData(surgicalAppointmentUuid, surgicalAppointmentUuid));
            }
        } catch (Exception e) {
            log.error("Error retrieving surgical block obs data for obs [concept:"
                    + obs.getConcept().getId() + "].", e);
        }
        return obs;
    }

    @Override
    public String[] getSupportedViews() {
        return supportedViews;
    }
}

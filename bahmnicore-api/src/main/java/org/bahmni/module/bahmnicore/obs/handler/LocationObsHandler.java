package org.bahmni.module.bahmnicore.obs.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.AbstractHandler;
import org.springframework.stereotype.Component;

@Component
public class LocationObsHandler extends AbstractHandler implements ComplexObsHandler {

    public static final Log log = LogFactory.getLog(LocationObsHandler.class);
    private static final String[] supportedViews = new String[] { ComplexObsHandler.RAW_VIEW,
            ComplexObsHandler.URI_VIEW, ComplexObsHandler.HTML_VIEW, ComplexObsHandler.TEXT_VIEW};

    @Override
    public Obs saveObs(Obs obs) throws APIException {
        LocationService ls = Context.getLocationService();
        Object complexObsData = obs.getValueComplex();

        try {
            int locationId = Integer.parseInt(complexObsData.toString());
            Integer conceptId = obs.getConcept().getId();
            Location location = ls.getLocation(locationId);
            if (location == null) {
                throw new APIException(String.format("Cannot save complex obs [concept:%d] with desired location [%d] information. Can not find location.", conceptId, locationId));
            }
            obs.setComplexData(null);
            obs.setValueComplex(String.valueOf(locationId));
            return obs;
        } catch (NumberFormatException e) {
            log.error("Error occurred while trying to parse Location info from obs. ");
            throw new APIException(String.format("Cannot save complex obs [concept:%d] with desired location [%s] information.", obs.getConcept().getId(), complexObsData.toString()));
        }

    }

    @Override
    public Obs getObs(Obs obs, String view) {
        LocationService ls = Context.getLocationService();
        try {
            String valueComplex = obs.getValueComplex();
            if (valueComplex != null && !valueComplex.isEmpty()) {
                String[] parts = valueComplex.split("\\|");
                Location location = ls.getLocation(Integer.parseInt(parts[0]));
                if (location != null) {
                    ComplexData cd = new ComplexData(location.getName(), location);
                    obs.setComplexData(cd);
                }
            }
        } catch (Exception e) {
            log.error(String.format("Error occurred while retreving location obs data for obs [concept:%d].", obs.getConcept().getId()), e);
            //TODO: should we be throwing error, how do the apis handle exception?
        }
        return obs;
    }

    @Override
    public String[] getSupportedViews() {
        return supportedViews;
    }


}

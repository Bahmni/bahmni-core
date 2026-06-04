package org.bahmni.module.bahmnicore.obs.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.Provider;
import org.openmrs.api.APIException;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.AbstractHandler;
import org.springframework.stereotype.Component;

@Component
public class ProviderObsHandler extends AbstractHandler implements ComplexObsHandler {

    public static final Log log = LogFactory.getLog(LocationObsHandler.class);
    private static final String[] supportedViews = new String[] { ComplexObsHandler.RAW_VIEW,
            ComplexObsHandler.URI_VIEW, ComplexObsHandler.HTML_VIEW, ComplexObsHandler.TEXT_VIEW};

    @Override
    public Obs saveObs(Obs obs) throws APIException {
        ProviderService ps = Context.getProviderService();
        Object complexObsData = obs.getValueComplex();

        try {
            int providerId = Integer.parseInt(complexObsData.toString());
            Integer conceptId = obs.getConcept().getId();
            Provider provider = ps.getProvider(providerId);
            if (provider == null) {
                throw new APIException(String.format("Cannot save complex obs [concept:%d] with desired provider [%d] information. Can not find provider.", conceptId, providerId));
            }
            obs.setComplexData(null);
            obs.setValueComplex(String.valueOf(providerId));
            return obs;
        } catch (NumberFormatException e) {
            log.error("Error occurred while trying to parse Provider info from obs. ");
            throw new APIException(String.format("Cannot save complex obs [concept:%d] with desired provider [%s] information.", obs.getConcept().getId(), complexObsData.toString()));
        }

    }

    @Override
    public Obs getObs(Obs obs, String view) {
        ProviderService ps = Context.getProviderService();
        try {
            String valueComplex = obs.getValueComplex();
            if (valueComplex != null && !valueComplex.isEmpty()) {
                String[] parts = valueComplex.split("\\|");
                Provider provider = ps.getProvider(Integer.parseInt(parts[0]));
                if (provider != null) {
                    ComplexData cd = new ComplexData(provider.getName(), provider);
                    obs.setComplexData(cd);
                }
            }
        } catch (Exception e) {
            log.error(String.format("Error occurred while retreving provider obs data for obs [concept:%d].", obs.getConcept().getId()), e);
            //TODO: should we be throwing error, how do the apis handle exception?
        }
        return obs;
    }

    @Override
    public String[] getSupportedViews() {
        return supportedViews;
    }


}

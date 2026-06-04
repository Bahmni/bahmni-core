package org.bahmni.module.bahmnicore.obs.handler;

import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.AbstractHandler;
import org.openmrs.obs.handler.BinaryDataHandler;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class ImageUrlHandler extends AbstractHandler implements ComplexObsHandler {

    @Override
    public Obs getObs(Obs obs, String view) {
        File file = BinaryDataHandler.getComplexDataFile(obs);
        ComplexData complexData = null;
        complexData = new ComplexData(file.getName(), null);
        String mimeType = OpenmrsUtil.getFileMimeType(file);
        complexData.setMimeType(mimeType);
        obs.setComplexData(complexData);
        return obs;
    }

    @Override
    public Obs saveObs(Obs obs) throws APIException {
        return obs;
    }
}

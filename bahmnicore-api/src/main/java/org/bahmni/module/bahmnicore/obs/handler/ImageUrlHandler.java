package org.bahmni.module.bahmnicore.obs.handler;

import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.StorageService;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.obs.ComplexData;
import org.openmrs.obs.ComplexObsHandler;
import org.openmrs.obs.handler.AbstractHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;

@Component
public class ImageUrlHandler extends AbstractHandler implements ComplexObsHandler {

    @Autowired
    private StorageService storageService;

    @Override
    public Obs getObs(Obs obs, String view) {
        String key = parseDataKey(obs);
        ObjectMetadata metadata;
        try {
            metadata = storageService.getMetadata(key);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        ComplexData complexData = new ComplexData(metadata.getFilename(), null);
        complexData.setMimeType(metadata.getMimeType());
        obs.setComplexData(complexData);
        return obs;
    }

    @Override
    public Obs saveObs(Obs obs) throws APIException {
        return obs;
    }
}

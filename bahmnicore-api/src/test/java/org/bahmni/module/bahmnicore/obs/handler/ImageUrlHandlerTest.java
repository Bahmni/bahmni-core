package org.bahmni.module.bahmnicore.obs.handler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openmrs.Obs;
import org.openmrs.api.StorageService;
import org.openmrs.api.storage.ObjectMetadata;
import org.openmrs.obs.ComplexData;

import java.io.IOException;
import java.io.UncheckedIOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ImageUrlHandlerTest {

    @Mock
    private StorageService storageService;

    @InjectMocks
    private ImageUrlHandler handler;

    private Obs obsWithKey() {
        Obs obs = new Obs();
        obs.setValueComplex("image-title|image-storage-key");
        return obs;
    }

    @Test
    public void getObs_shouldSetComplexDataFromStorageMetadata() throws IOException {
        Obs obs = obsWithKey();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setFilename("scan.image");
        metadata.setMimeType("image/test");
        when(storageService.exists("image-storage-key")).thenReturn(true);
        when(storageService.getMetadata("image-storage-key")).thenReturn(metadata);

        Obs result = handler.getObs(obs, "ignored-view");

        assertSame(obs, result);
        ComplexData complexData = result.getComplexData();
        assertNotNull(complexData);
        assertEquals("scan.image", complexData.getTitle());
        assertEquals("image/test", complexData.getMimeType());
        assertNull(complexData.getData());
        verify(storageService).getMetadata("image-storage-key");
    }

    @Test
    public void getObs_shouldWrapIOExceptionInUncheckedIOException() throws IOException {
        Obs obs = obsWithKey();
        IOException cause = new IOException("storage unavailable");
        when(storageService.exists("image-storage-key")).thenReturn(true);
        when(storageService.getMetadata("image-storage-key")).thenThrow(cause);

        try {
            handler.getObs(obs, "ignored-view");
            fail("Expected UncheckedIOException");
        } catch (UncheckedIOException e) {
            assertSame(cause, e.getCause());
        }
    }

    @Test
    public void saveObs_shouldReturnTheSameObsUnchanged() {
        Obs obs = obsWithKey();

        assertSame(obs, handler.saveObs(obs));
        assertNull(obs.getComplexData());
    }
}

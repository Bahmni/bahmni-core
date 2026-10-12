package org.bahmni.module.bahmnicore.contract;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class FormDraftResponseTest {

    @Test
    public void noArgConstructor_shouldCreateEmptyResponse() {
        FormDraftResponse response = new FormDraftResponse();
        assertNull(response.getUuid());
        assertNull(response.getFormData());
        assertNull(response.getMarkedAsSaved());
        assertNull(response.getTimestamp());
    }

    @Test
    public void uuidConstructor_shouldSetUuidOnly() {
        FormDraftResponse response = new FormDraftResponse("test-uuid");
        assertEquals("test-uuid", response.getUuid());
        assertNull(response.getFormData());
        assertNull(response.getMarkedAsSaved());
        assertNull(response.getTimestamp());
    }

    @Test
    public void uuidAndFormDataConstructor_shouldSetBothFields() {
        FormDraftResponse response = new FormDraftResponse("test-uuid", "{\"key\":\"value\"}");
        assertEquals("test-uuid", response.getUuid());
        assertEquals("{\"key\":\"value\"}", response.getFormData());
        assertNull(response.getMarkedAsSaved());
        assertNull(response.getTimestamp());
    }

    @Test
    public void threeArgConstructor_shouldSetUuidFormDataAndMarkedAsSaved() {
        FormDraftResponse response = new FormDraftResponse("test-uuid", "{\"key\":\"value\"}", true);
        assertEquals("test-uuid", response.getUuid());
        assertEquals("{\"key\":\"value\"}", response.getFormData());
        assertTrue(response.getMarkedAsSaved());
        assertNull(response.getTimestamp());
    }

    @Test
    public void fourArgConstructor_shouldSetAllFields() {
        FormDraftResponse response = new FormDraftResponse("test-uuid", "{\"key\":\"value\"}", false, 1234567890L);
        assertEquals("test-uuid", response.getUuid());
        assertEquals("{\"key\":\"value\"}", response.getFormData());
        assertFalse(response.getMarkedAsSaved());
        assertEquals(Long.valueOf(1234567890L), response.getTimestamp());
    }

    @Test
    public void setUuid_shouldUpdateUuid() {
        FormDraftResponse response = new FormDraftResponse();
        response.setUuid("new-uuid");
        assertEquals("new-uuid", response.getUuid());
    }

    @Test
    public void setFormData_shouldUpdateFormData() {
        FormDraftResponse response = new FormDraftResponse();
        response.setFormData("{\"updated\":true}");
        assertEquals("{\"updated\":true}", response.getFormData());
    }

    @Test
    public void setMarkedAsSaved_shouldUpdateFlag() {
        FormDraftResponse response = new FormDraftResponse();
        response.setMarkedAsSaved(true);
        assertTrue(response.getMarkedAsSaved());
    }

    @Test
    public void setTimestamp_shouldUpdateTimestamp() {
        FormDraftResponse response = new FormDraftResponse();
        response.setTimestamp(9999L);
        assertEquals(Long.valueOf(9999L), response.getTimestamp());
    }
}

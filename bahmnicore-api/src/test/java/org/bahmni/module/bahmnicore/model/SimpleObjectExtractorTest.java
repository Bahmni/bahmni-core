package org.bahmni.module.bahmnicore.model;

import org.junit.Test;
import org.openmrs.module.webservices.rest.SimpleObject;

import static org.junit.Assert.assertEquals;

public class SimpleObjectExtractorTest {
    @Test
    public void shouldReturnDefaultValueWhenKeyDoesNotExist() {
        SimpleObjectExtractor simpleObjectExtractor = new SimpleObjectExtractor(new SimpleObject().add("foo", 4));

        assertEquals(0, (int)simpleObjectExtractor.getValueOrDefault("bar", int.class));
    }
}

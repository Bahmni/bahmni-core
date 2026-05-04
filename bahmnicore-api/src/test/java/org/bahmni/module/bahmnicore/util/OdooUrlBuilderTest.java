package org.bahmni.module.bahmnicore.util;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Enclosed.class)
public class OdooUrlBuilderTest {

    public static class BasicTests {

        @Test
        public void buildAuthenticationUrl_shouldReturnUrlWithSessionAuthenticateEndpoint() {
            String url = OdooUrlBuilder.buildAuthenticationUrl();

            assertTrue(url.endsWith("/web/session/authenticate"));
            assertFalse(url.isEmpty());
        }

        @Test
        public void buildAvailableStocksUrl_shouldIncludeProductUuidParam() {
            String productUuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

            String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, null);

            assertTrue(url.contains("/api/get-available-stocks"));
            assertTrue(url.contains("product_uuid=" + productUuid));
        }

        @Test
        public void buildAvailableStocksUrl_shouldIncludeBothParamsWhenLocationUuidProvided() {
            String productUuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";
            String locationUuid = "7672b695-1872-40de-9ae8-a2bb38038208";

            String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, locationUuid);

            assertTrue(url.contains("product_uuid=" + productUuid));
            assertTrue(url.contains("location_uuid=" + locationUuid));
        }
    }

    @RunWith(Parameterized.class)
    public static class OmitLocationUuidTests {

        private final String locationUuidInput;

        public OmitLocationUuidTests(String locationUuidInput) {
            this.locationUuidInput = locationUuidInput;
        }

        @Parameterized.Parameters
        public static Collection<Object[]> data() {
            return Arrays.asList(new Object[][]{
                    {null},
                    {""},
                    {"   "}
            });
        }

        @Test
        public void buildAvailableStocksUrl_shouldOmitLocationUuidWhenBlankNullOrEmpty() {
            String productUuid = "6c8fa2a3-5714-466d-b83c-ce3c9f58641f";

            String url = OdooUrlBuilder.buildAvailableStocksUrl(productUuid, locationUuidInput);

            assertFalse(url.contains("location_uuid"));
            assertTrue(url.contains("product_uuid=" + productUuid));
        }
    }
}

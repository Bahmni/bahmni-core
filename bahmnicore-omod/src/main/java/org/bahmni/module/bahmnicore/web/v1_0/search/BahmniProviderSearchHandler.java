package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmrs.Provider;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;
import org.openmrs.customdatatype.CustomDatatype;
import org.openmrs.customdatatype.CustomDatatypeUtil;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.AlreadyPaged;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class BahmniProviderSearchHandler implements SearchHandler {

    public static final String PARAM_ATTRIBUTE_NAME = "attrName";
    public static final String PARAM_ATTRIBUTE_VALUE = "attrValue";
    public static final String PARAM_INCLUDE_ALL = "includeAll";
    public static final String SEARCH_BY_ATTRIBUTE = "byAttribute";
    public static final String ERR_UNDEFINED_ATTRIBUTE_NAME = "Undefined attribute %s";
    public static final String INVALID_ATTRIBUTE_VALUE = "Invalid attribute value for %s";

    private static final Logger log = LogManager.getLogger(BahmniProviderSearchHandler.class);
    public static final String INVALID_ATTRIBUTE_TYPE_DEFINITION = "Invalid Attribute type definition for %s";

    @Autowired
    ProviderService providerService;

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder("Allows you to find providers by attribute")
                                          .withRequiredParameters(PARAM_ATTRIBUTE_NAME, PARAM_ATTRIBUTE_VALUE)
                                          .build();
        return new SearchConfig(SEARCH_BY_ATTRIBUTE,
                                       RestConstants.VERSION_1 + "/provider",
                                       Arrays.asList("2.0.* - 2.*"),
                                       searchQuery);
    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {
        String attributeName = requestContext.getParameter(PARAM_ATTRIBUTE_NAME);
        String attributeValue = requestContext.getParameter(PARAM_ATTRIBUTE_VALUE);
        String includeAllStr = requestContext.getParameter(PARAM_INCLUDE_ALL);

        String query = requestContext.getParameter("q");
        boolean includeRetired = false;
        if (!StringUtils.isEmpty(includeAllStr)) {
            includeRetired = Boolean.getBoolean(includeAllStr);
        }

        Map<ProviderAttributeType, Object> attributeTypeObjectMap = getProviderAttributeTypeObjectMap(attributeName, attributeValue);

        List<Provider> providers = providerService.getProviders(query, requestContext.getStartIndex(), requestContext.getLimit(), attributeTypeObjectMap, includeRetired);
        return new AlreadyPaged<>(requestContext, providers, false);
    }

    private Map<ProviderAttributeType, Object> getProviderAttributeTypeObjectMap(String attributeName, String attributeValue) {
        ProviderAttributeType attributeType = findProviderAttributeType(attributeName);
        if (attributeType == null) {
            throw new IllegalArgumentException(String.format(ERR_UNDEFINED_ATTRIBUTE_NAME, attributeName));
        }

        CustomDatatype<?> attrDataType = CustomDatatypeUtil.getDatatype(attributeType.getDatatypeClassname(), attributeType.getDatatypeConfig());
        if (attrDataType == null) {
            throw new IllegalArgumentException(String.format(INVALID_ATTRIBUTE_TYPE_DEFINITION, attributeName));
        }

        try {
            Object value = attrDataType.fromReferenceString(attributeValue);
            Map<ProviderAttributeType, Object> attributeTypeObjectMap = new HashMap<>();
            if (attributeType != null) {
                attributeTypeObjectMap.put(attributeType, value);
            }
            return attributeTypeObjectMap;

        } catch (InvalidCustomValueException e) {
            String errorMessage = String.format(INVALID_ATTRIBUTE_VALUE, attributeName);
            log.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private ProviderAttributeType findProviderAttributeType(String attributeName) {
        List<ProviderAttributeType> allProviderAttributeTypes = providerService.getAllProviderAttributeTypes();
        for (ProviderAttributeType attributeType : allProviderAttributeTypes) {
            boolean result = attributeType.getName().equalsIgnoreCase(attributeName);
            if (result) {
                return attributeType;
            }
        }
        return null;
    }
}

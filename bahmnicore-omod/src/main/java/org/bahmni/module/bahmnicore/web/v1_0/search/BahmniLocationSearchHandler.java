package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.api.LocationService;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class BahmniLocationSearchHandler implements SearchHandler{

    private LocationService locationService;

    @Autowired
    public BahmniLocationSearchHandler(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public SearchConfig getSearchConfig() {
        return new SearchConfig("byTags", RestConstants.VERSION_1 + "/location", Arrays.asList("1.9.* - 2.*"),
                new SearchQuery.Builder("Allows you to find locations by tags attached to the location").withRequiredParameters("tags").build());

    }

    @Override
    public PageableResult search(RequestContext requestContext) throws ResponseException {
        String[] tagNames = requestContext.getRequest().getParameterMap().get("tags");
        String operator = requestContext.getParameter("operator");
        List<LocationTag> tags = new ArrayList<>();
        List<Location> locations = null;
        for (String tagName : tagNames) {
            tags.add(locationService.getLocationTagByName(tagName));
        }
        if(null == operator || "ALL".equals(operator)){
            locations = locationService.getLocationsHavingAllTags(tags);
        }
        if("ANY".equals(operator)){
            locations = locationService.getLocationsHavingAnyTag(tags);
        }
        return new AlreadyPaged<>(requestContext, locations, false);
    }
}

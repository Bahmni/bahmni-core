package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.Location;
import org.openmrs.LocationTag;
import org.openmrs.module.emrapi.EmrApiConstants;

public class LocationBuilder {
    private Location location;

    public LocationBuilder() {
        this.location = new Location();
    }

    public LocationBuilder withVisitLocationTag() {
        location.addTag(new LocationTag(EmrApiConstants.LOCATION_TAG_SUPPORTS_VISITS, "Visit Location"));
        return this;
    }

    public LocationBuilder withParent(Location parentLocation) {
        location.setParentLocation(parentLocation);
        return this;
    }

    public Location build() {
        return location;
    }
}
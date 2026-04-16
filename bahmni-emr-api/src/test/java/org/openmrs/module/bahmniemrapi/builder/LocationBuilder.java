/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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
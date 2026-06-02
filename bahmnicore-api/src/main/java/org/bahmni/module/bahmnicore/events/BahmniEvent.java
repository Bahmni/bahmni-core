/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.events;

import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

import java.time.LocalDateTime;
import java.util.UUID;

public class BahmniEvent {

    private static final long version = 1L;
    public UserContext userContext;
    public String eventId;
    public BahmniEventType eventType;
    public String payloadId;
    public LocalDateTime publishedDateTime;

    public BahmniEvent(BahmniEventType bahmniEventType) {
        this.eventType = bahmniEventType;
        this.eventId = UUID.randomUUID().toString();
        this.publishedDateTime = LocalDateTime.now();
        this.userContext= Context.getUserContext();
        this.payloadId="";
    }
}


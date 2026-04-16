/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.util;

import org.openmrs.module.webservices.rest.SimpleObject;

import java.util.LinkedHashMap;

public class WebUtils {

    public static SimpleObject wrapErrorResponse(String code, String reason) {
        LinkedHashMap map = new LinkedHashMap();
        if (reason != null && !"".equals(reason)) {
            map.put("message", reason);
        }
        if (code != null && !"".equals(code)) {
            map.put("code", code);
        }
        return (new SimpleObject()).add("error", map);
    }

}

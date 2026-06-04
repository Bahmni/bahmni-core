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

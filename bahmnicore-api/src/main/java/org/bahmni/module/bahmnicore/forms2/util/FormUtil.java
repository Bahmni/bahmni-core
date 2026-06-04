package org.bahmni.module.bahmnicore.forms2.util;

import org.openmrs.Obs;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class FormUtil {

    public static String getFormNameFromFieldPath(String formFieldPath) {
        return isNotBlank(formFieldPath) && formFieldPath.contains(".") ?
                formFieldPath.substring(0, formFieldPath.indexOf(".")) : "";
    }

    public static int getFormVersionFromFieldPath(String formFieldPath) {
        String formVersion = "";
        if (isNotBlank(formFieldPath) && formFieldPath.contains(".") && formFieldPath.contains("/")) {
            formVersion = formFieldPath.substring(formFieldPath.indexOf(".") + 1, formFieldPath.indexOf("/"));
        }
        return isNotBlank(formVersion) ? Integer.parseInt(formVersion) : 0;
    }

    public static List<Obs> filterFormBuilderObs(List<Obs> observations) {
        return observations != null ? observations.stream().filter(obs -> isNotBlank(obs.getFormFieldPath()))
                .collect(Collectors.toList()) : Collections.emptyList();
    }

    public static String getParentFormFieldPath(String formFieldPath) {
        return isNotBlank(formFieldPath) && formFieldPath.contains("/")
                ? formFieldPath.substring(0, formFieldPath.lastIndexOf("/")) : "";
    }
}


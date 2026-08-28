package org.bahmni.module.bahmnicore.openmrsadvice;

import org.openmrs.api.context.Context;

public abstract class BaseAdvice {

    protected abstract String getDefaultUrlTemplate();

    protected abstract String getEventRaiseFlagGlobalProperty();

    protected abstract String getUrlTemplateGlobalProperty();

    protected String getUrlPattern(String uuid) {
        String urlPattern = Context.getAdministrationService().getGlobalProperty(getUrlTemplateGlobalProperty());
        if (urlPattern == null || urlPattern.isEmpty()) {
            urlPattern = getDefaultUrlTemplate();
        }
        return urlPattern.replace("{uuid}", uuid);
    }

    protected boolean shouldRaiseEvent() {
        return Boolean.valueOf(Context.getAdministrationService().getGlobalProperty(getEventRaiseFlagGlobalProperty()));
    }
}

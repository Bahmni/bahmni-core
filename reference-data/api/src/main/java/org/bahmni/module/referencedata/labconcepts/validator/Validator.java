/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.referencedata.labconcepts.validator;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIException;

import java.util.List;

public class Validator {
    public void throwExceptionIfExists(List<String> errors) {
        String message = StringUtils.join(errors, "\n");
        if (!StringUtils.isBlank(message)) {
            throw new APIException(message);
        }
    }
}

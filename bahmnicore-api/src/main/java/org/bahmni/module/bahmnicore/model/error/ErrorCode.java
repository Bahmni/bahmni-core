/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.model.error;

public class ErrorCode {
    public static int DuplicatePatient = 1;
    public static int DuplicateCustomer = 2;
    public static int OpenERPError = 3;
    public static int OpenMRSError = 4;

    public static boolean duplicationError(int errorCode) {
        return errorCode == DuplicateCustomer || errorCode == DuplicatePatient;
    }
}

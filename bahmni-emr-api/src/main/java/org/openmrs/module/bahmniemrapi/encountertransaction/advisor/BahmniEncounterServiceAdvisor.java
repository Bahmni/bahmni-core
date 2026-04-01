/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.bahmniemrapi.encountertransaction.advisor;

import org.aopalliance.aop.Advice;
import org.openmrs.module.bahmniemrapi.encountertransaction.advice.BahmniEncounterTransactionUpdateAdvice;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;

import java.lang.reflect.Method;

public class BahmniEncounterServiceAdvisor extends StaticMethodMatcherPointcutAdvisor implements Advisor {
    private static final String SAVE_METHOD = "save";

    @Override
    public boolean matches(Method method, Class<?> aClass) {
        return SAVE_METHOD.equals(method.getName());
    }

    @Override
    public Advice getAdvice() {
        return new BahmniEncounterTransactionUpdateAdvice();
    }
}

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.bahmni.module.bahmnicore.web.filter;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

/**
 * Filter intended for all /ws/rest/* calls to sets Expires headers
 * based on global property bahmni.cacheHeadersFilter.expiresDuration
 */
public class CacheHeadersFilter implements Filter {

    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        log.debug("Initializing CacheHeadersFilter");
    }

    @Override
    public void destroy() {
        log.debug("Destroying CacheHeadersFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {

        chain.doFilter(request, response);

        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            if ("GET".equals(httpRequest.getMethod()) &&  httpResponse.getStatus() == 200) {
                int expiresDuration = NumberUtils.toInt(Context.getAdministrationService().getGlobalProperty("bahmni.cacheHeadersFilter.expiresDuration"), 0);
                log.debug(String.format("Setting expires header with duration %s", expiresDuration));
                httpResponse.setDateHeader("Expires", DateUtils.addMinutes(new Date(), expiresDuration).getTime());
             }
        }
    }
}



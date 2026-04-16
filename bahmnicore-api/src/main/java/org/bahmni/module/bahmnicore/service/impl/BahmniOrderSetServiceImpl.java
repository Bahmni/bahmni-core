/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.BahmniOrderSetDao;
import org.bahmni.module.bahmnicore.service.BahmniOrderSetService;
import org.openmrs.OrderSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Transactional
public class BahmniOrderSetServiceImpl implements BahmniOrderSetService {

    private  BahmniOrderSetDao bahmniOrderSetDao;

    @Autowired
    public BahmniOrderSetServiceImpl(BahmniOrderSetDao bahmniOrderSetDao) {
        this.bahmniOrderSetDao = bahmniOrderSetDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderSet> getOrderSetByQuery(String searchTerm) {
        return bahmniOrderSetDao.getOrderSetByQuery(searchTerm);
    }
}

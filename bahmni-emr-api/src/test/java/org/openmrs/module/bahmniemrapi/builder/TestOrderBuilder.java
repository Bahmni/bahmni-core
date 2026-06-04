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
package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.TestOrder;

import java.util.Date;
import java.util.UUID;

public class TestOrderBuilder {
    private TestOrder order;

    public TestOrderBuilder() {
        this.order = new TestOrder();
        this.order.setDateCreated(new Date());
        this.order.setUuid(UUID.randomUUID().toString());
        this.order.setDateCreated(new Date());
    }

    public TestOrderBuilder withUuid(UUID uuid) {
        order.setUuid(String.valueOf(uuid));
        return this;
    }

    public TestOrderBuilder withId(Integer id) {
        order.setId(id);
        return this;
    }

    public TestOrder build() {
        return order;
    }
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.bahmni.module.bahmnicore.model;

import org.openmrs.BaseOpenmrsData;

import java.io.Serializable;

public class NoteType extends BaseOpenmrsData implements Serializable {

    private Integer noteTypeId;

    private String name;

    private String description;
    public NoteType() {
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getNoteTypeId() {
        return noteTypeId;
    }
    public void setNoteTypeId(Integer noteTypeId) {
        this.noteTypeId = noteTypeId;
    }
    public Integer getId() {
        return getNoteTypeId();
    }

    public void setId(Integer id) {
        setNoteTypeId(id);
    }
}



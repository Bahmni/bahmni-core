-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

INSERT INTO concept (datatype_id,class_id,is_set,creator,date_created,changed_by,date_changed,uuid) VALUES (4,8,true,1,{ts '2013-07-23 11:26:35'},1,{ts '2013-07-23 11:26:35'},uuid());

select max(concept_id) from concept into @laboratory_concept_id;

insert into concept_name(concept_id, name, locale, locale_preferred,creator, date_created, concept_name_type, uuid) values (@laboratory_concept_id, 'Laboratory', 'en', true, 1, {ts '2013-07-23 11:26:35'}, 'FULLY_SPECIFIED', uuid());
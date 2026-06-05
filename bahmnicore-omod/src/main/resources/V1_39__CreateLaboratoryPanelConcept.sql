INSERT INTO concept (datatype_id,class_id,is_set,creator,date_created,changed_by,date_changed,uuid) VALUES (4,8,true,1,{ts '2013-07-23 11:26:35'},1,{ts '2013-07-23 11:26:35'},uuid());

select max(concept_id) from concept into @laboratory_concept_id;

insert into concept_name(concept_id, name, locale, locale_preferred,creator, date_created, concept_name_type, uuid) values (@laboratory_concept_id, 'Laboratory', 'en', true, 1, {ts '2013-07-23 11:26:35'}, 'FULLY_SPECIFIED', uuid());
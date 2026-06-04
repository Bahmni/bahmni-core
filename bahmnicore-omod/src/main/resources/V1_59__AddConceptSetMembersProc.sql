CREATE PROCEDURE add_concept_set_members (set_concept_id INT,
                              member_concept_id INT,weight INT)
BEGIN
	INSERT INTO concept_set (concept_id, concept_set,sort_weight,creator,date_created,uuid)
	values (member_concept_id, set_concept_id,weight,1, now(),uuid());
END;




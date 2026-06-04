CREATE PROCEDURE add_concept_answer (concept_id INT,
                              answer_concept_id INT,
                              sort_weight DOUBLE)
BEGIN
	INSERT INTO concept_answer (concept_id, answer_concept, answer_drug, date_created, creator, uuid, sort_weight) values (concept_id, answer_concept_id, null, now(), 1, uuid(), sort_weight);
END;
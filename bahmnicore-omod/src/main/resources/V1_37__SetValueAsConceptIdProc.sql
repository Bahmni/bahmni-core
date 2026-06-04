CREATE PROCEDURE set_value_as_concept_id (person_attribute_type_name VARCHAR(255))
BEGIN
  DECLARE c_id INT;
  DECLARE pa_id INT;
  DECLARE c_name VARCHAR(255);
  DECLARE val VARCHAR(255);
  DECLARE done INT DEFAULT FALSE;
  DECLARE cur1 CURSOR FOR SELECT person_attribute_id, value FROM person_attribute WHERE person_attribute_type_id IN
    (SELECT person_attribute_type_id from person_attribute_type where name = person_attribute_type_name) and value != '';

  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  CREATE TEMPORARY TABLE answer_concept_ids (id INT); 

  INSERT INTO answer_concept_ids SELECT answer_concept FROM concept_answer
     WHERE concept_id IN (SELECT BINARY foreign_key FROM person_attribute_type WHERE name = person_attribute_type_name);
  
  OPEN cur1;
  REPEAT
    FETCH cur1 INTO pa_id, val;
      SELECT concept_id INTO c_id FROM concept_name 
           WHERE lower(name) = lower(val) AND concept_name_type = 'FULLY_SPECIFIED' 
           AND concept_id IN (SELECT id FROM answer_concept_ids);
      UPDATE person_attribute set value = c_id where person_attribute_id = pa_id;
  UNTIL done END REPEAT;
 CLOSE cur1;
 DROP TABLE answer_concept_ids;
END

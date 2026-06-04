CREATE PROCEDURE introduce_new_address_level(parent_field_name VARCHAR(160), new_field_name VARCHAR(160), new_field_address_field_name VARCHAR(160))
introduce_new_address_level_proc: BEGIN
  DECLARE done INT DEFAULT FALSE;
  DECLARE parent_field_level_id INT;
  DECLARE parent_field_entry_id INT;
  DECLARE new_field_level_id INT;
  DECLARE new_field_entry_id INT;
  DECLARE number_children_fields_for_parent_field INT;
  DECLARE parent_field_entries_cursor CURSOR FOR SELECT id from parent_field_ids;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  SELECT address_hierarchy_level_id INTO parent_field_level_id from address_hierarchy_level where name = parent_field_name;
  INSERT INTO address_hierarchy_level(name, address_field, uuid, required) values(new_field_name, new_field_address_field_name, UUID(), false);

  select COUNT(*) INTO number_children_fields_for_parent_field from address_hierarchy_level where parent_level_id = parent_field_level_id;

  SELECT address_hierarchy_level_id INTO new_field_level_id from address_hierarchy_level where name = new_field_name;
  UPDATE address_hierarchy_level set parent_level_id = new_field_level_id where parent_level_id = parent_field_level_id;
  UPDATE address_hierarchy_level set parent_level_id = parent_field_level_id where name = new_field_name;

  -- If parent field was leaf node no address entry migration required
  IF (number_children_fields_for_parent_field = 0)THEN
	LEAVE introduce_new_address_level_proc;
  END IF;

  -- Start address entry migration
  CREATE TEMPORARY TABLE parent_field_ids(id INT); 
  INSERT INTO parent_field_ids SELECT address_hierarchy_entry_id from address_hierarchy_entry where level_id = parent_field_level_id;
  
  OPEN parent_field_entries_cursor;
  read_loop: LOOP
    FETCH parent_field_entries_cursor INTO parent_field_entry_id;
	IF done THEN
      LEAVE read_loop;
    END IF;
    INSERT INTO address_hierarchy_entry (name, level_id, parent_id, uuid) VALUES (NULL, new_field_level_id, parent_field_entry_id, UUID());
	SET new_field_entry_id = LAST_INSERT_ID();
	UPDATE address_hierarchy_entry SET parent_id = new_field_entry_id where parent_id = parent_field_entry_id and level_id != new_field_level_id;
  END LOOP;
  CLOSE parent_field_entries_cursor;
  DROP TABLE parent_field_ids;
END;

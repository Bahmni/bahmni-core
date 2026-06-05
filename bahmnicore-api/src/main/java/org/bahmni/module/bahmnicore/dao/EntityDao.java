package org.bahmni.module.bahmnicore.dao;

public interface EntityDao {

    public <T> T getByUuid(String uuid, Class<T> className);
}

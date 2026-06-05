package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.dao.BahmniAddressHierarchyDao;
import org.bahmni.module.bahmnicore.model.BahmniAddressHierarchyEntry;
import org.bahmni.module.bahmnicore.model.BahmniAddressHierarchyLevel;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BahmniAddressHierarchyDaoImpl implements BahmniAddressHierarchyDao {
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<BahmniAddressHierarchyEntry> getAddressHierarchyEntriesByUuid(List<String> uuids) {
        Session currentSession = sessionFactory.getCurrentSession();
        List<BahmniAddressHierarchyEntry> bahmniAddressHierarchyEntries;
        StringBuilder queryString = new StringBuilder("select ahe.address_hierarchy_entry_id as addressHierarchyEntryId, ahe.parent_id as parentId, ahe.uuid as uuid, ahe.level_id as levelId, " +
                " ahe.user_generated_id as userGeneratedId, ahe.name as name from address_hierarchy_entry ahe " +
                "where ahe.uuid in (:uuids) ");

        SQLQuery sqlQuery = currentSession
                .createSQLQuery(queryString.toString())
                .addScalar("uuid", StandardBasicTypes.STRING)
                .addScalar("addressHierarchyEntryId", StandardBasicTypes.INTEGER)
                .addScalar("parentId", StandardBasicTypes.INTEGER)
                .addScalar("levelId", StandardBasicTypes.INTEGER)
                .addScalar("userGeneratedId", StandardBasicTypes.STRING)
                .addScalar("name", StandardBasicTypes.STRING);

        sqlQuery.setParameterList("uuids", uuids);
        sqlQuery.setResultTransformer(Transformers.aliasToBean(BahmniAddressHierarchyEntry.class));

        bahmniAddressHierarchyEntries = (List<BahmniAddressHierarchyEntry>) sqlQuery.list();
        for(BahmniAddressHierarchyEntry bahmniAddressHierarchyEntry: bahmniAddressHierarchyEntries){
            bahmniAddressHierarchyEntry.setAddressHierarchyLevel(getAddressHierarchyLevelById(bahmniAddressHierarchyEntry.getLevelId()));
        }

        return bahmniAddressHierarchyEntries;
    }

    private BahmniAddressHierarchyLevel getAddressHierarchyLevelById(Integer levelId) {

        Session currentSession = sessionFactory.getCurrentSession();
        StringBuilder queryString = new StringBuilder("select ahl.address_hierarchy_level_id as levelId, ahl.parent_level_id as parentLevelId, ahl.uuid as uuid, ahl.required as required, " +
                " ahl.address_field as addressField, ahl.name as name from address_hierarchy_level ahl " +
                "where ahl.address_hierarchy_level_id = (:levelId) ");

        SQLQuery sqlQuery = currentSession
                .createSQLQuery(queryString.toString())
                .addScalar("uuid", StandardBasicTypes.STRING)
                .addScalar("parentLevelId", StandardBasicTypes.INTEGER)
                .addScalar("required", StandardBasicTypes.BOOLEAN)
                .addScalar("levelId", StandardBasicTypes.INTEGER)
                .addScalar("addressField", StandardBasicTypes.STRING)
                .addScalar("name", StandardBasicTypes.STRING);

        sqlQuery.setParameter("levelId", levelId);
        sqlQuery.setResultTransformer(Transformers.aliasToBean(BahmniAddressHierarchyLevel.class));

        return (BahmniAddressHierarchyLevel) sqlQuery.uniqueResult();
    }
}

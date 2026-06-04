package org.bahmni.module.obsrelationship.dao.impl;

import org.bahmni.module.obsrelationship.dao.ObsRelationshipDao;
import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.openmrs.Obs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class ObsRelationshipDaoImpl implements ObsRelationshipDao {
    /**
     * Hibernate session factory
     */

    private SessionFactory sessionFactory;

    /**
     * Set session factory
     *
     * @param sessionFactory
     */
    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    @Transactional
    public ObsRelationship saveOrUpdate(ObsRelationship obsRelationship) {
        sessionFactory.getCurrentSession().saveOrUpdate(obsRelationship);
        return obsRelationship;
    }

    @Override
    @Transactional
    public ObsRelationshipType saveOrUpdateRelationshipType(ObsRelationshipType obsRelationshipType) {
        sessionFactory.getCurrentSession().saveOrUpdate(obsRelationshipType);
        return obsRelationshipType;
    }

    @Override
    @Transactional
    public ObsRelationship getRelationByUuid(String uuid) {
        Query query = sessionFactory.getCurrentSession().createQuery("from ObsRelationship where uuid=:uuid");
        query.setString("uuid",uuid);
        List<ObsRelationship> list = query.list();
        if(list.size() != 0){
            return list.get(0);
        }
        return null;
    }

    @Override
    @Transactional
    public List<ObsRelationship> getRelationsBy(Obs sourceObs, Obs targetObs) {
        Query query = createGetRelationsQueryFor(sourceObs, targetObs);
        List<ObsRelationship> obsRelationshipList = query.list();
        return obsRelationshipList;
    }

    @Override
    @Transactional
    public List<ObsRelationshipType> getAllRelationshipTypes() {
        Query query = sessionFactory.getCurrentSession().createQuery("from ObsRelationshipType");
        return query.list();
    }

    @Override
    @Transactional
    public ObsRelationshipType getRelationshipTypeByName(String name) {
        Query query = sessionFactory.getCurrentSession().createQuery("from ObsRelationshipType where name=:name");
        query.setString("name", name);
        List<ObsRelationshipType> obsRelationshipTypes = query.list();
        if(obsRelationshipTypes.size()>0){
            return (ObsRelationshipType) query.list().get(0);
        }
        return null;
    }

    @Override
    @Transactional
    public List<ObsRelationship> getRelationsWhereSourceObsInEncounter(String encounterUuid) {
        Query query = sessionFactory.getCurrentSession().createQuery("from ObsRelationship obsRel where obsRel.sourceObs.encounter.uuid =:encounterUuid");
        query.setString("encounterUuid", encounterUuid);
        return query.list();
    }

    @Override
    public List<ObsRelationship> getObsRelationshipsByTargetObsUuid(String targetObsUuid) {
        Query query = sessionFactory.getCurrentSession().createQuery("from ObsRelationship obsRel where obsRel.targetObs.uuid =:targetObsUuid");
        query.setString("targetObsUuid", targetObsUuid);
        return query.list();
    }

    private Query createGetRelationsQueryFor(Obs sourceObs, Obs targetObs) {
        Query query = null;
        if(sourceObs == null && targetObs == null){
            throw new IllegalArgumentException("SourceObs and TargetObs are both null in method getRelationByUuid()");
        }
        if(sourceObs == null ){
            query = sessionFactory.getCurrentSession().createQuery("from ObsRelationship where targetObs=:targetObs");
            query.setInteger("targetObs", targetObs.getId());
        }
        else if(targetObs == null){
            query = sessionFactory.getCurrentSession().createQuery("from ObsRelationship where sourceObs=:sourceObs");
            query.setInteger("sourceObs", sourceObs.getId());
        }
        else{
            query = sessionFactory.getCurrentSession().createQuery("from ObsRelationship where sourceObs=:sourceObs and targetObs=:targetObs");
            query.setInteger("sourceObs", sourceObs.getId());
            query.setInteger("targetObs", targetObs.getId());
        }
        return query;
    }

}

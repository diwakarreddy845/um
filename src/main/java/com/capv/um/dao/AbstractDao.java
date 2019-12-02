package com.capv.um.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
 /**
  * <h1>AbstractDao</h1>
  * this dao class is used to defines simple CRUD methods.
  * @author narendra.muttevi
  * @version 1.0
  */
public abstract class AbstractDao implements Dao {
 
    @Autowired
    private SessionFactory sessionFactory;
    
    @Autowired
	private SessionFactory sessionFactoryForOpenFire;
 
    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }
    
    protected Session getOpenFireSession() {
    	return sessionFactoryForOpenFire.getCurrentSession();
    }
 
    /**
     * this method is used to save user
     * @param entity -the object
     */
    public void save(Object entity) {
        getSession().save(entity);
    }
    

    /**
     * this method is used to update user
     * @param entity -the object
     */
    public void update(Object entity) {
        getSession().update(entity);
    }
 
    /**
     * this method is used to delete user
     * @param entity -the object
     */
    public void delete(Object entity) {
        getSession().delete(entity);
    }
    
    /**
     * this method is used to get the record
     * @param classInstance -- instance of a class
     * @param id -- the id
     */
    @SuppressWarnings("rawtypes")
	public Object get(Class classInstance, Serializable id) {
       return getSession().get(classInstance, id);
    }
    
    /**
     * this method is used to get  list of records
     * @param classInstance -- instance of a class
     */
    @SuppressWarnings("rawtypes")
	public List getAll(Class classInstance) {
		
		Session session = getSession();
		
		Criteria criteria = session.createCriteria(classInstance);
		
		return criteria.list();
	}
    
    /**
     * this method is used to get Unique entity by matching properties
     * @param classInstance -- instance of a class
     * @param properties the properties
     */
    @SuppressWarnings("rawtypes")
	public Object getUniqueEntityByMatchingProperties(Class classInstance, 
    											Map<String, Object> properties) {
    	
    	Session session = getSession();
		
		Criteria criteria = session.createCriteria(classInstance);
		
		for(String propName :properties.keySet()) {
			criteria.add(Restrictions.eq(propName, properties.get(propName)));
		}
		
		return criteria.uniqueResult();
    }
    
    /**
     * this method is used to get get entities by matching properties
     * @param classInstance -- instance of a class
     * @param properties the properties
     */
    @SuppressWarnings("rawtypes")
    public List getEntitiesByMatchingProperties(Class classInstance, Map<String, Object> properties) {
    	
    	Session session = getSession();
		
		Criteria criteria = session.createCriteria(classInstance);
		
		for(String propName :properties.keySet()) {
			criteria.add(Restrictions.eq(propName, properties.get(propName)));
		}
		
		return criteria.list();
    }
}

package com.capv.um.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * <h1>Dao</h1>
 * this interface is used to perform  crud operations
 * @author narendra.muttevi
 * @version 1.0
 */
public interface Dao {

	/**
	 * this method is used to save entity
	 * @param entity the object
	 */
	void save(Object entity);
	
	/**
	 * this method is used to update entity
	 * @param entity the object
	 */
	void update(Object entity);
	
	/**
	 * this method is used to delete entity
	 * @param entity the object
	 */
	void delete(Object entity);
	
	 /**
     * this method is used to get the record
     * @param classInstance -- instance of a class
     * @param id -- the id
     * @return record based on id
     */
	@SuppressWarnings("rawtypes")
	Object get(Class classInstance, Serializable id);
	
	 /**
     * this method is used to get  list of records
     * @param classInstance -- instance of a class
     * @return list of users
     */
	@SuppressWarnings("rawtypes")
	List getAll(Class classInstance);
	
	/**
     * this method is used to get Unique entity by matching properties
     * @param classInstance -- instance of a class
     * @param properties the properties
     * @return Unique Entity By Matching Properties
     */ 
	@SuppressWarnings("rawtypes")
	Object getUniqueEntityByMatchingProperties(Class classInstance, Map<String, Object> properties);
	
	 /**
     * this method is used to get get entities by matching properties
     * @param classInstance -- instance of a class
     * @param properties the properties
     * @return Entities By Matching Properties
     */
	@SuppressWarnings("rawtypes")
	List getEntitiesByMatchingProperties(Class classInstance, Map<String, Object> properties);
}

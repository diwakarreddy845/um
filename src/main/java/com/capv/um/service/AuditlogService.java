package com.capv.um.service;

import com.capv.um.model.AuditLog;



/**
 * {@link AuditlogService} interface defines the methods to save {@link Auditlog} class's object
 * 
 * @author caprusit
 * @version 1.0
 */
public interface AuditlogService {
	/**
	 * This method is used to save the {@link Auditlog} class Object
	 * @param log specifies the {@link Auditlog} class's object that will we saved
	 */
	void saveLog(AuditLog log) ;
}

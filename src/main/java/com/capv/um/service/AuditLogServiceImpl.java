package com.capv.um.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.AuditLog;
import com.capv.um.repository.AuditlogRepository;

/**
 * AuditLogServiceImpl class implements the methods of {@link AuditlogService} interface to save the {@link Auditlog} class's object
 * 
 * @author caprusit
 * @version 1.0
 */
@Service
@Transactional("transactionManager")
public class AuditLogServiceImpl implements AuditlogService {

	@Autowired
	AuditlogRepository auditlogRepository;

	/**
	 * This method is used to save the {@link Auditlog} class Object
	 * 
	 * @param log specifies the {@link Auditlog} class's object that will we saved
	 */
	@Override
	public void saveLog(AuditLog log) {
		auditlogRepository.save(log);
	}
}

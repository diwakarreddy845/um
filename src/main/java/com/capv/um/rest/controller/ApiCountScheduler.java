package com.capv.um.rest.controller;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.capv.um.model.ApiCount;
import com.capv.um.service.ApiCountService;
import com.capv.um.spring.aop.LoggerConfig;

@Component
public class ApiCountScheduler {

	@Autowired
	ApiCountService apiCountService;
	
	@Scheduled(cron="*/3600 * * * * ?")
	void saveApiCount(){
			 apiCountService.saveApiCount(LoggerConfig.auditMap);
			 LoggerConfig.auditMap=new HashMap<Long, List<ApiCount>>();
		 
	}
}

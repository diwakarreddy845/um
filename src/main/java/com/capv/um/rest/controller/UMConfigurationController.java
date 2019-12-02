package com.capv.um.rest.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.util.ServiceStatus;

@RestController
public class UMConfigurationController {
	
	private static final Logger log = LoggerFactory.getLogger(UMConfigurationController.class);

	@RequestMapping(value = "/conf/reLoadConfiguration/{clientId}", method = RequestMethod.GET, produces = {"application/json" })
	public ServiceStatus<Object> getValidTryItRooms(@PathVariable("clientId") String clientId) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();	
			try{
						serviceStatus.setStatus("success");
						serviceStatus.setMessage("RoomId sent sucessfully.");
						CapvClientUserUtil.loadClientConfigProperties(Long.parseLong(clientId));
						return serviceStatus;
				}catch(Exception exception){
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("Invalid Client Id ");
						log.error(exception.getMessage());
						return serviceStatus;	
				}
	}
}

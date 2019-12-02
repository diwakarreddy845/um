package com.capv.um.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.EnhancedApnsNotification;

@Service("apnsService")
public class APNSServiceImpl implements APNSService {

	@Autowired
	Environment environment;

	private static final Logger log = LoggerFactory.getLogger(APNSServiceImpl.class);
	
	@Override
	public void pushCallNotification(String deviceId,String msg,Long clientId) {
		
		//log.debug("Entered into apnsService pushCallNotification method");
		String apns_cert = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CONFIG_APNS_CERT);
		String apns_key = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CONFIG_APNS_KEY);
		ApnsService service = APNS.newService()
				.withCert(apns_cert,apns_key)
				.withSandboxDestination().build();
		try {
		service.push(deviceId, msg);}
		catch(Exception e) {
			log.debug("apns"+e);
		}
		log.debug("Exit from apnsService pushCallNotification method");
	}

	

}

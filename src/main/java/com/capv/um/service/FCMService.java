package com.capv.um.service;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.web.filter.HeaderRequestInterceptor;
import com.google.gson.JsonObject;
 
@Service("fcmService")
public class FCMService {
 
	
	@Autowired
	Environment environment;
	
	private static final Logger log = LoggerFactory.getLogger(FCMService.class);
	

	private static final String FIREBASE_API_URL = "https://fcm.googleapis.com/fcm/send";
	
	@Async
	public CompletableFuture<String> pushToFCM(HttpEntity<String> entity,Long clientId) {

		
	    String FIREBASE_SERVER_KEY = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CONFIG_FCM_KEY);//environment.getRequiredProperty("capv.fcm_server_key");
		RestTemplate restTemplate = new RestTemplate();
		
		ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
		interceptors.add(new HeaderRequestInterceptor("Authorization", "key=" + FIREBASE_SERVER_KEY));
		interceptors.add(new HeaderRequestInterceptor("Content-Type", "application/json"));
		restTemplate.setInterceptors(interceptors);
		String firebaseResponse="";
		try {
			firebaseResponse = restTemplate.postForObject(FIREBASE_API_URL, entity, String.class);
		}catch (Exception e) {
			log.error(e.getMessage());
		}
 
		return CompletableFuture.completedFuture(firebaseResponse);
	}
	public void sendMessage(String deviceId,JsonObject msg,Long clientId) {
		JsonObject body = new JsonObject();
		 
	        body.addProperty("to", deviceId);
	        body.addProperty("priority", "high");
	        
	        //JsonObject notification = new JsonObject();
	       // notification.addProperty("title", "Incomming Message");
	        
	     //body.add("notification", notification);
	     body.add("data", msg);
	     
		HttpEntity<String> request = new HttpEntity<>(body.toString());
		CompletableFuture<String> pushNotification = pushToFCM(request, clientId);
	}
}

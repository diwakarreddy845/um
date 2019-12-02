package com.capv.um.service;

public interface APNSService {
	
	void pushCallNotification(String deviceId,String msg,Long clientId);
	
}

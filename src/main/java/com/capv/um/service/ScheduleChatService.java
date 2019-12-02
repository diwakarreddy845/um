package com.capv.um.service;

import java.util.List;

import com.capv.um.model.ScheduleChat;

public interface ScheduleChatService {

	void save(ScheduleChat scheduleChat);
	
	ScheduleChat update(ScheduleChat scheduleChat);
	
	List<ScheduleChat> getAllScheduledChat();
	
	List<ScheduleChat> getTodayScheduledChat();
	
	void delete(ScheduleChat scheduleChat);
	
	List<ScheduleChat> getAllByUserName(String userName, String clientId);
}

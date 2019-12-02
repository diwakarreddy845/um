package com.capv.um.rest.controller;

import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capv.um.model.ScheduleChat;
import com.capv.um.service.ScheduleChatService;
import com.capv.um.util.ServiceStatus;
import com.google.gson.JsonElement;

@RestController
@RequestMapping(value = { "/scheduleChat" })
public class ScheduleChatController {

	@Autowired
	private ScheduleChatService scheduleChatservice;

	@RequestMapping(value = "/save", method = RequestMethod.PUT)
	public ServiceStatus<Object> create(@RequestBody ScheduleChat scheduleChat) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<>();
		try {
			scheduleChatservice.save(scheduleChat);
		} catch (Exception e) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid details.");
			return serviceStatus;
		}
		serviceStatus.setStatus("success");
		serviceStatus.setMessage("Schedule created sucessfully");
		return serviceStatus;
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public ServiceStatus<Object> update(@RequestBody ScheduleChat scheduleChat) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<>();
		if (scheduleChat == null) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Record not found");
			return serviceStatus;
		}
		try {
			scheduleChatservice.update(scheduleChat);
		} catch (Exception e) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid details.");
			return serviceStatus;
		}
		serviceStatus.setStatus("success");
		serviceStatus.setMessage("Schedule created sucessfully");
		return serviceStatus;
	}

	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ServiceStatus<Object> deleteUser(@RequestBody ScheduleChat scheduleChat) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<>();
		if (scheduleChat == null || scheduleChat.getId() == null) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Record not found");
			return serviceStatus;
		}
		try {
			scheduleChatservice.delete(scheduleChat);
		} catch (Exception e) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid details.");
			return serviceStatus;
		}
		serviceStatus.setStatus("success");
		serviceStatus.setMessage("Schedule deleted sucessfully");
		return serviceStatus;
	}

	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public List<ScheduleChat> getAll(@RequestParam String userName, @RequestParam String clientId) {
		return scheduleChatservice.getTodayScheduledChat();
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/allByClientid", method = RequestMethod.GET)
	public JSONObject getAllByClientId(@RequestParam String userName, @RequestParam String clientId) {
		List<ScheduleChat> list = scheduleChatservice.getAllByUserName(userName, clientId);
		JSONObject result = new JSONObject();
		result.put("result", list);
		return result;
	}
}

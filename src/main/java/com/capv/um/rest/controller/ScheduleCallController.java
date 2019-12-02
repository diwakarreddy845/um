package com.capv.um.rest.controller;


import java.sql.Date;
import java.util.List;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.capv.um.model.ScheduleCall;
import com.capv.um.service.ScheduleCallService;
import com.capv.um.util.ServiceStatus;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RestController
@RequestMapping(value = { "/scheduleCall" })
public class ScheduleCallController {

	@Autowired
	private ScheduleCallService scheduleCallservice;

	@RequestMapping(value = "/save", method = RequestMethod.PUT)
	public ServiceStatus<Object> create(@RequestBody ScheduleCall scheduleCall) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		try {
			scheduleCallservice.save(scheduleCall);
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
	public ServiceStatus<Object> update(@RequestBody ScheduleCall scheduleCall) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		if (scheduleCall == null) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Record not found");
			return serviceStatus;
		}
		try {
			scheduleCallservice.update(scheduleCall);
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
	public ServiceStatus<Object> deleteUser(@RequestBody ScheduleCall scheduleCall) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		if (scheduleCall == null || scheduleCall.getId() == null) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Record not found");
			return serviceStatus;
		}
		try {
			scheduleCallservice.delete(scheduleCall);
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
	public List<ScheduleCall> getAll(@RequestParam String userName, @RequestParam String clientId) {
		return scheduleCallservice.getAllByDate(new Date(0), 0);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/allByClientid", method = RequestMethod.GET)
	public JSONObject getAllByClientId(@RequestParam String userName, @RequestParam String clientId) {
		List<ScheduleCall> list = scheduleCallservice.getAllByUserName(userName, clientId);
		JSONObject result = new JSONObject();
		result.put("result", list);
		return result;
	}
}

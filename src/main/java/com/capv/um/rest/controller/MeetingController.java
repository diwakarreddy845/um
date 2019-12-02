package com.capv.um.rest.controller;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.capv.um.model.MeetingConfigDTO;
import com.capv.um.model.ScheduleMeeting;
import com.capv.um.service.MeetingConfigurationService;
import com.capv.um.service.ScheduleMeetingService;
import com.capv.um.util.ServiceStatus;

@RestController
public class MeetingController {
	
	private static final Logger log = LoggerFactory.getLogger(MeetingController.class);

	@Autowired
	MeetingConfigurationService meetingService;
	
	@Autowired 
	ScheduleMeetingService scheduleMeetingsService;
	
	@Deprecated
	@RequestMapping(value = "/pushMeetingProperties", method = RequestMethod.POST) 
	public ServiceStatus<Object> pushMeetingProperties(@RequestBody List<MeetingConfigDTO> meetingConfigDto,HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into pushMeetingProperties method ");
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage("Successfully Inserted Meeting Property Id");
		try {
			
			meetingService.insertMeetingConfigInfo(meetingConfigDto);
		} catch (Exception e) {
			log.error("Exception occured due to pushMeetingProperties :", e);
			serviceStatus.setStatus("success");
			serviceStatus.setMessage("Unbale to push meeting property id");
		}
       log.debug("Exit from pushMeetingProperties method");
	return serviceStatus;
	}

	@RequestMapping(value = "/scheduleMeeting", method = RequestMethod.POST) 
	public ServiceStatus<Object> scheduleMeeting(@RequestBody ScheduleMeeting meetingConfigDto,HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into scheduleMeeting method ");
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage("Successfully scheduleMeeting");
		
		try {
			String room_id=UUID.randomUUID().toString();
			serviceStatus.setResult(room_id);
			meetingConfigDto.setMeetingRoomId(room_id);
			scheduleMeetingsService.save(meetingConfigDto);
		} catch (Exception e) {
			log.error("Exception occured due to scheduleMeeting :", e);
			serviceStatus.setStatus("success");
			serviceStatus.setMessage("Unbale to push scheduleMeeting");
		}
       log.debug("Exit from scheduleMeeting method");
	return serviceStatus;
	}
	
	@RequestMapping(value = "/getScheduleMeetings", method = RequestMethod.GET,produces = {
	"application/json" }) 
	public List<ScheduleMeeting> scheduleMeeting(HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into scheduleMeeting method ");
		List<ScheduleMeeting> serviceStatus = new ArrayList<ScheduleMeeting>();
		try {
			serviceStatus=scheduleMeetingsService.getScheduleMeetingList();
			
		} catch (Exception e) {
			log.error("Exception occured due to scheduleMeeting :", e);
			
		}
       log.debug("Exit from scheduleMeeting method");
	return serviceStatus;
	}
	
	
}

package com.capv.um.rest.controller;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.capv.um.model.ClientNetworkDetails;
import com.capv.um.model.TimeZone;
import com.capv.um.service.ClientNetworkService;
import com.capv.um.service.TimeZoneService;
import com.capv.um.util.CapvUtil;
import com.capv.um.util.ServiceStatus;
/**
 * <h1>TurnController</h1>
 * this class is used to perform turn configuration settings
 * @author narendra.muttevi
 * @version 1.0
 */
@RestController
public class TimeZoneController {
	
	@Autowired
	private Environment environment;
	@Autowired
	private TimeZoneService timeZoneService;
	
	int i=0;
	
	private static final Logger log = LoggerFactory.getLogger(TimeZoneController.class);
	/**
	 * this method is used to perform turn configuration settings
	 * @param request - the HttpServletRequest
	 * @param response - the HttpServletResponse
	 * @return list of turn urls
	 * @throws Exception
	 */
	@RequestMapping(value ="/timezone/getTimeZone",  method = { RequestMethod.GET})
	public @ResponseBody List<TimeZone> getTimezone(HttpServletRequest request, HttpServletResponse response) throws Exception {
  
		log.debug("Entered into /turn method");
		ServiceStatus<Map<String, Object>> regstatus = new ServiceStatus<Map<String, Object>>();
		
		try {
			
			
				List<TimeZone> timeZoneList=timeZoneService.getAllTimeZones();
				regstatus.setMessage(CapvUtil.environment.getProperty("message.insufficientParameters"));
				regstatus.setStatus("failure");
				
				return timeZoneList;
			
		} catch (Exception e) {
			log.error("Exception occured while timezone : ",e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			regstatus.setMessage(e.getMessage());
			regstatus.setStatus("failure");
			
			return null;
		}
	
		
	}
	
}

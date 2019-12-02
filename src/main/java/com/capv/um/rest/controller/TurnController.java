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
import com.capv.um.service.ClientNetworkService;
import com.capv.um.util.CapvUtil;
import com.capv.um.util.ServiceStatus;
/**
 * <h1>TurnController</h1>
 * this class is used to perform turn configuration settings
 * @author narendra.muttevi
 * @version 1.0
 */
@RestController
public class TurnController {
	
	@Autowired
	private Environment environment;
	@Autowired
	private ClientNetworkService clientNetworkService;
	
	int i=0;
	
	private static final Logger log = LoggerFactory.getLogger(TurnController.class);
	/**
	 * this method is used to perform turn configuration settings
	 * @param request - the HttpServletRequest
	 * @param response - the HttpServletResponse
	 * @return list of turn urls
	 * @throws Exception
	 */
	@RequestMapping(value ="/turn",  method = { RequestMethod.POST})
	public @ResponseBody ServiceStatus<Map<String, Object>> getTurn(HttpServletRequest request, HttpServletResponse response) throws Exception {
  
		log.debug("Entered into /turn method");
		ServiceStatus<Map<String, Object>> regstatus = new ServiceStatus<Map<String, Object>>();
		
		try {
			
			String userName	= request.getParameter("userName");
			
			if(userName != null && userName.trim().length() > 0) {
				
				try {
					
					String urlString = environment.getProperty("videocall.url");
					
					if(urlString != null && urlString.trim().length() > 0) {
						
						String[] urls = urlString.split(",");
						
						int currentIndex = 0;
						
						if(i < urls.length){
							
							currentIndex = i;
							i++;
							if(i == urls.length)
								i = 0;
						} else {
							i = 0;
						}
						
						Map<String, Object> resultMap = new HashMap<>();
						List<String> urlList = new ArrayList<>();
						
						resultMap.put("userName", userName);
						resultMap.put("password", "test1");
						
						urlList.add(urls[currentIndex].concat(":3478?transport=udp"));
						urlList.add(urls[currentIndex].concat(":3478?transport=tcp"));
						urlList.add(urls[currentIndex].concat(":3479?transport=udp"));
						urlList.add(urls[currentIndex].concat(":3479?transport=tcp"));
						
						resultMap.put("uris", urlList);
						
						regstatus.setStatus("success");
						regstatus.setResult(resultMap);
						
					} else {
						
						regstatus.setStatus("failure");
						regstatus.setMessage(CapvUtil.environment.getProperty("message.turnUrlsCofigurationNotFound"));
						
					}
					return regstatus;
				}  catch (Exception e) {
					log.error("Exception occured while turn :",e);
					regstatus.setMessage(e.toString());
					regstatus.setStatus("failure");
					
					return regstatus;
				}
			} else {
				
				regstatus.setMessage(CapvUtil.environment.getProperty("message.insufficientParameters"));
				regstatus.setStatus("failure");
				
				return regstatus;
			}
			
		} catch (Exception e) {
			log.error("Exception occured while turn : ",e);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			regstatus.setMessage(e.getMessage());
			regstatus.setStatus("failure");
			
			return regstatus;
		}
	
		
	}
	@RequestMapping(value="/saveClientNetworkDetails", method = RequestMethod.POST, produces={"application/json"}, consumes={"application/json"})
	public ServiceStatus<Object> saveClientNetworkDetails(@RequestBody ClientNetworkDetails clientNetworkDetails,
																															HttpServletRequest request, HttpServletResponse response)
	{
			log.debug("Entered into saveClientNetworkDetails  post method");
			log.info("clientNetworkDetails with clientNetworkDetails object    :{}",clientNetworkDetails);
			ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
			
			serviceStatus.setStatus("success");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.clientNetworkDetailsSavedSuccess"));
			
			try{
					String ipAddress = request.getRemoteAddr();
					
					clientNetworkDetails.setClientId(clientNetworkDetails.getClientId());
					clientNetworkDetails.setUserName(clientNetworkDetails.getUserName());
					clientNetworkDetails.setIpAddress(ipAddress);
					clientNetworkDetails.setDownloadSpeed(clientNetworkDetails.getDownloadSpeed());
					clientNetworkDetails.setUploadSpeed(clientNetworkDetails.getUploadSpeed());
					
					clientNetworkService.save(clientNetworkDetails);
			}catch(Exception exception){
					exception.printStackTrace();
					log.error("Exception  occured while saving client network details   :",exception);
					serviceStatus.setStatus("fail");
					serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
			}
			
			return serviceStatus;
	}
	
	@RequestMapping(value = "/downloadFile", method = RequestMethod.GET, produces={"application/json"})
	@ResponseBody
	public StringBuffer downloadFile(HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into downloadFile get method");
		StringBuffer sf = new StringBuffer();
		try{ 
				ServletContext context = request.getServletContext();
				BufferedInputStream is = (BufferedInputStream) context.getResourceAsStream("/imgs/"+request.getParameter("filename"));
				int i;   
				while ((i=is.read()) != -1) {  
					sf.append(i);
					}   
				is.close();
		} catch (Exception exception) {
			exception.printStackTrace();
			log.error("Exception  occured while downloading a file   :",exception);
			sf.append("Fail "+CapvUtil.environment.getProperty("message.serverFailedError"));
		}
		return sf;
	}
	
	@RequestMapping(value = "/uploadFile", method = RequestMethod.GET, produces={"application/json"})
	public ServiceStatus<Object> uploadFile(String authHeaderToken,HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into uploadFile get method");
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.fileUploadloadSuccess"));
		
		return serviceStatus;
	}
	
}

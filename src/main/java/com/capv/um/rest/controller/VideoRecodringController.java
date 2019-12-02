package com.capv.um.rest.controller;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.websocket.UserWebSocketMessage;
import com.capv.um.model.TryItRoom;
import com.capv.um.model.VideoRecording;
import com.capv.um.security.UserRepositoryUserDetails;
import com.capv.um.service.CallStateService;
import com.capv.um.service.TryItService;
import com.capv.um.service.VideoRecordingService;
import com.capv.um.util.CapvUtil;
import com.capv.um.util.MultipartFileSender;
import com.capv.um.util.ServiceStatus;
import com.google.gson.JsonObject;

/**
 * <h1>VideoRecodringController</h1>
 * this class is used to perform video recording operations
 * @author caprus it
 * @version 1.0
 */
@RestController
public class VideoRecodringController {
	
	@Autowired
	VideoRecordingService videoRecodingService;
	
	@Autowired
	CallStateService callStatesService;
	
	@Autowired
	private TryItService tryit;
	
	@Autowired
	Environment environment;
	
	TokenStore tokenStore;

	private static final Logger log = LoggerFactory.getLogger(VideoRecodringController.class);
	/**
	 * this method is used to save the start time of video
	 * @param videoRecordView instance of VideoRecordView
	 * @return status of insert operation
	 *//*
	@RequestMapping( value = "/video/recording/logtime-stamp", method = RequestMethod.POST, 
			produces={"application/json"})
	 public ServiceStatus<Long> videoRecordingStart(@RequestBody VideoRecordView videoRecordView){
		
		ServiceStatus<Long> serviceStatus = new ServiceStatus<Long>();
		System.out.println(videoRecordView.toString());
		VideoRecordingModel videoRecording = new VideoRecordingModel();
		try{
		videoRecording.setClientId(videoRecordView.getClientId());
		
		videoRecording.setUserName(videoRecordView.getUserName());
		videoRecording.setRoomId(videoRecordView.getRoomId());
		videoRecording.setFullVideo(videoRecordView.isFullVideo());
		videoRecording.setScheduler(videoRecordView.isScheduler());
		videoRecording.setStarttime(new Date());
		videoRecording.setS3path(videoRecordView.getS3path());
		videoRecording.setVideoRecId(videoRecordView.getVideoRecId());
		videoRecording.setCallerId(videoRecordView.getCallerId());
		videoRecording.setSourcePath(videoRecordView.getSourcePath());
		videoRecodingService.videoStarttime(videoRecording);
		serviceStatus.setMessage("Request processed successfully");
		serviceStatus.setStatus("success");
		serviceStatus.setResult(videoRecording.getId());
		}catch(Exception e){
			serviceStatus.setStatus("Failure");
			serviceStatus.setMessage("Request processed failure"+e);
			
		}
		return serviceStatus;
	}
	
	*//**
	 * this method is used to save the end time of video
	 * @param videoRecordView instance of VideoRecordView
	 * @return status of insert operation
	 *//*
	@RequestMapping( value = "/video/recording/logtime-stamp", method = RequestMethod.PUT, 
					produces={"application/json"})
	public ServiceStatus<Object> videoRecordingEnd(@RequestBody VideoRecordView videoRecordView){
		
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		VideoRecordingModel videorecording= new VideoRecordingModel();
		Date date = videoRecodingService.getEndtime(videoRecordView.getId());
		try{
		if(date == null){
			videorecording.setFullVideo(videoRecordView.isFullVideo());
		    videorecording.setId(videoRecordView.getId());
		    videorecording.setVideoRecId(videoRecordView.getVideoRecId());
		    if(videoRecordView.getCallerId() != null && videoRecordView.getRoomId() != null){
		    	videorecording.setCallerId(videoRecordView.getCallerId());
		    	videorecording.setRoomId(videoRecordView.getRoomId());
		    }
			videoRecodingService.videoendtime(videorecording);
		
			serviceStatus.setStatus("success");
			serviceStatus.setMessage("Request processed successfully");
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("End Time Already Registered");
		}
		}catch(Exception e){
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Exception :"+e);
		}
		return serviceStatus;
		
	}*/
	
	/**
	 * this method is used to retrieve the rooms list order by isFullVideo
	 * @param isFullVideo the isFullVideo
	 * @return list of rooms list
	 */
	@RequestMapping( value = "/roomList/{isFullVideo}", method = RequestMethod.GET, 
			produces={"application/json"})
	public ServiceStatus<Map<String, List<VideoRecording>>> roomList(@PathVariable("isFullVideo")Boolean isFullVideo){
		log.debug("Entered into /roomList/{isFullVideo} method");
		log.info("Getting room list with isFullVideo   :{}",isFullVideo);
		ServiceStatus<Map<String, List<VideoRecording>>> serviceStatus = new ServiceStatus<Map<String, List<VideoRecording>>>();
		List<VideoRecording> userList = null;
		try{
		if(isFullVideo == true){
		   userList = videoRecodingService.getRoomList();
		}else{
			userList = videoRecodingService.getRoomListN();
		}
		Map<String, List<VideoRecording>> videoReordingDataByRoom = new HashMap<String, List<VideoRecording>>();
		
		for(VideoRecording videoRecordingData: userList){
		   Date start=videoRecordingData.getStarttime();
		   Date end=videoRecordingData.getEndtime();
		   long  diff = end.getTime() - start.getTime();
		   
		   long SS = diff / 1000 % 60;
		   String ss=Long.toString(SS);
			long MM = diff / (60 * 1000) % 60;
			String mm=Long.toString(MM);
			long HH = diff / (60 * 60 * 1000) % 24;
			String hh=Long.toString(HH);
			if(ss.length()== 1){
				ss="0"+ss;
			}
			if(mm.length()== 1){
				mm="0"+mm;
			}
			if(hh.length() == 1){
				hh="0"+hh;
			}
			
		   videoRecordingData.setDiff(hh+":"+mm+":"+ss+".000");
			if(videoReordingDataByRoom.get(videoRecordingData.getRoomId()) == null) {
				
				List<VideoRecording> videoRecordingList = new ArrayList<VideoRecording>();
				videoRecordingList.add(videoRecordingData);
				videoReordingDataByRoom.put(videoRecordingData.getRoomId(), videoRecordingList);
				
			} else {
				
				List<VideoRecording> videoRecordingList = videoReordingDataByRoom.get(videoRecordingData.getRoomId());
				videoRecordingList.add(videoRecordingData);
			}
		}
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.requestProcessedSuccess"));
		serviceStatus.setResult(videoReordingDataByRoom);
		}
		catch(Exception e){
			log.error("Exception occured while getting room list  :",e);
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Exception:"+e);
		}
		log.debug("Exit from /roomList/{isFullVideo} method");
		return serviceStatus;
		
	}
	
	/**
	 * this method is used to update the s3path
	 * @param videoRecording_id the id 
	 * @param s3path the s3path
	 * @return status of update operation
	 */
	@RequestMapping( value = "/updateS3path/{s3path}/{id}", method = RequestMethod.POST, 
			produces={"application/json"})
	public ServiceStatus<Object> updateS3path(@PathVariable("id") Long videoRecording_id,@PathVariable("s3path") String s3path){
		log.debug("Ëntered into /updateS3path/{s3path}/{id} method");
		log.info("Updating S3path with  videoRecording_id   :{}    s3path   :{}",videoRecording_id,s3path);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		VideoRecording videoRecording= new VideoRecording();
			try{
		videoRecording.setId(videoRecording_id);
		videoRecording.setS3path(s3path);
		videoRecodingService.updateS3path(videoRecording);
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.requestProcessedSuccess"));
			}catch(Exception e){
				log.error("Exception occured while updating S3path  :",e);
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverError"));
			}
		log.debug("Exit from /updateS3path/{s3path}/{id} method");
		return serviceStatus;
	}
	
	@RequestMapping( value = "/video/getVideo/{roomName}", method = RequestMethod.GET, 
			produces={"application/json"})
	public ServiceStatus<String> loadIosVideo(@PathVariable("roomName") String roomName, 
	    HttpServletResponse response,HttpServletRequest request) throws Exception {
		log.debug("Entered into /video/getVideo/{roomName} method");
		log.info("LoadIosVideo with roomName    :{}" ,roomName);
		ServiceStatus<String> service = new ServiceStatus<String>();
		StringBuilder rString = new StringBuilder();
		String ipAddress = CapvClientUserUtil.getConfigProperty("capv_um_video.ipaddress"); 
		String playBachVideosList = null;
		service.setStatus("success");
		   if (ipAddress == null) {  
			   ipAddress = request.getRemoteAddr();  
		   }
			  playBachVideosList= videoRecodingService.getplayBackUrlsList(roomName);
			  String[] videosArray = playBachVideosList.split(",");
			  for (String each : videosArray) {
				  if(rString.length() != 0)
				    rString.append(",").append(ipAddress+"/"+roomName+"-"+each+".webm");
				  else
					  rString.append(ipAddress+"/"+roomName+"-"+each+".webm");
				}
		   
			  service.setResult(rString.toString());
		log.debug("Exit from /video/getVideo/{roomName}  method");
		return service;
	}
	
	@RequestMapping( value = "/video/getVideo/{fileName}/{fileExtension}", method = RequestMethod.GET, 
			produces={"application/json"})
	public void loadVideo(@PathVariable("fileName") String fileName, 
			@PathVariable("fileExtension") String fileExtension,
			HttpServletResponse response) throws Exception {
		log.debug("Entered into /video/getVideo/{fileName}/{fileExtension} method");
		log.info("Loading video with fileName   :{}     fileExtension  :{}",fileName,fileExtension);
		//System.out.println("Room Name::" + roomName);
		System.out.println("File Name::" + fileName);
		System.out.println("File Name::" + fileName);
		String filePath =CapvClientUserUtil.getConfigProperty("capv_um_video.sourcepath") + "/" + fileName + "." + fileExtension;   
		
		System.out.println("File Path::"+filePath);

	    InputStream inputStream = null;
	    ServletOutputStream out = null;

	    try
	    {
	    	out = response.getOutputStream();
	    	inputStream = new FileInputStream(filePath);
	    	String mimeType = Files.probeContentType(Paths.get(filePath));
	    	byte[] bytes = new byte[1024];
	    	int bytesRead;

	    	response.setContentType(mimeType);

	    	while ((bytesRead = inputStream.read(bytes)) != -1) {
	    	    out.write(bytes, 0, bytesRead);
	    	}

	    } catch (Exception e) {
	    	log.error("Exception occured while loading video : ", e);
	    	e.printStackTrace();
	    }
	    finally
	    {
	        if(inputStream != null)
	            inputStream.close();
	        if(out != null)
	        	out.close();
	        log.debug("Exit from /video/getVideo/{fileName}/{fileExtension} method");
	    }
	}
	
	@RequestMapping(value = "/updateSchedular/{id}",method = RequestMethod.PUT)
	public ServiceStatus<Object> updateSchedular(@PathVariable("id") Long id){
		log.debug("Entered into /updateSchedular/{id} method");
		log.info("Updating schedular with id    :{}",id);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<>();
		try{
			videoRecodingService.updateSchedular(id);
			serviceStatus.setStatus("success");
		}
		catch (Exception e) {
			log.error("Exception occured while updating schedular :",e);
			e.printStackTrace();
			serviceStatus.setStatus("failure");
		}
		log.debug("Exit from /updateSchedular/{id} method");
		return serviceStatus;
	}
	
	@RequestMapping(value = "/video/playback/gridSupport", 
					method = RequestMethod.GET, 
					produces={"application/json"})
	public ServiceStatus<Map<String, Object>> getPlaybackGridSupport(){
		
		log.debug("Entered into getPlaybackGridSupport() method");
		
		ServiceStatus<Map<String, Object>> serviceStatus = new ServiceStatus<>();
		try {
			boolean videoPlaybackGridSupport = false;
			
			String videoPlaybackGridSupportString = CapvClientUserUtil.getConfigProperty("recordedvideoplayback.gridsupport");
			
			if(videoPlaybackGridSupportString != null) {
				try {
					videoPlaybackGridSupport = Boolean.parseBoolean(videoPlaybackGridSupportString);
				} catch (Exception e){}
			}
			serviceStatus.setStatus("success");
			Map<String, Object> gridStatusObj = new HashMap<>();
			gridStatusObj.put("playbackGridSupport", videoPlaybackGridSupport);
			serviceStatus.setResult(gridStatusObj);
		}
		catch (Exception e) {
			log.error("Exception occured while getting video playback grid support status :" ,e);
			serviceStatus.setStatus("fail");
			serviceStatus.setMessage("Failed due to server error");
		}
		
		log.debug("Exit from getPlaybackGridSupport() method");
		
		return serviceStatus;
	}
	@RequestMapping( value = "/video/getRecVideo/{roomName}", method = RequestMethod.GET, 
			produces={"application/json"})
	public ServiceStatus<ArrayList<String>> getRecVideo(@PathVariable("roomName") String roomName, 
	    HttpServletResponse response,HttpServletRequest request) throws Exception {
		log.debug("Entered into /video/getRecVideo/{roomName} method");
		log.info("LoadIosVideo with roomName    :{}" ,roomName);
		ServiceStatus<ArrayList<String>> service = new ServiceStatus<ArrayList<String>>();
		ArrayList<String> response_rec=new ArrayList<String>();
 		String calleeList=callStatesService.getCalleeList(roomName);
		String[] calleeArray = calleeList.split(",");
		for(String calleeName :calleeArray) {
			if(calleeName.length()>3)
			response_rec.add(CapvClientUserUtil.getRecordedVideos(roomName,calleeName));
		}
		service.setResult(response_rec);
		log.debug("Exit from /video/getRecVideo/{roomName}  method");
		return service;
	}
	@RequestMapping( value = "/video/getMeetingsRecVideo/{roomName}", method = RequestMethod.GET, 
			produces={"application/json"})
	public ServiceStatus<ArrayList<String>> getMeetingRecVideo(@PathVariable("roomName") String roomName, 
	    HttpServletResponse response,HttpServletRequest request) throws Exception {
		log.debug("Entered into /video/getRecVideo/{roomName} method");
		log.info("LoadIosVideo with roomName    :{}" ,roomName);
		ServiceStatus<ArrayList<String>> service = new ServiceStatus<ArrayList<String>>();
		ArrayList<String> response_rec=new ArrayList<String>();
		TryItRoom meetingRoom=tryit.fetchUniqueRoomRecord(roomName);
		
		if(meetingRoom!=null) {
		response_rec.add(CapvClientUserUtil.getMeetingsRecordedVideos(roomName,meetingRoom.getClientId()));
		}else {
			service.setStatus("success");
			service.setMessage("Invalid Room Number");
		}
		
		if(response_rec.size()>0) {
			
			service.setResult(response_rec);
		}else {
			service.setStatus("success");
			service.setMessage("No Records Found");
		}
		
		log.debug("Exit from /video/getRecVideo/{roomName}  method");
		return service;
	}
	@RequestMapping( value = "/video/deleteMeetingVideo/{roomName}", method = RequestMethod.DELETE, 
			produces={"application/json"})
	public ServiceStatus<ArrayList<String>> deleteMeetingVideo(@PathVariable("roomName") String roomName, 
	    HttpServletResponse response,HttpServletRequest request) throws Exception {
		log.debug("Entered into /video/getRecVideo/{roomName} method");
		log.info("LoadIosVideo with roomName    :{}" ,roomName);
		ServiceStatus<ArrayList<String>> service = new ServiceStatus<ArrayList<String>>();
		
		TryItRoom meetingRoom=tryit.fetchUniqueRoomRecord(roomName);
		
		if(meetingRoom!=null) {
			tryit.delete(meetingRoom);
			service.setStatus("success");
			service.setMessage("Delete TryItRoom Videos");
		}else {
			service.setStatus("success");
			service.setMessage("Invalid Room Number");
		}
		
		
		log.debug("Exit from /video/getRecVideo/{roomName}  method");
		return service;
	}
}

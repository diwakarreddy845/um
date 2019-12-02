package com.capv.client.user.video_calling;

import com.capv.um.service.CallStateService;
import com.capv.um.service.UserService;
import com.capv.um.service.VideoRecordingService;

/**
 * This class is used as video calling service WebSocket configuration class
 * and used this configuration to initialize video calling WebSocket connection
 * <p>
 * This class contains the information about user WebSocket connection and used to 
 * process the messages in between user client and video calling service
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class CapvVideoCallingWebSocketClientConfiguration {
	
	private String userName;
	
	private String webSoketSessionId;
	
	private CallStateService callstateservice;
	
	private VideoRecordingService videoRecordingService;
	
	private UserService userService;
	
	private boolean tryIt;
	
	
	public boolean getTryIt() {
		return tryIt;
	}



	public String getUserName() {
		return userName;
	}
	
	public String getWebSoketSessionId() {
		return webSoketSessionId;
	}
	
	public CallStateService getCallStateService() {
		return callstateservice;
	}
	
	public UserService getUserService() {
		return userService;
	}
	
	public VideoRecordingService getVideoRecordingService() {
		return videoRecordingService;
	}
	
	public static ConnectionConfigurationBuilder connectionConfigurationBuilder() {
		return new ConnectionConfigurationBuilder();
	}
	
	private CapvVideoCallingWebSocketClientConfiguration(ConnectionConfigurationBuilder connectionConfigurationBuilder){
		
		this.userName			= connectionConfigurationBuilder.userName;
		this.webSoketSessionId	= connectionConfigurationBuilder.webSoketSessionId;
		this.callstateservice   = connectionConfigurationBuilder.callstateservice;
		this.videoRecordingService = connectionConfigurationBuilder.videoRecordingService;
		this.tryIt 				   = connectionConfigurationBuilder.tryIt;
		this.userService		= connectionConfigurationBuilder.userService;
	}

	/**
	 * 
	 * This class is used to build the video calling service configuration
	 * 
	 * @author ganesh.maganti
	 * @version 1.0
	 */
	public static class ConnectionConfigurationBuilder {
		
		private String userName;
		
		private String webSoketSessionId;
		
		private CallStateService callstateservice;
		
		private VideoRecordingService videoRecordingService;
		
		private boolean tryIt;
		
		private UserService userService;
		
		private ConnectionConfigurationBuilder(){}
		
		public ConnectionConfigurationBuilder setUserName(String userName) {
			this.userName = userName;
			return this;
		}
		public ConnectionConfigurationBuilder settryIt(boolean tryIt) {
			this.tryIt = tryIt;
			return this;
		}
		public ConnectionConfigurationBuilder setCallStateService(CallStateService callstateservice) {
			this.callstateservice = callstateservice;
			return this;
		}
		public ConnectionConfigurationBuilder setWebSocketSessionId(String webSocketSessionId) {
			this.webSoketSessionId = webSocketSessionId;
			return this;
		}
		
		public ConnectionConfigurationBuilder setVideoRecordingService(VideoRecordingService videoRecordingService) {
			this.videoRecordingService = videoRecordingService;
			return this;
		}
		
		public ConnectionConfigurationBuilder setUserService(UserService userService) {
			this.userService = userService;
			return this;
		}
		
		public CapvVideoCallingWebSocketClientConfiguration build() {
			return new CapvVideoCallingWebSocketClientConfiguration(this);
		}
		
	}

}

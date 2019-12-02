package com.capv.client.user;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.constants.CapvClientUserConstants.ChatClientAuthStatus;
import com.capv.client.user.constants.CapvClientUserConstants.UserServicesConnectStatus;
import com.capv.client.user.constants.CapvClientUserConstants.VideoCallingClientConnectStatus;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.video_calling.Capv_VC_WS_ClientHandler;
import com.capv.client.user.websocket.CapvUserWebSocketMessageProcessor;
import com.capv.um.util.CapvUtil;
import com.google.gson.JsonObject;

/**
 * <h1> User Session </h1>
 * 
 * This class is used to refer the user data for the processing of user requests 
 * after user has been successfully established connection with WebSocket 
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class UserSession {

	private Long userId;
	private String userName;
	private Long clientId;
	private WebSocketSession webSocketSession;
	
	private boolean callInProgress = false;
	private Map<String, Object> callData = null;
	
	private Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint;
	
	private CapvUserWebSocketMessageProcessor capvUserWebSocketMessageProcessor;
	
	private ChatClientAuthStatus chatClientAuthStatus;
	private VideoCallingClientConnectStatus videoCallingClientConnectStatus;
	private String connectionSource = "DEFAULT";
	private Timer webSocketPingTimer;
	private String presence;
	
	public String getPresence() {
		return presence;
	}

	public void setPresence(String presence) {
		this.presence = presence;
	}

	private long lastPongMessageReceived;
	private String lastSingedOs;
	private String key;
	private String iv;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getIv() {
		return iv;
	}

	public void setIv(String iv) {
		this.iv = iv;
	}

	public String getLastSingedOs() {
		return lastSingedOs;
	}

	public void setLastSingedOs(String lastSingedOs) {
		this.lastSingedOs = lastSingedOs;
	}

	/**
	 * This is the parameterized constructor which is used to initialize UserSession
	 * 
	 * @param userName							The userName
	 * @param userId							The userId
	 * @param clientId							The clientId which user is associated
	 * @param webSocketSession					The reference of WebSocket session which user is connected
	 * @param capvUserWebSocketMessageProcessor	The WebSocket message processor to deliver the messages to user
	 */
	public UserSession(String userName, Long userId, Long clientId, 
						WebSocketSession webSocketSession,
						CapvUserWebSocketMessageProcessor capvUserWebSocketMessageProcessor,String lastSingedOs,String key,String iv) {
		
		this.userName = userName;
		this.userId = userId;
		this.clientId = clientId;
		this.webSocketSession = webSocketSession;
		this.capvUserWebSocketMessageProcessor = capvUserWebSocketMessageProcessor;
		this.lastSingedOs=lastSingedOs;
		this.key=key;
		this.iv=iv;
	}
	
	
	
	public boolean isCallInProgress() {
		return callInProgress;
	}

	public void setCallInProgress(boolean callInProgress) {
		this.callInProgress = callInProgress;
	}

	public Map<String, Object> getCallData() {
		return callData;
	}
	
	public Map<String, Object> initializeCallData() {
		callData = new ConcurrentHashMap<>();
		return callData;
	}
	
	public void clearCallData() {
		callData = null;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public Long getClientId() {
		return clientId;
	}
	
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	
	public WebSocketSession getWebSocketSession() {
		return webSocketSession;
	}
	
	public void setWebSocketSession(WebSocketSession webSocketSession) {
		this.webSocketSession = webSocketSession;
	}
	
	public Capv_VC_WS_ClientHandler getCapvVideoCallingWebSocketClientEndpoint() {
		return capvVideoCallingWebSocketClientEndpoint;
	}

	public void setCapvVideoCallingWebSocketClientEndpoint(
			Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint) {
		this.capvVideoCallingWebSocketClientEndpoint = capvVideoCallingWebSocketClientEndpoint;
	}

	public CapvUserWebSocketMessageProcessor getCapvUserWebSocketMessageProcessor() {
		return capvUserWebSocketMessageProcessor;
	}

	public void setChatClientAuthStatus(ChatClientAuthStatus chatClientAuthStatus) {
		this.chatClientAuthStatus = chatClientAuthStatus;
		
		checkUserServicesStatus();
	}

	public void setVideoCallingClientConnectStatus(VideoCallingClientConnectStatus videoCallingClientConnectStatus) {
		
		if(this.videoCallingClientConnectStatus == null) {
			this.videoCallingClientConnectStatus = videoCallingClientConnectStatus;
			checkUserServicesStatus();
		}
	}
	
	/**
	 * This method used to check whether all the user services successfully started or not
	 * 
	 * @return returns whether the user services like chat and video calling services successfully started or not
	 */
	public boolean isUserServicesStarted() {
		
		boolean isUserServicesStarted = false;
		
		if(chatClientAuthStatus != null /*&& videoCallingClientConnectStatus != null */
				&& chatClientAuthStatus == ChatClientAuthStatus.SUCCESS /*&& 
				videoCallingClientConnectStatus == VideoCallingClientConnectStatus.SUCCESS*/)
			isUserServicesStarted = true;
		
		return isUserServicesStarted;
	}
	
	/**
	 * This method used to check whether all the user services started successfully or not 
	 * and send the status message to user client for further processing of requests
	 */
	private void checkUserServicesStatus(){
		
		if(chatClientAuthStatus != null /*&& videoCallingClientConnectStatus != null*/) {
			
			JsonObject messageToSend = new JsonObject();
			messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
										CapvClientUserConstants.WS_MESSAGE_USER_SERVICES_CONNECT_STATUS);
			
			if(chatClientAuthStatus == ChatClientAuthStatus.SUCCESS /*&& 
					videoCallingClientConnectStatus == VideoCallingClientConnectStatus.SUCCESS*/) 
	    		messageToSend.addProperty("status", UserServicesConnectStatus.SUCCESS.getStatus());
			else
				messageToSend.addProperty("status", UserServicesConnectStatus.FAIL.getStatus());
			
			String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
    		
			if(webSocketSession != null) {
				String key = getKey(); // 128 bit key
		        String initVector = getIv();
				//TextMessage textMessage = new TextMessage(CapvUtil.encrypt(key, initVector,userMessage));
		        
		        String encryption=(String)webSocketSession.getAttributes().get("encrytion");
		        TextMessage textMessage ;
		        if(encryption.equals("enabled")) {
		        		textMessage= new TextMessage(CapvUtil.encrypt(key, initVector, userMessage));
		        }else {
		        		textMessage= new TextMessage(userMessage);
		        }
		        
				try {
					webSocketSession.sendMessage(textMessage);
				} catch (Exception e){}
			}
		}
	}

	public String getConnectionSource() {
		return connectionSource;
	}

	public void setConnectionSource(String connectionSource) {
		this.connectionSource = connectionSource;
	}

	public Timer getWebSocketPingTimer() {
		return webSocketPingTimer;
	}

	public void setWebSocketPingTimer(Timer webSocketPingTimer) {
		this.webSocketPingTimer = webSocketPingTimer;
	}

	public long getLastPongMessageReceived() {
		return lastPongMessageReceived;
	}

	public void setLastPongMessageReceived(long lastPongMessageReceived) {
		this.lastPongMessageReceived = lastPongMessageReceived;
	}
	
	
}

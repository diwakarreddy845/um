package com.capv.client.user;

import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.capv.client.user.video_calling.Capv_VC_WS_ClientHandler;

/**
 * <h1> User Session </h1>
 * 
 * This class is used to refer the user data for the processing of user requests 
 * after user has been successfully established connection with WebSocket 
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class TryItUserSession {

	
	private String userName;
	private WebSocketSession webSocketSession;
	
	private boolean callInProgress = false;
	private Map<String, Object> callData = null;
	
	private Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint;
	
	private String connectionSource = "DEFAULT";
	
	private Timer webSocketPingTimer;
	private long lastPongMessageReceived;
	
	/**
	 * This is the parameterized constructor which is used to initialize UserSession
	 * 
	 * @param userName							The userName
	 * @param userId							The userId
	 * @param clientId							The clientId which user is associated
	 * @param webSocketSession					The reference of WebSocket session which user is connected
	 * @param capvUserWebSocketMessageProcessor	The WebSocket message processor to deliver the messages to user
	 */
	public TryItUserSession(String userName, WebSocketSession webSocketSession) {
		
		this.userName = userName;
		this.webSocketSession = webSocketSession;
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

	
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public String getConnectionSource() {
		return connectionSource;
	}

	public void setConnectionSource(String connectionSource) {
		this.connectionSource = connectionSource;
	}
	
	public synchronized void processMessage(String message) {
		
		
		if(webSocketSession != null) {
			
			try {
				TextMessage textMessage = new TextMessage(message);
				webSocketSession.sendMessage(textMessage);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}

	public long getLastPongMessageReceived() {
		return lastPongMessageReceived;
	}

	public void setLastPongMessageReceived(long lastPongMessageReceived) {
		this.lastPongMessageReceived = lastPongMessageReceived;
	}

	public Timer getWebSocketPingTimer() {
		return webSocketPingTimer;
	}

	public void setWebSocketPingTimer(Timer webSocketPingTimer) {
		this.webSocketPingTimer = webSocketPingTimer;
	}
	
	
}

package com.capv.client.user;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.CloseStatus;

import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.constants.CapvClientUserConstants.UserState;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.video_calling.Capv_VC_WS_ClientHandler;
import com.capv.client.user.websocket.UserWebSocketMessage;
import com.capv.um.model.VideoRecording;
import com.capv.um.service.CallStateService;
import com.capv.um.service.UserService;
import com.capv.um.service.VideoRecordingService;
import com.google.gson.JsonObject;

/**
 * 
 * <h1> User Registry Container</h1>
 * 
 * This class is used as a user session container and holds the user sessions 
 * until WebSocket connection of the user closed
 * <p>
 * This class holds the user sessions of the same user which are connected from different 
 * clients and used for WebSocket message processing
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class UserRegistry {
	
	private final static Map<String, Map<String, UserSession>> userSockets = new ConcurrentHashMap<>();
	private final static Map<String, String> usersBySession	= new ConcurrentHashMap<>();
	
	/**
	 * 
	 * This method used to return the user name for a given session id
	 * 
	 * @param sessionId	The session id
	 * @return returns the user name associated to the given session id
	 */
	public static String getUserNameBySessionId(String sessionId) {
		return usersBySession.get(sessionId);
	}
	
	/**
	 * 
	 * This method is used to return all the user sessions 
	 * which are alive at the time of this method invoked
	 * 
	 * @return returns all the user sessions which are alive
	 */
	public static Map<String, Map<String, UserSession>> getAllSessions() {
		return userSockets;
	}
	
	public static int getActiveSessionsCount() {
		return userSockets.size();
	}
	
	/**
	 * This method is used to get the user session for a given session id
	 * 
	 * @param sessionId The session id
	 * @return returs the user session associated to a given session id
	 */
	public static UserSession getUserSessionBySessionId(String sessionId) {
		
		UserSession userSession = null;
		
		if(sessionId != null) {
			
			String userName = getUserNameBySessionId(sessionId);
			
			if(userName != null) {
				Map<String, UserSession> userSessions = userSockets.get(userName);
				
				if(userSessions != null)
					userSession = userSessions.get(sessionId);
			}
			
		}
		
		return userSession;
	}
	
	/**
	 * This method is used to get all the user sessions for a given user name from the registry
	 * 
	 * @param userName The user name
	 * @return returns all the connected user sessions for a given user name
	 */
	public static List<UserSession> getUserSessionsByUserName(String userName) {
		
		List<UserSession> userSessions = null;
		
		Map<String, UserSession> userSessionsMap = userSockets.get(userName);
		
		if(userSessionsMap != null && !userSessionsMap.isEmpty()) {
			
			userSessions = new ArrayList<>();
			
			for(Entry<String, UserSession> userSessionEntry: userSessionsMap.entrySet())
				userSessions.add(userSessionEntry.getValue());
				
		}
		
		return userSessions;
	}
	
	/**
	 * 
	 * This method is used to add user session into registry after user successfully established WebSocket connection
	 * 
	 * @param userName		The user name
	 * @param sessionId		The session id of the user
	 * @param userSession	The user session created for a user
	 */
	public static void addUserSession(String userName, String sessionId, UserSession userSession){
		
		Map<String, UserSession> userSessionsMap = null;
		
		if(userSockets.get(userName) != null)
			userSessionsMap = userSockets.get(userName);
		else
			userSessionsMap = new HashMap<>();
		
		userSessionsMap.put(sessionId, userSession);
		userSockets.put(userName, userSessionsMap);
		usersBySession.put(sessionId, userName);
	}
	
	/**
	 * 
	 * This method is used to cleanup the user session from registry and close all 
	 * the user connections with capv services like chat service and video calling service etc.
	 * 
	 * @param sessionId		The session id
	 * @param userService	The user service which is used to change the user call state upon user disconnected from the WebSocket session
	 */
	public static void removeUserSession(String sessionId, UserService userService, 
										CallStateService callStateService, VideoRecordingService videoRecordingService,CloseStatus status) {
		
		String userName = usersBySession.get(sessionId);
		
		if(userName != null) {
			
			Map<String, UserSession> userSessionsMap = userSockets.get(userName);
			
			if(userSessionsMap != null) {
				UserSession userSession = userSessionsMap.get(sessionId);
				
				if(userSession != null) {
					
					Timer webSocketPingTimer = userSession.getWebSocketPingTimer();
					
					if(webSocketPingTimer != null) {
						webSocketPingTimer.cancel();
						userSession.setWebSocketPingTimer(null);
					}
					
					//close video streaming server client
					Capv_VC_WS_ClientHandler videoCallingWebSocketClientEndpoint = 
															userSession.getCapvVideoCallingWebSocketClientEndpoint();
					
					if(videoCallingWebSocketClientEndpoint != null) {
						
						if(userSession.isCallInProgress()) {
							
							Map<String, Object> callData = userSession.getCallData();
							
							JsonObject obj_msg=new JsonObject();
							obj_msg.addProperty(  "method" , "leaveRoom");
							obj_msg.addProperty("id" , 11);
							obj_msg.addProperty("jsonrpc" , "2.0");
							JsonObject params = new JsonObject();
							params.addProperty("user", userName);
							
							params.addProperty("updateStatistics", true);
							
							if(callData.get("roomNumber") != null)
								params.addProperty("roomName", (String)callData.get("roomNumber"));
							
							obj_msg.add("params", params);
							 
							videoCallingWebSocketClientEndpoint.sendExitMessageAndCloseSession(obj_msg.toString());
							
							com.capv.um.model.User user = userService.getById(userSession.getUserId());
							user.setCallStatus(UserState.IDLE.getStateId());
							userService.update(user, false);
							
							if(callData != null && callData.get("callType") != null && 
									((String)callData.get("callType")).equals(CapvClientUserConstants.CALL_TYPE_ONE)) {
								 
								 String room = null;
								 if(callData.get("roomNumber") != null)
									 room = (String)callData.get("roomNumber");
								 
								 if(room != null) {
									 String calleeList = (callStateService.getCalleeList(room));
									 
									 if(calleeList != null) {
										String[] calleeArray = calleeList.split(",");
										
										for(String calleeName :calleeArray) {
											if(!calleeName.equals(userName)) {
												List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(calleeName);
												if(userSessions != null){
													Long receiverId = null;
													for(UserSession receiverSession :userSessions)
													{
														if(receiverId == null)
															receiverId = receiverSession.getUserId();
														
														JsonObject scParams=new JsonObject();
														
														scParams.addProperty("id", "callDrop");
														scParams.addProperty("roomNumber", room);
														scParams.addProperty("from",userName);
				    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(receiverSession, scParams.toString());
														userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
													}
												}
											}
										}
									 }
								 }
							}
							
							if(callData != null && 
                            		callData.get("videoRecordingCurrentId") != null) {
                            	
                            	Long videoRecordingId = (Long)callData.get("videoRecordingCurrentId");
                            	
                            	VideoRecording videoRecordingModel = videoRecordingService.getById(videoRecordingId);
                            	
                            	if(videoRecordingModel != null) {
                                	Date endTime = new Date();
                            		videoRecordingModel.setEndtime(endTime);
                            		if(videoRecordingModel.getStarttime() != null) {
                            			Date startTime = videoRecordingModel.getStarttime();
                            			long diff = CapvClientUserUtil.getTimeDiffInSeconds(startTime, endTime);
                            			videoRecordingModel.setDiff(""+diff);
                            		}
                            		videoRecordingService.update(videoRecordingModel);
                            	}
                            	
                            	callData.remove("videoRecordingCurrentId");
                            }
							
						} else
							videoCallingWebSocketClientEndpoint.closeClientConnection();
						
					}
					
					userSessionsMap.remove(sessionId);
					
					if(userSessionsMap.isEmpty()) {
						
						userSockets.remove(userName);
						
						//close chat client
						try { // go offline
							CapvChatClientManagerRegistry.closeUserChatManagerConnection(userName);
						} catch (Exception e){}
						
						com.capv.um.model.User user = userService.getByUserName(userName, false);
						if(user!=null){
							if(user.getLastSigninOs().equalsIgnoreCase("web")){
								user.setCallStatus(UserState.NOTLOGGEDIN.getStateId());
								userService.update(user, false);
							}
							/** done for IOS users not to change the state of user in case he is going away(1001)**/
							else if(user.getLastSigninOs().equalsIgnoreCase("ios")){
								if(status.getReason().equalsIgnoreCase("logout")){
									user.setCallStatus(UserState.NOTLOGGEDIN.getStateId());
									userService.update(user, false);
								}
							}
							else if(user.getLastSigninOs().equalsIgnoreCase("android")){
								if(status.getReason().equalsIgnoreCase("logout")){
									user.setCallStatus(UserState.NOTLOGGEDIN.getStateId());
									userService.update(user, false);
								}
							}
							else{
								user.setCallStatus(UserState.NOTLOGGEDIN.getStateId());
								userService.update(user, false);
							}
						}
					}
					
					userSession = null;
					usersBySession.remove(sessionId);
				}
			}
		}
	}
	

}

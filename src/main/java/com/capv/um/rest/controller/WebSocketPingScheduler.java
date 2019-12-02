package com.capv.um.rest.controller;

import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.Presence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserRequestProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.um.model.User;
import com.capv.um.service.UserService;
import com.google.gson.JsonObject;

@Component
public class WebSocketPingScheduler {
	
	@Autowired
	private UserService userService;
	
	@Autowired
    private Environment environment;
	
	private static final Logger log = LoggerFactory.getLogger(WebSocketPingScheduler.class);
	
	@Scheduled(fixedRate=60000)
	public void checkPing() {
		System.out.println("check ping test....");
		try{
			
				List<User> userList=userService.getListOfLoggedUsers();
				if(userList!=null){
					for(int i=0;i<userList.size();i++) {

						long currentTimestamp = System.currentTimeMillis();
						long lastPongMessageReceived = userList.get(i).getPing().getTime();
						List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userList.get(i).getUserName());

						Long maxPingPongTime=Long.parseLong(environment.getProperty("pingpong"));

						if (userSessions!=null){
							if((currentTimestamp - lastPongMessageReceived) > maxPingPongTime ){

								for(int j=0;j<userSessions.size();j++){
									if(userSessions.get(j).getCapvVideoCallingWebSocketClientEndpoint() != null){
										Map<String, Object> callData = userSessions.get(j).getCallData();
										if(callData != null && callData.get("callType") != null && 
												callData.get("callType").equals(CapvClientUserConstants.CALL_TYPE_GROUP) && 
												callData.get("groupCallInitiator") != null) {
											System.out.println("check ping test...."+callData.get("callType").equals(CapvClientUserConstants.CALL_TYPE_GROUP));
											String calleeStr = (String)callData.get("callee");
											String[] calleeList = calleeStr.split(",");
											String roomName = (String)callData.get("roomName");
											JsonObject endpointsmsg=new JsonObject();
											endpointsmsg.addProperty(  "method" , "pause");
											endpointsmsg.addProperty("id" , 13);
											endpointsmsg.addProperty("jsonrpc" , "2.0");
											for(String calleeName: calleeList){
												List<UserSession> calleeUserSessions = UserRegistry.getUserSessionsByUserName(calleeName);

												if(calleeUserSessions != null) {
													for(UserSession calleeUserSession :calleeUserSessions) {
														Map<String, Object> calleeCallData = calleeUserSession.getCallData();

														if(calleeCallData != null && calleeCallData.get("roomNumber") != null&&calleeUserSession.getCapvVideoCallingWebSocketClientEndpoint()!=null) {
															JsonObject params = new JsonObject();
															params.addProperty("user", calleeUserSession.getUserName());
															endpointsmsg.add("params", params);

															calleeUserSession.getCapvVideoCallingWebSocketClientEndpoint().sendMessage(endpointsmsg.toString());
														}
													}
												}
											}

										}JsonObject obj_msg=new JsonObject();
										obj_msg.addProperty("method" , "leaveRoom");
										obj_msg.addProperty("id" , 11);
										obj_msg.addProperty("jsonrpc" , "2.0");
										JsonObject params = new JsonObject();
										params.addProperty("user", userSessions.get(j).getUserName());
										userSessions.get(j).getCapvVideoCallingWebSocketClientEndpoint().sendExitMessageAndCloseSession(obj_msg.toString());

										if(userSessions.get(j).getCapvVideoCallingWebSocketClientEndpoint() != null)
											userSessions.get(j).getWebSocketSession().close();
									}
								}
							}

						}
					}

				}
			List<User> userPresenceList=userService.getListOfLoggedUsersForPresence();
			if(userPresenceList!=null) {
				for(int i=0;i<userPresenceList.size();i++) {

					long currentTimestamp = System.currentTimeMillis();
					long lastPongMessageReceived = userPresenceList.get(i).getPing().getTime();
					List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userPresenceList.get(i).getUserName());

					Long maxPingPongTime=Long.parseLong(environment.getProperty("idle.pingpong"));

					if (userSessions!=null){
						if((currentTimestamp - lastPongMessageReceived) > maxPingPongTime ){
							for(int j=0;j<userSessions.size();j++){
								if(CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userSessions.get(j).getUserName()).isChatConnectionAlive()) {
									
									CapvChatUserRequestProcessor capvChatUserRequestProcessor = CapvChatClientManagerRegistry
																									.getCapvChatClientManagerByUser(userSessions.get(j).getUserName())
																									.getCapvChatUserRequestProcessor();
									if(userSessions.get(j).getPresence()!=null){
										if(userSessions.get(j).getPresence().equals(Presence.Mode.dnd.toString())) {
										}else if(userSessions.get(j).getPresence().equals(Presence.Mode.away.toString())){
										}else {
											capvChatUserRequestProcessor.changeUserPresence(Presence.Mode.away.toString(), "away");
											userSessions.get(j).setPresence(Presence.Mode.away.toString());
										}
											
									}else {
										capvChatUserRequestProcessor.changeUserPresence(Presence.Mode.away.toString(), "away");
										userSessions.get(j).setPresence(Presence.Mode.away.toString());
									}
							}
								break; 
							}
						}
					}
				}
			}
			}catch(Exception ioe){
				log.error("IO exception raised while ping the websocket. Reason: {}", ioe.getMessage());
			}
		
		}
		/*
		if(CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName).isChatConnectionAlive()) {
			
			CapvChatUserRequestProcessor capvChatUserRequestProcessor = CapvChatClientManagerRegistry
																			.getCapvChatClientManagerByUser(userName)
																			.getCapvChatUserRequestProcessor();
			
			capvChatUserRequestProcessor.changeUserPresence(Presence.Mode.away.toString(), "away");
		}*/
}

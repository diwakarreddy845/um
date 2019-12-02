package com.capv.client.user.chat.handler;

import java.util.List;

import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.websocket.CapvUserWebSocketMessageProcessor;
import com.capv.client.user.websocket.UserWebSocketMessage;
import com.google.gson.JsonObject;
/**
 * <h1>CapvChatClientMessagesDeviceSyncHandler</h1>
 * this class is used to sync between devices which users are  loggedIn in different devices
 * @author narendra.muttevi
 * version 1.0
 */
public class CapvChatClientMessagesDeviceSyncHandler {

   /**
    * process to sync between devices which users are  loggedIn in different devices
    * @param senderName the senderName.
    * @param senderSessionId the senderSessionId
    * */
	public static void syncChatMessageToSenderDevices(JsonObject jsonMessageObj, 
														String senderName, 
														String senderSessionId) {
		
		if(jsonMessageObj != null && senderName != null && senderSessionId != null && 
				jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_ID_KEY) != null) {
    		
    		switch(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_ID_KEY).getAsString()) {
    		
    			case CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND: 
    												{
    										    		String message	= null;
    										    		String receiver = null;
    										    		
    										    		if(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY) != null) {
    										    			message = jsonMessageObj.get(
																			CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY).getAsString();
    										    		}
    										    		if(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_RECEIVER_KEY) != null) {
    										    			receiver = jsonMessageObj.get(
																			CapvClientUserConstants.WS_MESSAGE_CHAT_RECEIVER_KEY).getAsString();
    										    		}
    															
														if(message != null && receiver != null) {
															
															JsonObject messageToSend = new JsonObject();
															
															messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
												    									CapvClientUserConstants.WS_MESSAGE_MESSAGE_RECEIVE);
												    		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, senderName);
															messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, message);
															messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_RECEIVER_KEY, receiver);
															
															syncMessagesToSenderSessions(senderName, senderSessionId, messageToSend.toString());
														}
    				
    												}
    												break;
    												
    			case CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_SEND:
									    			{
									    				String room = null;
									    				String msg	= null;
									    				
									    				if(jsonMessageObj.get("room") != null)
									    					room = jsonMessageObj.get("room").getAsString();
									    				
									    				if(jsonMessageObj.get("message") != null)
									    					msg = jsonMessageObj.get("message").getAsString();
									    				
									    				if(room != null && msg != null) {
									    					
									    					JsonObject messageToSend = new JsonObject();
									    					
									    					messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
					    																	CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_RECEIVE);
											        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, senderName);
											        		messageToSend.addProperty("room", room);
											        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, msg);
											        		
											        		syncMessagesToSenderSessions(senderName, senderSessionId, messageToSend.toString());
									    				}
									    			}
									    			break;
    		}
		}
		
	}
	
    /**
     * process to sync between devices which users are  loggedIn in different devices for receiving messages 
     * @param senderName
     * @param senderMessage
     * @param senderMessageSessionId.
     * */
	private static void syncMessagesToSenderSessions(String senderName, 
														String senderMessageSessionId, 
														String senderMessage) {
		
		List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(senderName);
		
		if(userSessions != null && !userSessions.isEmpty()) {
			
			for(UserSession userSession :userSessions) {
				
				if(userSession.getWebSocketSession() !=  null && 
						!userSession.getWebSocketSession().getId().equals(senderMessageSessionId)) {
					
					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, senderMessage);
					
					CapvUserWebSocketMessageProcessor capvUserWebSocketMessageProcessor = 
														userSession.getCapvUserWebSocketMessageProcessor();
					capvUserWebSocketMessageProcessor.processMessage(userWebSocketMessage);
				}
			}
		}
	}
}

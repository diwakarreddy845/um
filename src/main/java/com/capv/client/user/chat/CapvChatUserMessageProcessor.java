package com.capv.client.user.chat;

import java.util.List;

import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.websocket.UserWebSocketMessage;
/**
 * <h1> CapvChatUserMessageProcessor</h1>
 * this class is used to send message to user web socket
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvChatUserMessageProcessor {
	/**
	 * process to send message to user web socket by using user session.
	 * if user sessions are not null it will send message.
	 * @param userName the userName
	 * @param message the message
	 * */
	private static void sendMessageToUserWebSocket(String userName, String message) {
		
		List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
		
		if(userSessions != null && !userSessions.isEmpty()) {
			
			for(UserSession userSession :userSessions) {
				
				if(userSession.isUserServicesStarted()) {
					
					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, message);
					userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
				}
			}
		}
	}
	/**
	 * process to send message to sendMessageToUserWebSocket.
	 * @param userName the userName
	 * @param message the message
	 * */
	public static void sendChatClientMessageToUser(String userName, String message) {
		sendMessageToUserWebSocket(userName, message);
	}

}

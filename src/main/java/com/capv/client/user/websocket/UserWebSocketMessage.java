package com.capv.client.user.websocket;

import com.capv.client.user.UserSession;

/**
 * <h1> User WeSocket Message </h1>
 * 
 * This class is used as data transfer object(DTO) to 
 * process and deliver messages to user clients
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class UserWebSocketMessage {

	private UserSession userSession;
	private String message;
	
	/**
	 * This is the parameterized constructor which is used to initialize the class 
	 * with the required properties to process and deliver message to client
	 * 
	 * @param userSession	The user session which is used require to send the message 
	 * @param message		The message needs to be send to the user
	 */
	public UserWebSocketMessage(UserSession userSession, 
								String message) {
		this.userSession = userSession;
		this.message = message;
	}
	
	public UserSession getUserSession() {
		return userSession;
	}
	
	public void setUserSession(UserSession userSession) {
		this.userSession = userSession;
	}
	
	public String getMessage() {
		return message;
	}
	
	public void setMessage(String message) {
		this.message = message;
	}
	
	
}

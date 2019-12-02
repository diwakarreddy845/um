package com.capv.client.user.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.capv.client.user.UserSession;
import com.capv.um.util.CapvUtil;
import com.google.gson.JsonObject;

/**
 * <h1>Capv User WebSocket Message Processor</h1>
 * This class is used to process and deliver WebSocket messages to the user clients
 * 
 * @author ganesh.maganti
 * @version 1.0
 *
 */
public class CapvUserWebSocketMessageProcessor {

	/*private ConcurrentLinkedQueue<UserWebSocketMessage> userWebSocketMessagesQueue = new ConcurrentLinkedQueue<>();
	
	private boolean isProcessRuning = false;*/
	
	/**
	 * This method is used to process and deliver WebSocket messages to the user clients
	 * 
	 * @param userWebSocketMessage	This parameter contains the message which needs to be 
	 * 								deliver to user and UserSession needs to be push the message
	 * 								@see com.capv.client.user.websocket.UserWebSocketMessage
	 */
	public synchronized void processMessage(UserWebSocketMessage userWebSocketMessage) {
		
		
		if(userWebSocketMessage != null && 
				userWebSocketMessage.getUserSession() != null && 
						userWebSocketMessage.getMessage() != null) {
			UserSession userSession = userWebSocketMessage.getUserSession();
			WebSocketSession webSocketSession = userSession.getWebSocketSession();
			try {
				String key = userSession.getKey(); // 128 bit key
		        String initVector = userSession.getIv();
		        TextMessage textMessage;
		        String encryption=(String)webSocketSession.getAttributes().get("encrytion");
		        if(encryption.equals("enabled")) {
		        		textMessage= new TextMessage(CapvUtil.encrypt(key, initVector, userWebSocketMessage.getMessage()));
		        }else {
		        		textMessage= new TextMessage(userWebSocketMessage.getMessage());
		        }
				
		       // TextMessage textMessage = new TextMessage(userWebSocketMessage.getMessage());
				webSocketSession.sendMessage(textMessage);
				//System.out.println(textMessage);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * This method is used to process and deliver WebSocket messages to the user clients
	 * 
	 * @param userWebSocketMessage	This parameter contains the message which needs to be 
	 * 								deliver to user and UserSession needs to be push the message
	 * 								@see com.capv.client.user.websocket.UserWebSocketMessage
	 */
	public synchronized void processMessage1(UserWebSocketMessage userWebSocketMessage) {
		
		
		if(userWebSocketMessage != null && 
				userWebSocketMessage.getUserSession() != null && 
						userWebSocketMessage.getMessage() != null) {
			UserSession userSession = userWebSocketMessage.getUserSession();
			
			WebSocketSession webSocketSession = userSession.getWebSocketSession();
			
			try {
				
				TextMessage textMessage = new TextMessage( userWebSocketMessage.getMessage());
				webSocketSession.sendMessage(textMessage);
				System.out.println(textMessage);
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}
	
	/*public synchronized void addMessageToQueue(UserWebSocketMessage webSocketUserMessage) {
		
		System.out.println("Messages Size::"+userWebSocketMessagesQueue.size());
		
		if(webSocketUserMessage != null && 
				webSocketUserMessage.getUserSession() != null && 
						webSocketUserMessage.getMessage() != null) {
			
			userWebSocketMessagesQueue.add(webSocketUserMessage);
			
			if(!isProcessRuning)
				processMessage();
			
			if(Thread.currentThread().getState().equals(Thread.State.WAITING)) {
				userWebSocketMessagesQueue.notifyAll();
				//Thread.currentThread().notifyAll();
			}
			
		}
	}*/
}

package com.capv.client.user.video_calling;

import java.net.URI;
import java.util.concurrent.CountDownLatch;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * 
 * This class is used to connect the capv video calling service 
 * and process the call handling messages between client and video calling service
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class Capv_VC_WS_ClientEndpoint extends Endpoint {
	
	private Session session = null;
	private MessageHandler.Whole<String> messageHandler;
	
	private static int activeVCSessionsCount = 0;
	
	private CountDownLatch latch= new CountDownLatch(1);

	public Capv_VC_WS_ClientEndpoint(URI vcEndpointURI, ClientEndpointConfig clientEndpointConfig, 
									MessageHandler.Whole<String> messageHandler) throws Exception {
		try {
			this.messageHandler = messageHandler;
			
	        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, clientEndpointConfig, vcEndpointURI);
			
		} catch (Exception e) {
			throw e;
		}
	}
	
	public boolean isSessionAlive() {
		return (session != null && session.isOpen());
	}
	
    /**
     * This method is used to post processing of request after video calling service is getting opened.
     *
     * @param session the session which is opened.
     */
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        System.out.println("opening video calling websocket session");
        this.session = session;
        this.session.addMessageHandler(messageHandler);
        activeVCSessionsCount++;
        latch.countDown();
    }

    /**
     * This method is used to handle post processing of requests after video calling service is getting closed.
     *
     * @param code This is the first parameter of the method which refers the status code of the connection closed 
 * 					which is used to check whether connection closed normally or closed due to error 
     * @param reason the reason for connection close
     */
    public void onClose(Session userSession, CloseReason reason) {
        System.out.println("Closed video calling websocket session. "
        						+ "Reason::" + reason.toString());
        activeVCSessionsCount--;
    }
    
    public static int getActiveVideoCallSessionsCount() {
    	return activeVCSessionsCount;
    }

    /**
     * This method is used to send message to video calling service
     *
     * @param message The message need to be send to video calling service
     */
    public void sendMessage(String message) {
    	try {
    		this.session.getAsyncRemote().sendText(message);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * This method is used to send exit message to video calling service before closing the session 
     * and close the session after message sent to video calling service
     * 
     * @param message The exit message require to send to the video calling service
     */
    public void sendExitMessageAndCloseSession(String message) {
    	try {
    		this.session.getAsyncRemote().sendText(message);
    		
    		new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
						
						session.close();
					} catch(Exception e){
						e.printStackTrace();
					}
					
				}
			}).start();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * This method is used to close the video calling service session
     */
    public void closeSession() {
    	try {
    		System.out.println("Closing video calling socket session");
    		this.session.close();
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    }

}

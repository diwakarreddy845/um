package com.capv.client.user.video_calling;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.MessageHandler;

import com.capv.client.user.TryItUserSession;
/*import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
*/
import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.constants.CapvClientUserConstants.CallState;
import com.capv.client.user.constants.CapvClientUserConstants.UserState;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.websocket.TryItWebSocketEndpoint;
import com.capv.client.user.websocket.UserWebSocketMessage;
import com.capv.um.model.User;
import com.capv.um.model.UserCallState;
import com.capv.um.model.VideoRecording;
import com.capv.um.service.CallStateService;
import com.capv.um.service.UserService;
import com.capv.um.service.VideoRecordingService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * This class is used to connect the capv video calling service 
 * and process the call handling messages between client and video calling service
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class Capv_VC_WS_ClientHandler {
	
	private CapvVideoCallingWebSocketClientConfiguration webSocketClinetConfiguration;
	private Capv_VC_WS_ClientEndpoint capv_VC_WS_ClientEndpoint;
	private MessageHandler.Whole<String> messageHandler;
	
	/**
	 * 
	 * This is the parameterized constructor used to initialize the video calling service 
	 * on behalf of the user connected to the CapV using video calling WebSocket client configuration
	 * 
	 * @param webSocketClinetConfiguration	The web socket client configuration require to 
	 * 										connect the video calling service
	 * 										@see com.capv.client.user.video_calling.CapvVideoCallingWebSocketClientConfiguration
	 * @throws Exception throws the Exception during the video calling service initialization
	 */
	public Capv_VC_WS_ClientHandler( CapvVideoCallingWebSocketClientConfiguration webSocketClinetConfiguration ) throws Exception {
		
		this.webSocketClinetConfiguration = webSocketClinetConfiguration;
	}
	
	public void connectToClient() throws Exception {
		
		UserSession userSession = UserRegistry.getUserSessionBySessionId(webSocketClinetConfiguration.getWebSoketSessionId());
		
		if(userSession != null || webSocketClinetConfiguration.getTryIt()) {
			
			try {
				String vcClientEndpointURIString="";
				
				if(webSocketClinetConfiguration.getTryIt()) {
					String clientIdStr = CapvClientUserUtil.getConfigProperty("client.id");
					Long clientId = Long.parseLong(clientIdStr);
					vcClientEndpointURIString = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.VC_TRYIT_URL_KEY);
				} else
					vcClientEndpointURIString  = CapvClientUserUtil.getClientConfigProperty(userSession.getClientId(), 
																								CapvClientUserConstants.VC_SERVER_URL_KEY);
				
				if(vcClientEndpointURIString != null) {
					URI vcClientEndpointURI = new URI(vcClientEndpointURIString);
					
					Map<String, String> configCustomHeaders = new HashMap<>();
					String originURL = "";
					
					if(vcClientEndpointURIString.startsWith("wss://")) {
						originURL = vcClientEndpointURIString.replace("wss://", "https://");
						originURL = originURL.substring(0, originURL.lastIndexOf("/"));
					} else if(vcClientEndpointURIString.startsWith("ws://")) {
						originURL = vcClientEndpointURIString.replace("ws://", "http://");
						originURL = originURL.substring(0, originURL.lastIndexOf("/"));
					} else {
						throw new Exception("Invalid VC endpoint URI configured. Please contact administrator.");
					}
					configCustomHeaders.put("Origin", originURL);
					
					this.messageHandler = getMessageHandler();
					
					Capv_VC_ClientEndpointConfigurator capv_VC_ClientEndpointConfigurator = new Capv_VC_ClientEndpointConfigurator(configCustomHeaders);
					ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder
																					.create()
																					.configurator(capv_VC_ClientEndpointConfigurator)
																					.build();
				    capv_VC_WS_ClientEndpoint = new Capv_VC_WS_ClientEndpoint(vcClientEndpointURI, clientEndpointConfig, messageHandler);
				    		
				} else {
					throw new Exception("VC endpoint URI not configured. Please contact administrator.");
				}
				
			} catch (Exception e) {
				throw e;
			}
			
		}
		
	}
	
	public boolean isSessionAlive() {
		return capv_VC_WS_ClientEndpoint.isSessionAlive();
	}
	
	public MessageHandler.Whole<String> getMessageHandler() {
		
		MessageHandler.Whole<String> messageHandler = null;
		
		messageHandler = new MessageHandler.Whole<String>() {
			
			@Override
			public void onMessage(String message) {
				System.out.println("Video Calling Socket Message::"+message);
		    	try {
		        		if (message.contains("call_status") && !webSocketClinetConfiguration.getTryIt()){
		        			
		        			JsonObject call_status_room = new JsonParser().parse(message).getAsJsonObject();
		        			
		        			if(call_status_room.get("result") != null) {
		        				
		        				JsonObject resultObj = call_status_room.get("result").getAsJsonObject();
		        				
		        				if(resultObj.get("value") != null) {
		        					JsonObject VideoMessage = new JsonParser().parse(resultObj.get("value").getAsString()).getAsJsonObject();
				        			
				        			String roomNumber = VideoMessage.get("roomNumber").getAsString();
				        			closeRoom(roomNumber);
		        				}
		        			}
		        			
		        		} else {
		        			JsonObject jsonObject = CapvClientUserUtil.convertToJsonObject(message);
		        			if(jsonObject.get("result") != null) {
		        				
		        				JsonObject resultObject = jsonObject.get("result").getAsJsonObject();
		        				
		        				if(resultObject.get("Err") != null) {
		        					String errorMessage = resultObject.get("Err").getAsString();
		        					
		        					if(errorMessage.contains("already exists in room")) {
		        						if(resultObject.get("roomName") != null) {
		            						
		            						String roomNumber = resultObject.get("roomName").getAsString();
		            						
		            						JsonObject leaveMessage = new JsonObject();
		    								
		    								leaveMessage.addProperty(  "method" , "leaveRoom");
		    								leaveMessage.addProperty("id" , 11);
		    								leaveMessage.addProperty("jsonrpc" , "2.0");
		    								
		    								JsonObject params = new JsonObject();
		    								params.addProperty("user", webSocketClinetConfiguration.getUserName());
		    								params.addProperty("roomName", roomNumber);
		    								params.addProperty("updateStatistics", true);
		    								leaveMessage.add("params", params);
		    								
		    								sendMessage(leaveMessage.toString());
		    								
		    								JsonObject roomConflictMessage = new JsonObject();
		    								
		    								roomConflictMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
		    																CapvClientUserConstants.WS_MESSAGE_ROOM_JOIN_CONFLICT);
		    								roomConflictMessage.addProperty("roomNumber", roomNumber);
		    								
		    								if(webSocketClinetConfiguration.getTryIt()) {
		    									
		    									TryItWebSocketEndpoint.removeUserFromRoom(webSocketClinetConfiguration.getUserName(), roomNumber);
		    									
		    			        				TryItUserSession tryItUserSession = 
		    			        						TryItWebSocketEndpoint.getTryItUserSessionBySessionId(webSocketClinetConfiguration.getWebSoketSessionId());
		    				            		
		    			        				if(tryItUserSession != null)
		    			        					tryItUserSession.processMessage(roomConflictMessage.toString());
		    			        			} else {
		    			        				UserSession conflictUserSession = 
		    											UserRegistry.getUserSessionBySessionId(webSocketClinetConfiguration.getWebSoketSessionId());
		    									
		    									UserService userService = webSocketClinetConfiguration.getUserService();
		    									
		    									User conflictUser = userService.getById(conflictUserSession.getUserId());
		    									conflictUser.setCallStatus(UserState.IDLE.getStateId());
		    									userService.update(conflictUser, false);
		    									
		    									try {
		    										Thread.sleep(1000);
		    									} catch (Exception e){}
		    									
		    			        				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
		    																					(conflictUserSession, roomConflictMessage.toString());
		    			        				conflictUserSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
		    			        			}
		    								
		            					}
		        					} else {
		        						JsonObject vcConnectErrorMessage = new JsonObject();
		        						String roomNumber = resultObject.get("roomName").getAsString();
	    								
						    			if(webSocketClinetConfiguration.getTryIt()) {
						    				
						    				vcConnectErrorMessage.addProperty("id", "CallJoinError");
							    			vcConnectErrorMessage.addProperty("error", "VCConnectError");
							    			
						    				TryItWebSocketEndpoint.removeUserFromRoom(webSocketClinetConfiguration.getUserName(), roomNumber);
	    									
	    			        				TryItUserSession tryItUserSession = 
	    			        						TryItWebSocketEndpoint.getTryItUserSessionBySessionId(webSocketClinetConfiguration.getWebSoketSessionId());
	    				            		
	    			        				if(tryItUserSession != null)
	    			        					tryItUserSession.processMessage(vcConnectErrorMessage.toString());
						    			} else {
						    				closeRoom(roomNumber);
						    				
						    				vcConnectErrorMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																				CapvClientUserConstants.WS_MESSAGE_VC_SERVICE_CONNECT_ERROR);
						    				
						    				UserSession userSession = 
	    											UserRegistry.getUserSessionBySessionId(webSocketClinetConfiguration.getWebSoketSessionId());
						    				
						    				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																								(userSession, vcConnectErrorMessage.toString());
					    					userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
						    			}
										
		        					}
		        				} else {
		        					processVCMessage(message);
		        				}
		        			} else if(!message.contains("error")){
		        				processVCMessage(message);
		        			}
		        		}
		    		
		    	} catch(Exception e){
		    		e.printStackTrace();
		    	}
			}
		};
								
		return messageHandler;
	}
	
	private void processVCMessage(String message) {
		
		if(webSocketClinetConfiguration.getTryIt()) {
			TryItUserSession tryItUserSession = 
					TryItWebSocketEndpoint.getTryItUserSessionBySessionId(webSocketClinetConfiguration.getWebSoketSessionId());
    		
			if(tryItUserSession != null)
				tryItUserSession.processMessage(message);
		} else {
			UserSession userSession = UserRegistry.getUserSessionBySessionId(webSocketClinetConfiguration.getWebSoketSessionId());
    		
    		if(userSession != null) {
    			UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, message);
    			userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
    		}
		}
	}
	
	private void closeRoom(String roomNumber) {
		
		CallStateService callStateService	= webSocketClinetConfiguration.getCallStateService();
		UserCallState state					= callStateService.callStateByRoom(roomNumber);
		
		if(state!=null) {
			
			state.setCallStatus(CallState.ENDED.getStateId());
			state.setEndTime(new Date());
			state.setUpdateTime(new Date());
			callStateService.update(state);
			
			if(state.getCallMode() == 1) {
        		Map<String, Object> properties = new HashMap<>();
        		properties.put("roomId", state.getRoomNo());
        		properties.put("isFullVideo", true);
        		VideoRecordingService videoRecordingService = webSocketClinetConfiguration.getVideoRecordingService();
        		VideoRecording videoRecordingModel = 
        				videoRecordingService.getVideoRecordingDetailsByMatchingProperties(properties);
        		
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
        	}
		}
	}
	

    /**
     * This method is used to send message to video calling service
     *
     * @param message The message need to be send to video calling service
     */
    public void sendMessage(String message) {
    	try {
    		capv_VC_WS_ClientEndpoint.sendMessage(message);
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
    		capv_VC_WS_ClientEndpoint.sendMessage(message);
    		
    		new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
						closeClientConnection();
					} catch(Exception e){
						e.printStackTrace();
					}
					
				}
			}).start();
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public void closeClientConnection() {
    	
    	if(capv_VC_WS_ClientEndpoint != null && 
    			capv_VC_WS_ClientEndpoint.isSessionAlive())
    		capv_VC_WS_ClientEndpoint.closeSession();
    	
    	this.messageHandler = null;
    	this.capv_VC_WS_ClientEndpoint = null;
    }
    
}

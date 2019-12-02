package com.capv.client.user.websocket;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.disco.packet.DiscoverItems.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.capv.client.user.User;
import com.capv.client.user.UserChatRoom;
import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.chat.CapvChatClientConfiguration;
import com.capv.client.user.chat.CapvChatClientManager;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.chat.CapvChatUserRequestProcessor;
import com.capv.client.user.chat.handler.CapvChatClientMessagesDeviceSyncHandler;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.constants.CapvClientUserConstants.CallState;
import com.capv.client.user.constants.CapvClientUserConstants.ChatClientAuthStatus;
import com.capv.client.user.constants.CapvClientUserConstants.MessageType;
import com.capv.client.user.constants.CapvClientUserConstants.UserRoomRequestState;
import com.capv.client.user.constants.CapvClientUserConstants.UserState;
import com.capv.client.user.constants.CapvClientUserConstants.VideoCallingClientConnectStatus;
import com.capv.client.user.constants.CapvClientUserConstants.VideoRecordingStatus;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.util.ChatUserUtil;
import com.capv.client.user.video_calling.CapvVideoCallingWebSocketClientConfiguration;
import com.capv.client.user.video_calling.Capv_VC_WS_ClientHandler;
import com.capv.um.cache.CacheUserEntity;
import com.capv.um.cache.UserCacheManager;
import com.capv.um.chat.model.OfMessageArchive;
import com.capv.um.model.OfGroupArchive;
import com.capv.um.model.UserCallState;
import com.capv.um.model.UserConfig;
import com.capv.um.model.UserRoomRequest;
import com.capv.um.model.VideoRecording;
import com.capv.um.rest.validation.ChangePasswordValidator;
import com.capv.um.rest.validation.UserValidator;
import com.capv.um.rest.validation.UserValidator.ValidationType;
import com.capv.um.service.APNSService;
import com.capv.um.service.CallStateService;
import com.capv.um.service.FCMService;
import com.capv.um.service.OfGroupArchiveService;
import com.capv.um.service.OfMessageArchiveService;
import com.capv.um.service.UserConfigService;
import com.capv.um.service.UserRoomRequestService;
import com.capv.um.service.UserService;
import com.capv.um.service.VideoRecordingService;
import com.capv.um.util.CapvUtil;
import com.capv.um.util.CustomComparator;
import com.capv.um.util.CustomGroupComparator;
import com.capv.um.view.ChangePasswordView;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * <h1>Capv WebSocket Endpoint</h1>
 * 
 * This class is used to handle WebSocket messages sent by WebSocket clients 
 * and process them as per the instructions sent by client as part of message
 * 
 * @author caprus.it
 * @version 1.0
 *
 */
public class CapvWebSocketEndpoint extends TextWebSocketHandler {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserConfigService userConfigService;
	
	@Autowired
	private CallStateService callStatesService;
	
	@Autowired
	private VideoRecordingService videoRecordingService;
	
	@Autowired
	private OfMessageArchiveService ofMessageArchiveService;
	
	@Autowired
	private OfGroupArchiveService ofGroupArchiveService;
	
	@Autowired
	private UserCacheManager userCacheManager;
	
	@Autowired
	private APNSService apnsService;
	
	@Autowired
	private FCMService fcmService;
	
	
	@Autowired
	private UserRoomRequestService userRoomRequestService;
	
	private static final Logger log = LoggerFactory.getLogger(CapvWebSocketEndpoint.class);
	
	private static final Gson gson = new GsonBuilder().create();
	
	/**
	 * This method used to get CapvChatUserRequestProcessor to process 
	 * user friend requests, group handling and chat messages processing
	 * 
	 * 
	 * @param userSession	This parameter is require to get associated CapvChatUserRequestProcessor instance
	 * @return CapvChatUserRequestProcessor This return CapvChatUserRequestProcessor to process user request
	 * @see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 */
	private CapvChatUserRequestProcessor getCapvChatUserRequestProcessor(UserSession userSession) {
		
		CapvChatUserRequestProcessor capvChatUserRequestProcessor = null;
		
		if(userSession != null && userSession.getUserName() != null && 
				CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userSession.getUserName()) != null) {
			
			CapvChatClientManager capvChatClientManager = CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userSession.getUserName());
			
			capvChatUserRequestProcessor = (capvChatClientManager.getCapvChatUserRequestProcessor() != null) ? 
														capvChatClientManager.getCapvChatUserRequestProcessor() : null;
		}
			
		return capvChatUserRequestProcessor;
	}
	
	/**
	 * This method is used to process the user messages sent through the user websocket session
	 * <p>
	 * This method process all the chat messages, user friend request messages, group handling messages and video calling messages
	 * <p>
	 * This method expects <b>capv_msg</b> as a key in JSON message and corresponding value of this key differentiates the action
	 * to be taken for chat, friend requests and group management
	 * This method expects <b>id</b> as a key in JSON message and corresponding value of this key used process call management of the user
	 * 
	 * @param session	This is the first parameter of the method refers the WebSocketSession
	 * @param message	This is the second parameter of the method which holds user message sent by websocket client
	 */
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			
	       
			String userName			= UserRegistry.getUserNameBySessionId(session.getId());
			UserSession userSession = UserRegistry.getUserSessionBySessionId(session.getId());
			XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
			String key = userSession.getKey(); 
	        String initVector = userSession.getIv(); 
	       String encryption=(String)session.getAttributes().get("encrytion");
	       final JsonObject jsonMessageObj ;
	        if(encryption.equals("enabled")) {
	        	 	jsonMessageObj = gson.fromJson(CapvUtil.decrypt(key, initVector,message.getPayload()), JsonObject.class);
	        }else {
	        	 	jsonMessageObj = gson.fromJson(message.getPayload(), JsonObject.class);
	        }
			if(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_ID_KEY) != null) {
	    		
	    		switch(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_ID_KEY).getAsString()) {
	    		
	    		case CapvClientUserConstants.WS_MESSAGE_LOGOUT: {
	    			CloseStatus status= new CloseStatus(1000,CapvClientUserConstants.WS_MESSAGE_LOGOUT);
	    			afterConnectionClosed(session,status);
    			}
    			break;	
    			
	    		case CapvClientUserConstants.WS_MESSAGE_SCREEN_SHARE_INTIATED: {
	    			String msg = jsonMessageObj.get(
							CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY).getAsString();
	    			
	    			String receiver = jsonMessageObj.get(
							CapvClientUserConstants.WS_MESSAGE_CHAT_RECEIVER_KEY).getAsString();
	    			JsonObject messageToSend = new JsonObject();
	           		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
	           									CapvClientUserConstants.WS_MESSAGE_SCREEN_SHARE_INTIATED);
					messageToSend.addProperty("msg", msg);
					
					
					String userMessage	= CapvClientUserUtil.convertToJsonString(messageToSend);
					
					CapvChatUserMessageProcessor.sendChatClientMessageToUser(receiver, userMessage);
					CapvChatClientMessagesDeviceSyncHandler.syncChatMessageToSenderDevices(jsonMessageObj, 
																				userSession.getUserName(), 
																				session.getId());
    			}
    			break;
	    		case CapvClientUserConstants.WS_MESSAGE_RECIEVE_FILE: {
	    			
    					com.capv.um.model.User caleeUser = new com.capv.um.model.User();
		    		try{
		    			 String receiver = jsonMessageObj.get(
									CapvClientUserConstants.WS_MESSAGE_CHAT_RECEIVER_KEY).getAsString();
			    		 caleeUser = userService.getByUserName(receiver, false);
			    		 
		    		}
		    		catch (Exception e) {
		    			e.printStackTrace();
					}
					if(userSession != null) {
						CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
													getCapvChatUserRequestProcessor(userSession);
						if(capvChatUserRequestProcessor != null) {
							String msg = jsonMessageObj.get(
												CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY).getAsString();
							String receiver = jsonMessageObj.get(
													CapvClientUserConstants.WS_MESSAGE_CHAT_RECEIVER_KEY).getAsString();
							JsonObject scParams=new JsonObject();
							
							if(msg != null && receiver != null) {
								JsonObject messageToSend = new JsonObject();
				           		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
				           									CapvClientUserConstants.WS_MESSAGE_RECIEVE_FILE);
								messageToSend.addProperty("msg", msg);
								
								
								String userMessage	= CapvClientUserUtil.convertToJsonString(messageToSend);
								
								CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
								CapvChatClientMessagesDeviceSyncHandler.syncChatMessageToSenderDevices(jsonMessageObj, 
																							userSession.getUserName(), 
																							session.getId());
							}
						}
					}

				
    			}
    			break;
    			
	    			case CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND: 
	    												{
	    													com.capv.um.model.User caleeUser = new com.capv.um.model.User();
	    										    		try{
	    										    			 String receiver = jsonMessageObj.get("receiver").getAsString();
	    											    		 caleeUser = userService.getByUserName(receiver, false);
	    											    			String msg = jsonMessageObj.get(
																			CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY).getAsString();
	    											    		 if(msg.isEmpty()){
	    											    			 JsonObject errorToUser = new JsonObject();
												         				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND);
												         				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ERROR, CapvClientUserConstants.WS_MESSAGE_CHAT_EMPTY_MESSAGE);
												         				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, CapvClientUserUtil.convertToJsonString(errorToUser));
															         	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															         	break;
	    											    		 }
	    											    		 
	    										    		}
	    										    		catch (Exception e) {
	    										    			e.printStackTrace();
	    													}
	    													if(userSession != null) {
	    														CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
	    																					getCapvChatUserRequestProcessor(userSession);
	    														if(capvChatUserRequestProcessor != null) {
	    															String msg = jsonMessageObj.get(
	    																				CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY).getAsString();
	    															String receiver = jsonMessageObj.get(
	    																					CapvClientUserConstants.WS_MESSAGE_CHAT_RECEIVER_KEY).getAsString();
	    															String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(caleeUser.getClientId(), CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
	    															
															JsonObject scParams=new JsonObject();
	    															long currentTimestamp = System.currentTimeMillis();
														         	OfGroupArchive oneOneArchiveMessage =new OfGroupArchive();
														         	oneOneArchiveMessage.setBody(msg);
														         	oneOneArchiveMessage.setFromJID(userName+"@"+chatServerServiceName);
														         	oneOneArchiveMessage.setFromJIDResource("Smack");
														         	oneOneArchiveMessage.setToJID(receiver+"@"+chatServerServiceName);
														         	oneOneArchiveMessage.setToJIDResource(userName);
														         	oneOneArchiveMessage.setSentDate(currentTimestamp);
														         	oneOneArchiveMessage.setIsEdited(0);
														         	oneOneArchiveMessage.setIsDeleted(0);
														         String msg_type="1";
														         String reply_message_body="";
														         	if(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE)!=null) {
														         		String type=jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE).getAsString();
														         		if(type.equals("2")) {
														         			oneOneArchiveMessage.setMessage_type(MessageType.REPLY.getTypeId());
														         			msg_type=type;
														         			if(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY)!=null) {
														         			    reply_message_body=CapvClientUserUtil.convertToJsonString(
														         					   jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY).getAsJsonObject());
														         				oneOneArchiveMessage.setReply_message_body(reply_message_body);
														         				msg=reply_message_body+"##"+msg;
														         			}else {
														         				JsonObject errorToUser = new JsonObject();
														         				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND);
														         				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ERROR, CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY);
														         				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, CapvClientUserUtil.convertToJsonString(errorToUser));
																	         	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
																	         	break;
														         			}
														         		}else if(type.equals("1")) {
														         			msg_type="1";
														         			oneOneArchiveMessage.setMessage_type(MessageType.MESSAGE.getTypeId());
													         			}
														         	}else {
														         		/*JsonObject errorToUser = new JsonObject();
														         		errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND);
														         		errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ERROR, CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE);
												         				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, CapvClientUserUtil.convertToJsonString(errorToUser));
															         	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);*/
														         		oneOneArchiveMessage.setMessage_type(MessageType.MESSAGE.getTypeId());
														         		msg_type="1";
															         	//break;
														         	}
														        Long messageId=ofGroupArchiveService.save(oneOneArchiveMessage);
														        JsonObject messageToUser = new JsonObject();
													    			messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND);
													    			messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_ID_KEY, messageId);
													    			
														         	
														         	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, CapvClientUserUtil.convertToJsonString(messageToUser));
														         	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														         
														         	capvChatUserRequestProcessor.sendMessage(messageId+":"+msg_type+":"+msg, receiver);
														         	
	    															if(msg != null && receiver != null) {
	    																if(caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("ios")){
								    							    		
	    																String userFullName = ChatUserUtil.getUserFullName(userName);
	    						    										scParams.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, "message_receive");
	    							    									scParams.addProperty("sender",userName);
	    							    									scParams.addProperty("message", msg);
	    							    									scParams.addProperty("userFullName",userFullName);
	    							    									scParams.addProperty("messageID",messageId);
	    							    									scParams.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE,msg_type);
	    							    									scParams.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY,reply_message_body);
								    							    		apnsService.pushCallNotification(caleeUser.getTokenId(),scParams.toString(),caleeUser.getClientId());
								    							    	}
	    																else if(caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("android")){
	    																	String userFullName = ChatUserUtil.getUserFullName(userName);
	    						    												scParams.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, "message_receive");
	    						    												scParams.addProperty("sender",userName);
	    						    												scParams.addProperty("message", msg);
	    						    												scParams.addProperty("userFullName",userFullName);
	    						    												scParams.addProperty("messageID",messageId);
	    						    												//scParams.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE,msg_type);
	    	    							    										//scParams.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY,reply_message_body);
	    						    												fcmService.sendMessage(caleeUser.getTokenId(),scParams,caleeUser.getClientId());
	    																}
	    																
	    																CapvChatClientMessagesDeviceSyncHandler.syncChatMessageToSenderDevices(jsonMessageObj, 
																																	userSession.getUserName(), 
																																	session.getId());
	    															}
	    														}
	    													}
	    				
	    												}
	    												break;
	    			
	    			case CapvClientUserConstants.WS_MESSAGE_GET_USERS: 
	    												{
	    													if(userSession != null) {
	    														CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																								getCapvChatUserRequestProcessor(userSession);
	    														if(capvChatUserRequestProcessor != null) {
	    															List<User> users = capvChatUserRequestProcessor.getUsers();
	    															if(users != null) {
	    																
	    																JsonObject jsonObject = new JsonObject();
	    																
	        															jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
	        																						CapvClientUserConstants.WS_MESSAGE_USER_LIST);
	        															Type objType = new TypeToken<List<User>>() {}.getType();
	        															jsonObject.add("users", CapvClientUserUtil.convertToJsonElement(users, objType));
	        															
	        															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
	        																											(userSession, 
    																													CapvClientUserUtil.convertToJsonString(jsonObject));
	        															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    																
	    															}
	    														}
	    													}
	    													
	    												}
	    												break;
	    												
	    			case CapvClientUserConstants.WS_MESSAGE_GET_ROSTERUSERS:
										    			{
										    				if(userSession != null) {
																CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																								getCapvChatUserRequestProcessor(userSession);
																if(capvChatUserRequestProcessor != null) {
																	List<User> users = capvChatUserRequestProcessor.getRosterUsers();
																	List<User> pendingList = new ArrayList<User>();
																	if(users != null) {
																		//System.out.println(users.size());
																		for(User user1 : users){
																			if(user1.getStatusPending()){
																				pendingList.add(user1);
																				System.out.println(user1.getStatusPending());
																			}
																			
																			/*if(user.getStatus().equals("subscribe")){
																				users.remove(user);
																			}*/
																		}
																		
																		JsonObject jsonObject = new JsonObject();
																		
																		jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																									CapvClientUserConstants.WS_MESSAGE_ROSTER_LIST);
																		Type objType = new TypeToken<List<User>>() {}.getType();
																		jsonObject.add("users", CapvClientUserUtil.convertToJsonElement(pendingList, objType));
																		
																		UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																														(userSession, 
																														CapvClientUserUtil.convertToJsonString(jsonObject));
																		userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
																		
																	}
																}
															}
															
														}
										    			break;
	    												
	    			case CapvClientUserConstants.WS_MESSAGE_GET_TOTAL_USERS:
										    			{
										    				String clientId="";
										                	CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																							getCapvChatUserRequestProcessor(userSession);
										                	
										                	String searchText = null;
										                	Long lastFetchUserId	= null;
									                		int maxResults			= 25;
										                	
										                	if(jsonMessageObj.get("searchText") != null)
										                		searchText = jsonMessageObj.get("searchText").getAsString();
										                	
										                	if(jsonMessageObj.get("lastFetchUserId") != null) {
									                			String lastFetchUserString = jsonMessageObj.get("lastFetchUserId").getAsString();
									                			try {
									                				lastFetchUserId = new Long(lastFetchUserString);
									                			} catch (NumberFormatException nfe){}
									                		}
									                		if(jsonMessageObj.get("maxResults") != null) {
									                			String maxResultsString = jsonMessageObj.get("maxResults").getAsString();
									                			try {
									                				maxResults = Integer.parseInt(maxResultsString);
									                			} catch (NumberFormatException nfe){}
									                		}
									                		if(jsonMessageObj.get("clientId") != null) {
									                			 clientId = jsonMessageObj.get("clientId").getAsString();
									                		
									                		}
									                		if(maxResults < 1)
									                			maxResults = 25;
									                		if(maxResults > 100)
									                			maxResults = 100;
									                		
									                		List<User> userList = new ArrayList<>();
										                	List<User> users = null;
										                	try {
																users = capvChatUserRequestProcessor.getRosterUsers();
																
																List<String> filteredUsers = new ArrayList<>();
																
																
																for(User rosterUser : users)
																	filteredUsers.add(rosterUser.getName());
																
																filteredUsers.add(userName);
																
																List<CacheUserEntity> searchUserResults = userCacheManager.searchAppUsers(searchText, filteredUsers, 
																																		lastFetchUserId, maxResults,clientId);

																for(CacheUserEntity searchUserResult :searchUserResults) {
																	
																	User searchUser = new User();
																	
																	searchUser.setUser(searchUserResult.getUserName());
																	searchUser.setFullName(searchUserResult.getFirstName() + " " 
																							+ searchUserResult.getLastName());
																	searchUser.setEmail(searchUserResult.getEmail());
																	searchUser.setMobile(searchUserResult.getMobile());
																	searchUser.setRegistrationSource(searchUserResult.getRegistrationSource());
																	searchUser.setProfilePicture(capvChatUserRequestProcessor
																									.getUserProfilePicture(searchUserResult.getUserName()));
																	
																	userList.add(searchUser);
																}
																Long searchResultLastFetchId = null;
																if(searchUserResults.size() == maxResults) {
																	CacheUserEntity searchResultUserEntity = 
																					searchUserResults.get(searchUserResults.size() - 1);
																	searchResultLastFetchId = searchResultUserEntity.getId();
																	
																}
																JsonObject jsonObject = new JsonObject();
																jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																						CapvClientUserConstants.WS_MESSAGE_TOTAL_USERS);
																Type objType = new TypeToken<List<User>>() {}.getType();
																jsonObject.add("userList", CapvClientUserUtil.convertToJsonElement(userList, objType));
																
																if(searchResultLastFetchId != null)
																	jsonObject.addProperty("lastFetchUserId", searchResultLastFetchId);
																else
																	jsonObject.addProperty("maxFetchReached", true);
																
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, 
																												CapvClientUserUtil
																													.convertToJsonString(jsonObject));
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															} catch (Exception e) {
																e.printStackTrace();
															}
										    			}
										    			break;
										    			
	    			case CapvClientUserConstants.WS_MESSAGE_USER_SEARCH: 
									                {
									                	String clientId="";
									                	if(jsonMessageObj.get("user") != null) {
									                		String searchText = jsonMessageObj.get("user").getAsString();
									                		
									                		Long lastFetchUserId	= 0l;
									                		int maxResults			= 25;
									                		
									                		if(jsonMessageObj.get("lastFetchUserId") != null) {
									                			String lastFetchUserString = jsonMessageObj.get("lastFetchUserId").getAsString();
									                			try {
									                				lastFetchUserId = new Long(lastFetchUserString);
									                			} catch (NumberFormatException nfe){}
									                		}
									                		if(jsonMessageObj.get("maxResults") != null) {
									                			String maxResultsString = jsonMessageObj.get("maxResults").getAsString();
									                			try {
									                				maxResults = Integer.parseInt(maxResultsString);
									                			} catch (NumberFormatException nfe){}
									                		}
									                		if(maxResults < 1)
									                			maxResults = 25;
									                		if(maxResults > 100)
									                			maxResults = 100;
									                		if(jsonMessageObj.get("clientId") != null) {
									                			 clientId = jsonMessageObj.get("clientId").getAsString();
									                		
									                		}
										                	List<User> userList = new ArrayList<>();
										                	List<User> users	= null;
										                	CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																						getCapvChatUserRequestProcessor(userSession);
										                	try {
																users = capvChatUserRequestProcessor.getRosterUsers();
																List<String> filteredUsers = new ArrayList<>();
																
																
																for(User rosterUser : users)
																	filteredUsers.add(rosterUser.getName());
																
																filteredUsers.add(userName);
																
																List<CacheUserEntity> searchUserResults = userCacheManager.searchAppUsers(searchText, filteredUsers, 
																																		lastFetchUserId, maxResults,clientId);
																
																for(CacheUserEntity searchUserResult :searchUserResults) {
																	
																	User searchUser = new User();
																	
																	searchUser.setUser(searchUserResult.getUserName());
																	searchUser.setFullName(searchUserResult.getFirstName() + " " 
																							+ searchUserResult.getLastName());
																	searchUser.setEmail(searchUserResult.getEmail());
																	searchUser.setMobile(searchUserResult.getMobile());
																	searchUser.setRegistrationSource(searchUserResult.getRegistrationSource());
																	searchUser.setProfilePicture(capvChatUserRequestProcessor
																									.getUserProfilePicture(searchUserResult.getUserName()));
																	
																	userList.add(searchUser);
																}
																
																Long searchResultLastFetchId = null;
																if(searchUserResults.size() == maxResults) {
																	CacheUserEntity searchResultUserEntity = 
																					searchUserResults.get(searchUserResults.size() - 1);
																	searchResultLastFetchId = searchResultUserEntity.getId();
																	
																}
																JsonObject jsonObject = new JsonObject();
																jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																						CapvClientUserConstants.WS_MESSAGE_SEARCH_USER);
																Type objType = new TypeToken<List<User>>() {}.getType();
																jsonObject.add("userList", CapvClientUserUtil.convertToJsonElement(userList, objType));
																
																if(searchResultLastFetchId != null)
																	jsonObject.addProperty("lastFetchUserId", searchResultLastFetchId);
																else
																	jsonObject.addProperty("maxFetchReached", true);
																
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, 
																														CapvClientUserUtil
																															.convertToJsonString(jsonObject));
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															} catch (Exception e) {
																e.printStackTrace();
															}
									                	}
									                 }
									                 break;
									                 
	    			case CapvClientUserConstants.WS_MESSAGE_ADD_FRIEND:
									    			{
									    				String userFullName = null;
									    				
									    				String name = jsonMessageObj.get("name").getAsString();
									    				
									    				com.capv.um.model.User user = userService.getByUserName(name, false);
									    				
									    				if(user != null)
									    					userFullName = user.getName();
									    				
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																						getCapvChatUserRequestProcessor(userSession);

								    					capvChatUserRequestProcessor.addBuddy(name, userFullName);
								    				
									    				if(user != null && user.getLastSigninOs() != null && 
									    						user.getLastSigninOs().equals("ios")){
									    					JsonObject scParams=new JsonObject();
									    					scParams.addProperty("capv_msg", "friend_request");
					    									scParams.addProperty("from",user.getUserName());
					    									scParams.addProperty("userFullName",userFullName);
									    					apnsService.pushCallNotification(user.getTokenId(), scParams.toString(),user.getClientId());
									    					//capvChatUserRequestProcessor.addBuddy(name, userFullName);
									    				}else if(user != null && user.getLastSigninOs() != null && 
									    						user.getLastSigninOs().equals("android")) {
									    					JsonObject scParams=new JsonObject();
									    					scParams.addProperty("capv_msg", "friend_request");
					    									scParams.addProperty("from1",userName);
					    									if(CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName) != null) {
										    					String orginUserFullName = ChatUserUtil.getUserFullName(userName);
										    					if(orginUserFullName != null) {
										    						scParams.addProperty("userFullName",orginUserFullName);
										    					}
										    				}
									    					fcmService.sendMessage(user.getTokenId(), scParams,user.getClientId());
									    					//capvChatUserRequestProcessor.addBuddy(name, userFullName);
									    				}
									    				
									    			}
									    			 break;
	    			case CapvClientUserConstants.WS_MESSAGE_DELETE_GROUP:
									    			{
									    				JsonArray roomsList = null;
									    				roomsList = jsonMessageObj.get("roomsList").getAsJsonArray();
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																						getCapvChatUserRequestProcessor(userSession);
									    				Boolean roomResponse = capvChatUserRequestProcessor.leaveRoom(roomsList);
									    				
									    				System.out.println("roomResponse for delete : "+roomResponse);
									    				
									    				if(roomResponse){
									    					for(JsonElement room: roomsList){
									    						System.out.println("roomName : "+room.getAsString()+"@conference."+chatUserConnection.getServiceName());
									    						List<UserRoomRequest> userRoomRequestList = userRoomRequestService.getRecordByRoomName(room.getAsString()+"@conference."+chatUserConnection.getServiceName());
									    						
									    						if(userRoomRequestList != null && userRoomRequestList.size() >0){
									    							for(UserRoomRequest userRoomRequest : userRoomRequestList){
									    								System.out.println("set delete flag : "+userRoomRequest.getIsPending());
									    								userRoomRequest.setIsPending(UserRoomRequestState.DELETED.getStateId());
									    								userRoomRequest.setUpdateDate(new Date());
									    								userRoomRequestService.update(userRoomRequest);
									    							}
									    						}
									    					}
									    				}
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_TRANSFER_PRIVILEGE:
	    			{
	    				String roomName = jsonMessageObj.get("roomName").getAsString();
	    				String toJID = jsonMessageObj.get("to").getAsString();
	    				JsonObject transferPriviliage=new JsonObject();
	    				transferPriviliage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,CapvClientUserConstants.WS_MESSAGE_TRANSFER_PRIVILEGE);
	    				
	    				if(toJID!=null&&toJID!=null&&roomName!=null) {
	    					
	    					transferPrivilage(roomName, userSession,chatUserConnection,userName,toJID);
	    					
	    				}else {
	    					transferPriviliage.addProperty("message","Invalid Params");
		    				transferPriviliage.addProperty("status", "failure");
	    					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession,  transferPriviliage.toString());
	    					userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    				}
	    				
	    			}
	    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_DELETE_FRIEND:
									    			{
									    				JsonArray friendsList = null;
									    				friendsList = jsonMessageObj.get("friendsList").getAsJsonArray();
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																					getCapvChatUserRequestProcessor(userSession);
									    				capvChatUserRequestProcessor.removeFriends(friendsList);
									    			}
									    			break;
									    			
	    			case CapvClientUserConstants.WS_MESSAGE_GET_ROOM_DETAILS:
									    			{
									    				if(jsonMessageObj.get("room") != null) {
									    					try {
									    						Integer propertyValue = Integer.parseInt(CapvClientUserUtil.getClientConfigProperty(userSession.getClientId(), "capv.video.groupsize"));
									    						String room = jsonMessageObj.get("room").getAsString();
									    						room = room + "@conference."+CapvClientUserUtil.getClientConfigProperty(
													    														userSession.getClientId(), 
													    														CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
									    						CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																									getCapvChatUserRequestProcessor(userSession);
									    						//UserChatRoom userChatRoom = getTUserChatRoomDetails(room,userName,userSession.getClientId());
									    						
									    						Map<String, String> pendingOccupants = new HashMap<>();
									    						UserChatRoom userChatRoom = capvChatUserRequestProcessor.getUserChatRoomDetails(room);
									    						if(userChatRoom != null) {
									    							JsonObject messageToSend = new JsonObject();
									    							messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
		    																			CapvClientUserConstants.WS_MESSAGE_GET_ROOM_DETAILS_RESPONSE);
									    							UserCallState groupCall =callStatesService.getLastActiveGroupCallByJid(userChatRoom.getJid());
																	if(groupCall!=null) {
																		userChatRoom.setProgress(true);
																	}
																	if(userChatRoom.getOccupantsLength()>propertyValue) {
															        		userChatRoom.setVideoEnable(false);
															        	}else {
															        		userChatRoom.setVideoEnable(true);
															        	}
																	
																	List<UserRoomRequest> pendinRequestList= userRoomRequestService.getUserPendingRequestRoom(jsonMessageObj.get("room").getAsString()+"@conference."+chatUserConnection.getServiceName());
											                		JsonArray pendingReqArray= new JsonArray();
											                		for(UserRoomRequest obj:pendinRequestList){
											                			String member_name=obj.getInvitee().substring(0, obj.getInvitee().indexOf("@"));
											                			pendingOccupants.put(member_name, ChatUserUtil.getUserFullName(member_name));
											                		}
											                		userChatRoom.setPendingOccupants(pendingOccupants);
									    							Type objType = new TypeToken<UserChatRoom>() {}.getType();
									    							messageToSend.add("roomDetails", CapvClientUserUtil.convertToJsonElement(userChatRoom, objType));
									    							
									    							UserWebSocketMessage userWebSocketMessage = 
									    												new UserWebSocketMessage(userSession,  messageToSend.toString());
																	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									    						}
									    						
									    					} catch (Exception e){}
									    				}
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_GET_GROUP_GROUP_DETAILS:
	    			{
	    					try {
	    							List<UserCallState> groupCallList =callStatesService.getActiveGroupCallListByClientId(userSession.getClientId());
									if(groupCallList!=null) {
										Type objType = new TypeToken<List<UserCallState>>() {}.getType();
										JsonObject messageToSend = new JsonObject();
		    							messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
															CapvClientUserConstants.WS_MESSAGE_GET_GROUP_GROUP_DETAILS_RESPONSE);
		    							messageToSend.add("result", CapvClientUserUtil.convertToJsonElement(groupCallList, objType));
		    							UserWebSocketMessage userWebSocketMessage = 
												new UserWebSocketMessage(userSession,  messageToSend.toString());
							userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									}
	    						
	    					} catch (Exception e){
	    						
	    					}
	    				
	    			}
	    			break;				    			 
	    			case CapvClientUserConstants.WS_MESSAGE_CHANGE_USER_PRESENCE:
									    			{
									    				String presence = jsonMessageObj.get("presence").getAsString();
									    				String status = jsonMessageObj.get("status").getAsString();
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																						getCapvChatUserRequestProcessor(userSession);
									    				capvChatUserRequestProcessor.changeUserPresence(presence, status);
									    			}
									    			break;
									    			
	    			case CapvClientUserConstants.WS_MESSAGE_GET_USER_PRESENCE:
									    			{
									    				String userNameForPresence = jsonMessageObj.get("user_name").getAsString();
									    				
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																						getCapvChatUserRequestProcessor(userSession);
									    				capvChatUserRequestProcessor.getUserPresence(userNameForPresence);
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_VIDEO_DELETE_USERS : 
					{

							String room_id = jsonMessageObj.get("delete_room_id").getAsString();
							if(room_id!=null){
								
								UserCallState call_red_vid_delete = callStatesService.getCallLogRoomList(room_id);
								
								call_red_vid_delete.setDelete_vid_flag(1);
								callStatesService.update(call_red_vid_delete);
								
								JsonObject jsonObject = new JsonObject();
							
								jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
														CapvClientUserConstants.WS_VIDEO_DELETE_USERS);
							
								jsonObject.addProperty("msg", "Recorded Video Successfully deleted.");
							
								UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																			(userSession, 
																			CapvClientUserUtil.convertToJsonString(jsonObject));
								userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
							}
					}
					break;				    			 
	    			case CapvClientUserConstants.WS_MESSAGE_SELECTED_FRIENDS:
									    			{
									    				JsonArray friendsList = null;
									    				friendsList = jsonMessageObj.get("friendsList").getAsJsonArray();
									    				String roomname = jsonMessageObj.get("roomname").getAsString();
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																					getCapvChatUserRequestProcessor(userSession);
									    				
									    				Boolean roomResponse = capvChatUserRequestProcessor.createRoom(roomname.toLowerCase(),friendsList);
									    				
									    				JsonObject createRoomResponse=new JsonObject();
									    				createRoomResponse.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																			CapvClientUserConstants.WS_MESSAGE_CREATE_ROOM_RESPONSE);
									    				
									    				if(!roomResponse){
									    					
									    					createRoomResponse.addProperty("message", roomname.toLowerCase()+" already exits" );
									    					createRoomResponse.addProperty("status", "failure");
									    					createRoomResponse.addProperty("room", roomname.toLowerCase());
    														
					    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																											(userSession, createRoomResponse.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															
									    				} else {
									    					try{
									    					
										    				 for(JsonElement friend : friendsList){
										    					 UserRoomRequest userRoomReq=new UserRoomRequest();
										    					 userRoomReq.setInvitee(friend.getAsString());
										    					 userRoomReq.setInviter(userName);
										    					 userRoomReq.setIsPending(UserRoomRequestState.PENDING.getStateId());
										    					 userRoomReq.setRoomName(roomname.toLowerCase()+"@conference."+chatUserConnection.getServiceName());
										    					 userRoomReq.setCreatedDate(new Date());
										    					 userRoomReq.setUpdateDate(new Date());
										    					 userRoomRequestService.save(userRoomReq);
										    	             }
									    					}
									    					catch(Exception e){
									    						e.printStackTrace();
									    						log.debug("Exception is saving room request in user_room_request",e.getMessage());
									    					}
										    				
									    					createRoomResponse.addProperty("message", roomname.toLowerCase()+" created successfully" );
									    					createRoomResponse.addProperty("status", "success");
									    					createRoomResponse.addProperty("room", roomname.toLowerCase());
    														
					    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																											(userSession, createRoomResponse.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									    					
									    					Iterator<JsonElement> friendList = friendsList.iterator();
									    					
									    					while(friendList.hasNext()) {
									    						
									    						String friendJid = friendList.next().getAsString();
									    						String friendName = friendJid.substring(0, friendJid.indexOf("@"));
									    						
									    						com.capv.um.model.User friend = userService.getByUserName(friendName, false);
																
																
																if(friend != null && friend.getLastSigninOs() != null && 
																		friend.getLastSigninOs().equals("ios")){
																	
																	JsonObject groupRequestAPNSMessage = new JsonObject();
																	
																	groupRequestAPNSMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
					    																				CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
												    				groupRequestAPNSMessage.addProperty("room", roomname);
												    				groupRequestAPNSMessage.addProperty("inviter", userName);
												    				groupRequestAPNSMessage.addProperty("reason", 
										    														"Hi "+friend.getName()+" welcome to my chat room "+roomname);
																	
																	apnsService.pushCallNotification(friend.getTokenId(), 
																									groupRequestAPNSMessage.toString(),friend.getClientId());
											    				}else if(friend != null && friend.getLastSigninOs() != null && 
																		friend.getLastSigninOs().equals("android")) {
											    						JsonObject groupRequestAPNSMessage = new JsonObject();
																	
																	groupRequestAPNSMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
					    																				CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
												    				groupRequestAPNSMessage.addProperty("room", roomname);
												    				groupRequestAPNSMessage.addProperty("inviter", userName);
												    				groupRequestAPNSMessage.addProperty("reason", 
										    														"Hi "+friend.getName()+" welcome to my chat room "+roomname);
																	
																	fcmService.sendMessage(friend.getTokenId(),groupRequestAPNSMessage,friend.getClientId());
											    				}
									    					}
									    				}
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_ADD_GROUP_FRIENDS:
	    			{
	    				JsonArray friendsList = null;
	    				friendsList = jsonMessageObj.get("friendsList").getAsJsonArray();
	    				String roomname = jsonMessageObj.get("roomname").getAsString();
	    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
													getCapvChatUserRequestProcessor(userSession);
	    				
	    				//Boolean roomResponse = capvChatUserRequestProcessor.editRoom(roomname,friendsList);
	    				
	    				JsonObject createRoomResponse=new JsonObject();
	    				createRoomResponse.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
											CapvClientUserConstants.WS_MESSAGE_ADD_GROUP_FRIENDS);
	    			
	    					try{
	    					
		    				 for(JsonElement friend : friendsList){
		    					 if(userRoomRequestService.getUniqeRecord(friend.getAsString(),roomname+"@conference."+chatUserConnection.getServiceName())==null){
		    					 UserRoomRequest userRoomReq=new UserRoomRequest();
		    					 userRoomReq.setInvitee(friend.getAsString());
		    					 userRoomReq.setInviter(userName);
		    					 userRoomReq.setIsPending(UserRoomRequestState.PENDING.getStateId());
		    					 userRoomReq.setRoomName(roomname+"@conference."+chatUserConnection.getServiceName());
		    					 userRoomReq.setCreatedDate(new Date());
		    					 userRoomReq.setUpdateDate(new Date());
		    					 capvChatUserRequestProcessor.editRoom(roomname,friend.getAsString());
		    					 userRoomRequestService.save(userRoomReq);
		    					 }
		    	             }
	    					}
	    					catch(Exception e){
	    						e.printStackTrace();
	    						log.debug("Exception is saving room request in user_room_request",e.getMessage());
	    					}
		    				
	    					createRoomResponse.addProperty("message", "Added Friends successfully to Group "+roomname);
	    					createRoomResponse.addProperty("status", "success");
	    					createRoomResponse.addProperty("room", roomname);
							
							UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																			(userSession, createRoomResponse.toString());
							userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    					
	    					Iterator<JsonElement> friendList = friendsList.iterator();
	    					
	    					while(friendList.hasNext()) {
	    						
	    						String friendJid = friendList.next().getAsString();
	    						String friendName = friendJid.substring(0, friendJid.indexOf("@"));
	    						
	    						com.capv.um.model.User friend = userService.getByUserName(friendName, false);
								
								
								if(friend != null && friend.getLastSigninOs() != null && 
										friend.getLastSigninOs().equals("ios")){
									
									JsonObject groupRequestAPNSMessage = new JsonObject();
									
									groupRequestAPNSMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																		CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
				    				groupRequestAPNSMessage.addProperty("room", roomname);
				    				groupRequestAPNSMessage.addProperty("inviter", userName);
				    				groupRequestAPNSMessage.addProperty("reason", 
		    														"Hi "+friend.getName()+" welcome to my chat room "+roomname);
									
									apnsService.pushCallNotification(friend.getTokenId(), 
																	groupRequestAPNSMessage.toString(),friend.getClientId());
			    				}else if(friend != null && friend.getLastSigninOs() != null && 
										friend.getLastSigninOs().equals("android")){
			    						JsonObject groupRequestAPNSMessage = new JsonObject();
									
									groupRequestAPNSMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																		CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
				    				groupRequestAPNSMessage.addProperty("room", roomname);
				    				groupRequestAPNSMessage.addProperty("inviter", userName);
				    				groupRequestAPNSMessage.addProperty("reason", 
		    														"Hi "+friend.getName()+" welcome to my chat room "+roomname);
									
									fcmService.sendMessage(friend.getTokenId(), groupRequestAPNSMessage,friend.getClientId());
			    				}
	    					}
	    				
	    			}
	    			break;	
	    			case CapvClientUserConstants.WS_MESSAGE_EDIT_GROUP_FRIENDS:
	    			{
	    				JsonArray friendsList = null;
	    				JsonArray deleteList = null;
	    				friendsList = jsonMessageObj.get("friendsList").getAsJsonArray();
	    				deleteList = jsonMessageObj.get("deleteFriendList").getAsJsonArray();
	    				
	    				String roomname = jsonMessageObj.get("roomname").getAsString();
	    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
													getCapvChatUserRequestProcessor(userSession);
	    				
	    				//Boolean roomResponse = capvChatUserRequestProcessor.editRoom(roomname,friendsList);
	    				if(deleteList!=null&&deleteList.size()>0) {
	    					deleteGroupMember(deleteList, roomname, userSession, chatUserConnection, userName);
	    				}
	    				if(friendsList!=null&&friendsList.size()>0) {
	    					
	    			
	    					try{
	    					 for(JsonElement friend : friendsList){
		    				   if(userRoomRequestService.getUniqeRecord(friend.getAsString(),roomname+"@conference."+chatUserConnection.getServiceName())==null){
		    					 UserRoomRequest userRoomReq=new UserRoomRequest();
		    					 userRoomReq.setInvitee(friend.getAsString());
		    					 userRoomReq.setInviter(userName);
		    					 userRoomReq.setIsPending(UserRoomRequestState.PENDING.getStateId());
		    					 userRoomReq.setRoomName(roomname+"@conference."+chatUserConnection.getServiceName());
		    					 userRoomReq.setCreatedDate(new Date());
		    					 userRoomReq.setUpdateDate(new Date());
		    					 capvChatUserRequestProcessor.editRoom(roomname,friend.getAsString());
		    					 userRoomRequestService.save(userRoomReq);
		    					 }
		    	              }
	    					}
	    					catch(Exception e){
	    						e.printStackTrace();
	    						log.debug("Exception is saving room request in user_room_request",e.getMessage());
	    					}
	    					
	    					Iterator<JsonElement> friendList = friendsList.iterator();
	    					
	    					while(friendList.hasNext()) {
	    						
	    						String friendJid = friendList.next().getAsString();
	    						String friendName = friendJid.substring(0, friendJid.indexOf("@"));
	    						
	    						com.capv.um.model.User friend = userService.getByUserName(friendName, false);
								
								
								if(friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("ios")){
									JsonObject groupRequestAPNSMessage = new JsonObject();
									
									groupRequestAPNSMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
				    					groupRequestAPNSMessage.addProperty("room", roomname);
				    					groupRequestAPNSMessage.addProperty("inviter", userName);
				    					groupRequestAPNSMessage.addProperty("reason", "Hi "+friend.getName()+" welcome to my chat room "+roomname);
									
									apnsService.pushCallNotification(friend.getTokenId(),groupRequestAPNSMessage.toString(),friend.getClientId());
									
			    					}else if(friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("android")){
			    						JsonObject groupRequestAPNSMessage = new JsonObject();
									groupRequestAPNSMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
				    					groupRequestAPNSMessage.addProperty("room", roomname);
				    					groupRequestAPNSMessage.addProperty("inviter", userName);
				    					groupRequestAPNSMessage.addProperty("reason", "Hi "+friend.getName()+" welcome to my chat room "+roomname);
									fcmService.sendMessage(friend.getTokenId(), groupRequestAPNSMessage,friend.getClientId());
			    				}
	    					}
	    				}
	    				JsonObject createRoomResponse=new JsonObject();
    					createRoomResponse.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,CapvClientUserConstants.WS_MESSAGE_EDIT_GROUP_FRIENDS);
    					createRoomResponse.addProperty("message",roomname+" Group Updated successfully ");
    					createRoomResponse.addProperty("status", "success");
    					createRoomResponse.addProperty("room", roomname);
						
						UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, createRoomResponse.toString());
						userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    			}
	    			break;	
	       		case CapvClientUserConstants.WS_MESSAGE_DELETE_GROUP_FRIENDS:
	    			{
	    				
	    			}
	    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_ACCEPT_FREIND_REQUEST:
									    			{
									    				
									    				String name = jsonMessageObj.get("name").getAsString();
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																					getCapvChatUserRequestProcessor(userSession);
									    				capvChatUserRequestProcessor.acceptFreindRequest(name);
									    				
									    				User subscribedUser = new User();
									    				
									    				String chatServerServiceName = 
									    						CapvClientUserUtil.getClientConfigProperty(userSession.getClientId(), 
									    																	CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
						        						
									    				subscribedUser.setUser(name + "@"+chatServerServiceName);
									    				subscribedUser.setName(name);
									    				
									    				CacheUserEntity cacheUser = userCacheManager.getUserByUserName(name);
									    				if(cacheUser != null)
									    					subscribedUser.setFullName(cacheUser.getFirstName() + " " + cacheUser.getLastName());
						        						
									    				capvChatUserRequestProcessor.getUserPresence(userName);
					        							subscribedUser.setStatus(Presence.Type.unavailable.toString());
								       					
									    				java.lang.reflect.Type objType = new TypeToken<User>() {}.getType();
									    				JsonObject messageToSend = new JsonObject();
								       					
								       					messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
								       												CapvClientUserConstants.WS_MESSAGE_FRIEND_SUBSCRIBED);
			        				
								        				messageToSend.add("user_subscribed", CapvClientUserUtil.convertToJsonElement(subscribedUser, objType));
								        				
						        						String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
										       			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
									    			}
									    			 break;
									    			 
	    			case CapvClientUserConstants.WS_MESSAGE_DECLINE_FRIEND_REQUEST:
									    			{
									    				System.out.println("declinecheck");
									    				String name = jsonMessageObj.get("name").getAsString();
									    				System.out.println(name);
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																					getCapvChatUserRequestProcessor(userSession);
									    				capvChatUserRequestProcessor.declineFreindRequest(name);
									    			}
									    			break;
									    			
	    			case CapvClientUserConstants.WS_MESSAGE_ACCEPT_ROOM_REQUEST:
									    			{
									    				String room = jsonMessageObj.get("room").getAsString();
									    				//System.out.println(room);
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
									    				
									    				List<Item> occuPantList=capvChatUserRequestProcessor.getOccupantsByRoom(room);
									    				
									    				if(occuPantList!=null)
									    				{
									    					if(occuPantList.size()<1) {
									    						List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
																if(userSessions != null){
																	Long receiverId = null;
																	for(UserSession receiverSession :userSessions)
																	{
																		if(receiverId == null)
																			receiverId = receiverSession.getUserId();
																		
																		JsonObject messageToSend = new JsonObject();
																		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																								CapvClientUserConstants.WS_MESSAGE_ACCEPT_ROOM_REQUEST_RESPONSE);
																		messageToSend.addProperty("message", "Room has been Deleted");
																		messageToSend.addProperty("status", "success");
																		
																		UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																														(receiverSession, messageToSend.toString());
																		receiverSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
																		
																		
																		UserRoomRequest userRoomRequest=userRoomRequestService.getUniqeRecord(userName+"@"+chatUserConnection.getServiceName(), room);
																		userRoomRequest.setIsPending(UserRoomRequestState.ACCEPTED.getStateId());
																		userRoomRequest.setUpdateDate(new Date());
																		userRoomRequestService.update(userRoomRequest);
																	}
															 }
									    					}else {
									    						
											    				
											    				String memberJoinedRoom = room.substring(0, room.indexOf("@"));
																
																List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
																if(userSessions != null){
																	Long receiverId = null;
																	for(UserSession receiverSession :userSessions)
																	{
																		if(receiverId == null)
																			receiverId = receiverSession.getUserId();
																		
																		JsonObject messageToSend = new JsonObject();
																		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																								CapvClientUserConstants.WS_MESSAGE_ACCEPT_ROOM_REQUEST_RESPONSE);
																		messageToSend.addProperty("message", "Room request accepted successfully");
																		messageToSend.addProperty("status", "success");
																		messageToSend.addProperty("room", memberJoinedRoom);
																		
																		UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																														(receiverSession, messageToSend.toString());
																		receiverSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
																		
																		
																		UserRoomRequest userRoomRequest=userRoomRequestService.getUniqeRecord(userName+"@"+chatUserConnection.getServiceName(), room);
																		userRoomRequest.setIsPending(UserRoomRequestState.ACCEPTED.getStateId());
																		userRoomRequest.setUpdateDate(new Date());
																		userRoomRequestService.update(userRoomRequest);
																		capvChatUserRequestProcessor.joinRoom(room);
																	}
															 }
									    					}
									    						String memberJoinedRoom = room.substring(0, room.indexOf("@"));
																for (int i=0;i<occuPantList.size();i++) {
																	String occupantName =occuPantList.get(i).getEntityID();
																	int index = 0;
													        		if(occupantName.lastIndexOf("/") >= 0)
													        			index = occupantName.lastIndexOf("/") + 1;
													        		
													        		occupantName = occupantName.substring(index, occupantName.length());
																	List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(occupantName);
																	if(userSessions != null){
																		Long receiverId = null;
																		for(UserSession receiverSession :userSessions)
																		{
																			if(receiverId == null)
																				receiverId = receiverSession.getUserId();
																	
																			JsonObject messageToSend = new JsonObject();
																			messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_ACCEPT_ROOM_REQUEST_RESPONSE);
																			messageToSend.addProperty("message", "accepted room request successfully");
																			messageToSend.addProperty("status", "success");
																			messageToSend.addProperty("room", memberJoinedRoom);
																	
																	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																													(receiverSession, messageToSend.toString());
																	receiverSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
																}
															}
														  }
									    				}
									    				
									    			}
									    			 break;
	    			case CapvClientUserConstants.WS_MESSAGE_DECLINE_ROOM_REQUEST:{
	    				UserRoomRequest userRoomRequest=userRoomRequestService.getUniqeRecord(userName+"@"+chatUserConnection.getServiceName(), jsonMessageObj.get("room").getAsString());
						userRoomRequest.setIsPending(UserRoomRequestState.REJECTED.getStateId());
						userRoomRequest.setUpdateDate(new Date());
						userRoomRequestService.update(userRoomRequest);
	    			}
	    			break;
									    			 
	    			case CapvClientUserConstants.WS_MESSAGE_GET_HOSTEDROOMS:
									    			{
									    				List<UserChatRoom> joinedRooms = null;
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																				getCapvChatUserRequestProcessor(userSession);
									    				
									                	try {
									                			String host_service=CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName).getService();
									                		//joinedRooms=joinedTRooms( userName+"@"+chatUserConnection.getServiceName(), chatUserConnection.getServiceName(),userSession.getClientId(),capvChatUserRequestProcessor);
										                	joinedRooms = capvChatUserRequestProcessor.joinedRooms(userSession.getClientId());
										                		List<UserRoomRequest> pendinRequestList= userRoomRequestService.getUserPendingRequest(userName+"@"+chatUserConnection.getServiceName());
										                		JsonArray pendingReqArray= new JsonArray();
										                		for(UserRoomRequest obj:pendinRequestList){
										                			JsonObject pendingReqObj = new JsonObject();
										                			pendingReqObj.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
										                			pendingReqObj.addProperty("room",obj.getRoomName());
										                			pendingReqObj.addProperty("inviter", obj.getInviter());
										                			pendingReqObj.addProperty("reason", "Welcome to my room");
										                			pendingReqArray.add(pendingReqObj);
										                			
										                		}
										                		if(pendingReqArray.size()>0){
										                			JsonObject messageToSend = new JsonObject();
										                			messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
										                			messageToSend.addProperty("pendingReqArray",CapvClientUserUtil.convertToJsonString(pendingReqArray));
										                			String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
										                			
										                			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
										                		}
																JsonObject jsonObject = new JsonObject();
																
																jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																						CapvClientUserConstants.WS_MESSAGE_SET_HOSTEDROOMS);
																if(joinedRooms!=null) {
																	for(int i=0;i<joinedRooms.size();i++) {
																		UserCallState groupCall =callStatesService.getLastActiveGroupCallByJid(joinedRooms.get(i).getJid());
																		if(groupCall!=null) {
																			joinedRooms.get(i).setProgress(true);
																		}
																	}
																}
																Type objType = new TypeToken<List<UserChatRoom>>() {}.getType();
																jsonObject.add("hostedRooms", CapvClientUserUtil.convertToJsonElement(joinedRooms, objType));
																
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, jsonObject.toString());
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														} catch (NoResponseException e) {
															e.printStackTrace();
														} catch (XMPPErrorException e) {
															e.printStackTrace();
														} catch (NotConnectedException e) {
															e.printStackTrace();
														} catch (Exception e) {
															e.printStackTrace();
														}
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_SEND:
									    			{
									    	    		
									    				String room = jsonMessageObj.get("room").getAsString();
									    				String msg ="";
									    				try {
									    					msg=jsonMessageObj.get("message").getAsString();
															if(msg.isEmpty()){
																JsonObject errorToUser = new JsonObject();
										         				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND);
										         				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ERROR, CapvClientUserConstants.WS_MESSAGE_CHAT_EMPTY_MESSAGE);
										         				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, CapvClientUserUtil.convertToJsonString(errorToUser));
													         	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
													         	break;
															}
														} catch (Exception e) {
															e.printStackTrace();
														}
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
									    				String roomName = room + "@conference." + chatUserConnection.getServiceName();
										    			String  toJid=userName+"@"+chatUserConnection.getServiceName();
									    				long currentTimestamp = System.currentTimeMillis();
											         	OfGroupArchive groupArchiveMessage =new OfGroupArchive();
											         	groupArchiveMessage.setBody(msg);
											         	groupArchiveMessage.setFromJID(toJid);
											         	groupArchiveMessage.setFromJIDResource("Smack");
											         	groupArchiveMessage.setToJID(roomName);
											         	groupArchiveMessage.setToJIDResource(userName);
											         	groupArchiveMessage.setSentDate(currentTimestamp);
											         	groupArchiveMessage.setIsEdited(0);
											         	groupArchiveMessage.setIsDeleted(0);
											         	String msg_type="1";
												         String reply_message_body="";
												         	if(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE)!=null) {
												         		String type=jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE).getAsString();
												         		if(type.equals("2")) {
												         			groupArchiveMessage.setMessage_type(MessageType.REPLY.getTypeId());
												         			msg_type="2";
												         			if(jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY)!=null) {
												         				reply_message_body=CapvClientUserUtil.convertToJsonString(
													         					   jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY).getAsJsonObject());
												         			   // reply_message_body=jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY).getAsString();
												         			   groupArchiveMessage.setReply_message_body(reply_message_body);
												         				msg=reply_message_body+"##"+msg;
												         			}else {
												         				JsonObject errorToUser = new JsonObject();
												         				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND);
												         				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ERROR, CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY);
												         				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, CapvClientUserUtil.convertToJsonString(errorToUser));
															         	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															         	break;
												         			}
												         		}else if(type.equals("1")) {
												         			groupArchiveMessage.setMessage_type(MessageType.MESSAGE.getTypeId());
											         			}
												         	}else {
												         		/*JsonObject errorToUser = new JsonObject();
												         		errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND);
												         		errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ERROR, CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE);
										         				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, CapvClientUserUtil.convertToJsonString(errorToUser));
													         	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);*/
												         		groupArchiveMessage.setMessage_type(MessageType.MESSAGE.getTypeId());
													         	//break;
												         	}
											         	
											         	Long msgId=ofGroupArchiveService.save(groupArchiveMessage);
											         	
										    			capvChatUserRequestProcessor.sendGroupMessage(room, msgId+":"+msg_type+":"+msg);
										    			
										    			JsonObject messageToUser = new JsonObject();
										    			messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_SEND);
										    			messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_ID_KEY, msgId);
										    			
										    			UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());
				
										    			userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
										    			
										    			CapvChatClientMessagesDeviceSyncHandler.syncChatMessageToSenderDevices(jsonMessageObj, 
																																userSession.getUserName(), 
																																session.getId());
										    			
										    			String service = CapvClientUserUtil
																				.getClientConfigProperty(userSession.getClientId(), 
																						CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
										    			
										    			List<Item> occupants = capvChatUserRequestProcessor.getOccupantsByRoom(room + "@conference." + service);
										    			
										    			JsonObject messageToSend = new JsonObject();
										        		
										        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
										        									CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_RECEIVE);
										        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, userName);
										        		messageToSend.addProperty("room", room);
										        		messageToSend.addProperty("stamp", new Date().toString());
										        		messageToSend.addProperty("messageId", msgId);
										        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, msg);
										        		
														for(Item item : occupants){
															String occupant = item.getEntityID().toString().split("/")[1];
															
															if(!occupant.equals(userName)) {
																com.capv.um.model.User friend = userService.getByUserName(occupant, false);
																
																if(friend != null && friend.getLastSigninOs() != null && 
																		friend.getLastSigninOs().equals("ios")){
																	apnsService.pushCallNotification(friend.getTokenId(), messageToSend.toString(),friend.getClientId());
																}else if(friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("android")){
																	fcmService.sendMessage(friend.getTokenId(), messageToSend,friend.getClientId());
																}
															}
														}
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_MESSAGE_EDIT:{
	    						String room = jsonMessageObj.get("room").getAsString();
	    						String msg = jsonMessageObj.get("message").getAsString();
	    						String msgId = jsonMessageObj.get("messageId").getAsString();
	    						String type = jsonMessageObj.get("type").getAsString();
	    						
	    						if(type.equals("group")) {
	    							if(room!=null&&msg!=null&&msgId!=null) {
	    								CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
	    								OfGroupArchive groupArchiveMessage=ofGroupArchiveService.fetchById(Long.parseLong(msgId));
	    								if(groupArchiveMessage!=null) {
	    									groupArchiveMessage.setBody(msg);
	    									groupArchiveMessage.setIsEdited(1);
	    									groupArchiveMessage.setIsDeleted(0);
	    									ofGroupArchiveService.update(groupArchiveMessage);
	    									JsonObject messageToUser = new JsonObject();
	    									messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_EDIT);
	    									messageToUser.addProperty("status", "updated successfully");
	    									messageToUser.addProperty("messageId", msgId);
	    									messageToUser.addProperty("body", msg);
	    									messageToUser.addProperty("room", room);
	    									messageToUser.addProperty("senderName", userName);
	    									messageToUser.addProperty("type", type);
	    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());
	    									userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);

	    									String service = CapvClientUserUtil.getClientConfigProperty(userSession.getClientId(), CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
						    			
	    									List<Item> occupants = capvChatUserRequestProcessor.getOccupantsByRoom(room + "@conference." + service);
						    			
	    									for(Item item : occupants){
											String occupant = item.getEntityID().toString().split("/")[1];
											List<UserSession> toUserSessions = UserRegistry.getUserSessionsByUserName(occupant);
						    						if(toUserSessions != null) {
						    							for(UserSession toUserSession :toUserSessions) {
						    								UserWebSocketMessage remoteWebSocketMessage = new UserWebSocketMessage(toUserSession, messageToUser.toString());
						    								toUserSession.getCapvUserWebSocketMessageProcessor().processMessage(remoteWebSocketMessage);
						    							}
						    						}
						    						if(!occupant.equals(userName)) {
														com.capv.um.model.User friend = userService.getByUserName(occupant, false);
														
														if(friend != null && friend.getLastSigninOs() != null && 
																friend.getLastSigninOs().equals("ios")){
															apnsService.pushCallNotification(friend.getTokenId(), messageToUser.toString(),friend.getClientId());
														}else if(friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("android")){
															fcmService.sendMessage(friend.getTokenId(), messageToUser,friend.getClientId());
														}
													}
										}
	    							}
	    						}else {
	    							JsonObject messageToUser = new JsonObject();
					    			messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_EDIT);
					    			messageToUser.addProperty("Invalid", msgId);
					    			UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());

					    			userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    						}
	    					}else if(type.equals("one-one")) {
	    							if(room!=null&&msgId!=null) {
	    								OfGroupArchive groupArchiveMessage=ofGroupArchiveService.fetchById(Long.parseLong(msgId));
	    								if(groupArchiveMessage!=null) {
	    									groupArchiveMessage.setBody(msg);
	    									groupArchiveMessage.setIsEdited(1);
	    									groupArchiveMessage.setIsDeleted(0);
	    									ofGroupArchiveService.update(groupArchiveMessage);
	    									JsonObject messageToUser = new JsonObject();
	    									messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_EDIT);
	    									messageToUser.addProperty("status", "updated successfully");
	    									messageToUser.addProperty("messageId", msgId);
	    									messageToUser.addProperty("body", msg);
	    									messageToUser.addProperty("room", room);
	    									messageToUser.addProperty("senderName", userName);
	    									messageToUser.addProperty("type", type);
	    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());
	    									userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    									
	    									List<UserSession> toUserSessions = UserRegistry.getUserSessionsByUserName(room);
	    	    								if(toUserSessions != null) {
	    	    									for(UserSession toUserSession :toUserSessions) {
	    	    									UserWebSocketMessage remoteWebSocketMessage = new UserWebSocketMessage(toUserSession, messageToUser.toString());
	    	    									toUserSession.getCapvUserWebSocketMessageProcessor().processMessage(remoteWebSocketMessage);
	    	    									}
	    	    								}
	    	    							if(!room.equals(userName)) {
	    									com.capv.um.model.User friend = userService.getByUserName(room, false);
	    									if(friend != null && friend.getLastSigninOs() != null && 
	    											friend.getLastSigninOs().equals("ios")){
	    										apnsService.pushCallNotification(friend.getTokenId(), messageToUser.toString(),friend.getClientId());
	    									}else if(friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("android")){
	    										fcmService.sendMessage(friend.getTokenId(), messageToUser,friend.getClientId());
	    									}
	    								 }
	    								}
	    							}else {
	    								JsonObject messageToUser = new JsonObject();
	    								messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_EDIT);
	    			    					messageToUser.addProperty("Invalid", msgId);
	    			    					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());

	    			    					userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    							}
	    						}
	    			}
	    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_MESSAGE_DELETE:{
						String room = jsonMessageObj.get("room").getAsString();
						String msgId = jsonMessageObj.get("messageId").getAsString();
						String type = jsonMessageObj.get("type").getAsString();
						if(type.equals("group")) {
							if(room!=null&&msgId!=null) {
								CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
								OfGroupArchive groupArchiveMessage=ofGroupArchiveService.fetchById(Long.parseLong(msgId));
								if(groupArchiveMessage!=null) {
									groupArchiveMessage.setIsDeleted(1);
									ofGroupArchiveService.update(groupArchiveMessage);
									JsonObject messageToUser = new JsonObject();
									messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_DELETE);
									messageToUser.addProperty("status", "Deleted successfully");
									messageToUser.addProperty("messageId", msgId);
									messageToUser.addProperty("room", room);
									messageToUser.addProperty("senderName", userName);
									messageToUser.addProperty("type", type);
									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());
									userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									
									String service = CapvClientUserUtil.getClientConfigProperty(userSession.getClientId(), CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
									List<Item> occupants = capvChatUserRequestProcessor.getOccupantsByRoom(room + "@conference." + service);
				    			
									for(Item item : occupants){
										String occupant = item.getEntityID().toString().split("/")[1];
										List<UserSession> toUserSessions = UserRegistry.getUserSessionsByUserName(occupant);
				    							if(toUserSessions != null) {
				    								for(UserSession toUserSession :toUserSessions) {
				    									UserWebSocketMessage remoteWebSocketMessage = new UserWebSocketMessage(toUserSession, messageToUser.toString());
				    									toUserSession.getCapvUserWebSocketMessageProcessor().processMessage(remoteWebSocketMessage);
				    								}
				    							}
				    							if(!occupant.equals(userName)) {
												com.capv.um.model.User friend = userService.getByUserName(occupant, false);
												
												if(friend != null && friend.getLastSigninOs() != null && 
														friend.getLastSigninOs().equals("ios")){
													apnsService.pushCallNotification(friend.getTokenId(), messageToUser.toString(),friend.getClientId());
												}else if(friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("android")){
													fcmService.sendMessage(friend.getTokenId(), messageToUser,friend.getClientId());
												}
											}
									}
								}
							}else {
								JsonObject messageToUser = new JsonObject();
								messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_DELETE);
			    				messageToUser.addProperty("Invalid", msgId);
			    				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());

			    				userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
							}
						}else if(type.equals("one-one")) {
							if(room!=null&&msgId!=null) {
								OfGroupArchive groupArchiveMessage=ofGroupArchiveService.fetchById(Long.parseLong(msgId));
								if(groupArchiveMessage!=null) {
									groupArchiveMessage.setIsDeleted(1);
									ofGroupArchiveService.update(groupArchiveMessage);
									JsonObject messageToUser = new JsonObject();
									messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_DELETE);
									messageToUser.addProperty("status", "Deleted successfully");
									messageToUser.addProperty("messageId", msgId);
									messageToUser.addProperty("room", room);
									messageToUser.addProperty("senderName", userName);
									messageToUser.addProperty("type", type);
									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());
									userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									
									List<UserSession> toUserSessions = UserRegistry.getUserSessionsByUserName(room);
	    								if(toUserSessions != null) {
	    									for(UserSession toUserSession :toUserSessions) {
	    									UserWebSocketMessage remoteWebSocketMessage = new UserWebSocketMessage(toUserSession, messageToUser.toString());
	    									toUserSession.getCapvUserWebSocketMessageProcessor().processMessage(remoteWebSocketMessage);
	    									}
	    								}
	    							if(!room.equals(userName)) {
									com.capv.um.model.User friend = userService.getByUserName(room, false);
									if(friend != null && friend.getLastSigninOs() != null && 
											friend.getLastSigninOs().equals("ios")){
										apnsService.pushCallNotification(friend.getTokenId(), messageToUser.toString(),friend.getClientId());
									}else if(friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("android")){
										fcmService.sendMessage(friend.getTokenId(), messageToUser, friend.getClientId());
									}
								}
								}
							}else {
								JsonObject messageToUser = new JsonObject();
								messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_DELETE);
			    					messageToUser.addProperty("Invalid", msgId);
			    					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());

			    					userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
							}
						}
			}
	    		break;
	    			case CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEARCH:{
						String room = jsonMessageObj.get("room").getAsString();
						String searchParam = jsonMessageObj.get("searchParam").getAsString();
						String type = jsonMessageObj.get("type").getAsString();
						String service = CapvClientUserUtil.getClientConfigProperty(userSession.getClientId(), CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
						if(type.equals("group")&&searchParam.length()>=2&&room!=null) {
							List<OfGroupArchive> searchResult=ofGroupArchiveService.getGroupSearch(room+"@conference."+service, searchParam);
							Collections.sort(searchResult, new CustomGroupComparator());
							
							String chatHistory = CapvClientUserUtil.getGroupChatHistoryJSON(room, searchResult,1000);
							UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																			(userSession, chatHistory);
							
							userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
						}else if(type.equals("one-one")&&searchParam.length()>=2&&room!=null) {
							List<OfGroupArchive> searchResult=ofGroupArchiveService.getOneOneSearch(room+"@"+service, userName+"@"+service, searchParam);
							Collections.sort(searchResult, new CustomGroupComparator());
							
							String chatHistory = CapvClientUserUtil.getOneToOneChatHistoryJSON(userSession.getClientId(), userName, room, searchResult, 1000);
							UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																			(userSession, chatHistory);
							
							userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
							
						}else {
							JsonObject messageToUser = new JsonObject();
							messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEARCH);
		    					messageToUser.addProperty("Invalid", "Invalid Params");
		    					
		    					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());

		    					userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
						}
			}
			break;
	    			case CapvClientUserConstants.WS_MESSAGE_GROUP_JOIN_ROOM:
									    			{
                                                        String toJID = jsonMessageObj.get("room").getAsString();
                                                        String historyEndDate = jsonMessageObj.get("fromDate") != null ? 
																jsonMessageObj.get("fromDate").getAsString() : null;
														Long clientId = userSession.getClientId();
														
														
														String noOfRecordsConfig = CapvClientUserUtil.getClientConfigProperty(clientId, 
																											CapvClientUserConstants.CHAT_HISTORY_MAX_RECORDS);
														int noOfRecords = 25;
														
														if(noOfRecordsConfig != null) {
															try {
																noOfRecords = Integer.parseInt(noOfRecordsConfig);
															} catch (NumberFormatException nfe) {}
														}
														
														String historyStartDate = "";
														try{
															if(historyEndDate == null)
																historyEndDate = Long.toString(System.currentTimeMillis());
															else
																//subtract 1 millisecond from last fetched time to get the previous records from last fetched date
																historyEndDate = Long.toString((Long.parseLong(historyEndDate) - 1));
															
															int maxHistoryDays = 60;
															String maxHistoryConfig = CapvClientUserUtil.getClientConfigProperty(clientId, 
																												CapvClientUserConstants.CHAT_MAX_HISTORY);
															
															if(maxHistoryConfig != null) {
																try {
																	maxHistoryDays = Integer.parseInt(maxHistoryConfig);
																} catch (NumberFormatException nfe){}
															}
															
															Calendar calendar = Calendar.getInstance();
															calendar.add(Calendar.DATE, -maxHistoryDays);
															
															historyStartDate = Long.toString(calendar.getTime().getTime());
															
															String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
															
															List<OfGroupArchive> allChatHistory = new ArrayList<OfGroupArchive>();
															allChatHistory = ofGroupArchiveService.getArchiveGroupHistory(toJID+"@conference."+chatServerServiceName,
																																historyStartDate, historyEndDate,noOfRecords);
															
															Collections.sort(allChatHistory, new CustomGroupComparator());
															
															String chatHistory = CapvClientUserUtil.getGroupChatHistoryJSON(toJID, allChatHistory, noOfRecords);
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																											(userSession, chatHistory);
															
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
										    				
										    				/*String room = jsonMessageObj.get("room").getAsString();
										    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																							getCapvChatUserRequestProcessor(userSession);
										    				capvChatUserRequestProcessor.joinRoom(room);*/
														
									    			}catch(Exception e){
														JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty("status", "Fail");
			                                        	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.historyFetchError"));
			                                        	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession,  messageToSend.toString());
														userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
													}
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_FILE_SHARE:
									    			{
									    				
									    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																					getCapvChatUserRequestProcessor(userSession);
									    				
									    				if(jsonMessageObj.get("type") != null && jsonMessageObj.get("type").getAsString().equals("group")) {
									    					CapvClientUserUtil.shareFileToGroup(jsonMessageObj, userName);
									    				} else
									    					capvChatUserRequestProcessor.sendFile(jsonMessageObj);
									    			}
													break;
	    			case CapvClientUserConstants.WS_CHAT_ONE_HISTORY:
													{
														String toJID = jsonMessageObj.get("toJID").getAsString();
														String fromJID = jsonMessageObj.get("fromJID").getAsString();
														String historyEndDate = jsonMessageObj.get("fromDate") != null ? 
																					jsonMessageObj.get("fromDate").getAsString() : null;
														Long clientId = userSession.getClientId();
														
														
														String noOfRecordsConfig = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_HISTORY_MAX_RECORDS);
														int noOfRecords = 25;
														
														if(noOfRecordsConfig != null) {
															try {
																noOfRecords = Integer.parseInt(noOfRecordsConfig);
															} catch (NumberFormatException nfe) {}
														}
														
														String historyStartDate = "";
														try{
															if(historyEndDate == null)
																historyEndDate = Long.toString(System.currentTimeMillis());
															else
																historyEndDate = Long.toString((Long.parseLong(historyEndDate) - 1));
															
															int maxHistoryDays = 60;
															String maxHistoryConfig = CapvClientUserUtil.getClientConfigProperty(clientId, 
																												CapvClientUserConstants.CHAT_MAX_HISTORY);
															
															if(maxHistoryConfig != null) {
																try {
																	maxHistoryDays = Integer.parseInt(maxHistoryConfig);
																} catch (NumberFormatException nfe){}
														     }
															
															if(jsonMessageObj.get("chatHistoryMaxDays") != null){
																    String maxHistoryDaysString = jsonMessageObj.get("chatHistoryMaxDays").getAsString();
																  	try {
										                				  maxHistoryDays = Integer.parseInt(maxHistoryDaysString);
										                				  
										                				  if(maxHistoryDays < 0)
										                					  maxHistoryDays = 0;
										                			} catch (NumberFormatException nfe){}
																
															}
															
															Calendar calendar = Calendar.getInstance();
															calendar.add(Calendar.DATE, -maxHistoryDays);
															
															historyStartDate = Long.toString(calendar.getTime().getTime());
															
															String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
					//List<OfMessageArchive> chatHistoryList = ofMessageArchiveService.getArchiveHistory(toJID+"@"+chatServerServiceName, fromJID+"@"+chatServerServiceName,historyStartDate, historyEndDate, noOfRecords);
															List<OfGroupArchive> allChatHistory = new ArrayList<OfGroupArchive>();
															allChatHistory = ofGroupArchiveService.getOneOneHistory(toJID+"@"+chatServerServiceName, fromJID+"@"+chatServerServiceName,
																																	historyStartDate, historyEndDate, noOfRecords);
												
															Collections.sort(allChatHistory, new CustomGroupComparator());
														
															String chatHistory = CapvClientUserUtil.getOneToOneChatHistoryJSON(clientId, fromJID, toJID, 
																										allChatHistory, noOfRecords);
														
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(userSession, chatHistory);
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														
															
														}catch(Exception e){
															JsonObject messageToSend = new JsonObject();
															messageToSend.addProperty("status", "Fail");
				                                        	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.historyFetchError"));
				                                        	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession,  messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														}
														break;	
												 }
	    			case CapvClientUserConstants.WS_MESSAGE_GET_CHAT_LOG:
									    			{
									    				
                                                        Long clientId = userSession.getClientId();
                                                        String userJID = jsonMessageObj.get("userJID").getAsString();
                                                        String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
                                                        CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
                                                        List<OfMessageArchive> chatHistory = null;
                                                		List<OfMessageArchive> chatHistoryAll = new ArrayList<>();
                                                		List<String> justRooms = new ArrayList<String>();
                                                		
                                                		StringBuilder joinedRoomsString = new StringBuilder();
                                                		
                                                			List<UserChatRoom> joinedRooms = capvChatUserRequestProcessor.joinedRooms(clientId);
                                                			for(UserChatRoom chatRoom : joinedRooms){
                                                				justRooms.add("'" + chatRoom.getJid()+ "'");
                                                    		}
                                                    		for(String room: justRooms){
                                                    			if(joinedRoomsString.length() != 0 )
                                                    				joinedRoomsString.append(",").append(room);
                                                    			else
                                                    				joinedRoomsString.append(room);
                                                    		}
                                                			chatHistory = ofMessageArchiveService.getArchiveHistoryLastMessage(userJID+"@"+chatServerServiceName,joinedRoomsString.toString());
                                                			
                                                			chatHistoryAll = chatHistory;
                                                			for (int i = 0; i < chatHistory.size(); i++) {
                                                				for(int j = 0; j < chatHistory.size(); j++){
                                                					if(chatHistory.get(i).getToJID().equals(chatHistory.get(j).getToJID()) && i!=j){
                                                						if(chatHistory.get(i).getSentDate() > chatHistory.get(j).getSentDate())
                                                				    		chatHistoryAll.add(chatHistory.get(i));
                                                				    	else
                                                				    		chatHistoryAll.add(chatHistory.get(j));
                                                						
                                                						chatHistoryAll.remove(j);j--;
                                                						chatHistoryAll.remove(i);i--;
                                                					}
                                                					if(chatHistory.get(i).getToJID().equals(chatHistory.get(j).getFromJID()) && i!=j
                                                							&& chatHistory.get(j).getToJID().equals(chatHistory.get(i).getFromJID())){
                                                						if(chatHistory.get(i).getSentDate() > chatHistory.get(j).getSentDate())
                                                				    		chatHistoryAll.add(chatHistory.get(i));
                                                				    	else
                                                				    		chatHistoryAll.add(chatHistory.get(j));
                                                						
                                                						chatHistoryAll.remove(j);j--;
                                                						chatHistoryAll.remove(i);i--;
                                                					}
                                                				}
                                                			}	
                                                			Collections.sort(chatHistoryAll, new CustomComparator());
														String chatHistory2 = CapvClientUserUtil.getChatLog(clientId,userJID,capvChatUserRequestProcessor,chatHistoryAll);
														UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(userSession, chatHistory2);
														userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									    			}
								                    break;
                    
	    			case CapvClientUserConstants.WS_MESSAGE_IS_CALL_IN_PROGRESS:
									    			{
						    							List<UserCallState> userCalls = callStatesService.callStatesInprogress(
																userName,  userSession.getClientId());
						    							JsonObject jsonObject = new JsonObject();
														jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																				CapvClientUserConstants.WS_MESSAGE_CALL_PROGRESS);
														Type objType = new TypeToken<List<UserCallState>>() {}.getType();
														jsonObject.add("userCalls", CapvClientUserUtil.convertToJsonElement(userCalls, objType));
														
														UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(userSession, 
																										CapvClientUserUtil
																											.convertToJsonString(jsonObject));
														userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_CALL_LOG_EVENTS:
									    			{
									    				int resultOffset = 0;
									    				int maxResults = 20;
									    				String fetchType = "call_log";
									    				
									    				if(jsonMessageObj.get("resultOffset") != null) {
									    					try {
									    						resultOffset = Integer.parseInt(jsonMessageObj.get("resultOffset").getAsString());
									    					} catch (Exception e){}
									    				}
									    				if(jsonMessageObj.get("maxResults") != null) {
									    					try {
									    						maxResults = Integer.parseInt(jsonMessageObj.get("maxResults").getAsString());
									    					} catch (Exception e){}
									    				}
									    				if(jsonMessageObj.get("fetchType") != null) {
								    						fetchType = jsonMessageObj.get("fetchType").getAsString();
									    				}
									    				
														List<UserCallState> userCalls = callStatesService.userCallLog(userName, resultOffset, maxResults, fetchType);
														JsonObject jsonObject = new JsonObject();
														jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																				CapvClientUserConstants.WS_MESSAGE_CALL_EVENTS);
														Type objType = new TypeToken<List<UserCallState>>() {}.getType();
														jsonObject.add("userCalls", CapvClientUserUtil.convertToJsonElement(userCalls, objType));
														jsonObject.addProperty("fetchType", fetchType);
														UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(userSession, 
																										CapvClientUserUtil
																											.convertToJsonString(jsonObject));
														userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									    			}
									    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_FETCH_VIDEO_RECORDED_EVENTS:
	    			{
	    				int resultOffset = 0;
	    				int maxResults = 20;
	    				String fetchType = "call_log";
	    				
	    				if(jsonMessageObj.get("resultOffset") != null) {
	    					try {
	    						resultOffset = Integer.parseInt(jsonMessageObj.get("resultOffset").getAsString());
	    					} catch (Exception e){}
	    				}
	    				if(jsonMessageObj.get("maxResults") != null) {
	    					try {
	    						maxResults = Integer.parseInt(jsonMessageObj.get("maxResults").getAsString());
	    					} catch (Exception e){}
	    				}
	    				if(jsonMessageObj.get("fetchType") != null) {
    						fetchType = jsonMessageObj.get("fetchType").getAsString();
	    				}
	    				
						List<UserCallState> userCalls = callStatesService.userRecodedVideoLog(userName, resultOffset, maxResults, fetchType);
						JsonObject jsonObject = new JsonObject();
						jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
												CapvClientUserConstants.WS_MESSAGE_CALL_EVENTS);
						Type objType = new TypeToken<List<UserCallState>>() {}.getType();
						jsonObject.add("userCalls", CapvClientUserUtil.convertToJsonElement(userCalls, objType));
						jsonObject.addProperty("fetchType", fetchType);
						UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																		(userSession, 
																		CapvClientUserUtil
																			.convertToJsonString(jsonObject));
						userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    			}
	    			break;
	    			case CapvClientUserConstants.WS_MESSAGE_RECORD_START: 
													{
														JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_VIDEO_RECORD_STATUS);
														messageToSend.addProperty("action", "start");
														Map<String, Object> callData = userSession.getCallData();
														try
														{
					                                        
					                                        if(callData != null && 
					                                        		callData.get("roomNumber") != null &&
					                                        		callData.get("videoFullRecordId") != null) {
					                                        	
					                                        	if(callData.get("videoRecordingCurrentId") == null && 
					                                        			callData.get("videoRecordingRequestInProgress") == null) {
					                                        		callData.put("videoRecordingRequestInProgress", true);
					                                        		String roomNubmer = (String)callData.get("roomNumber");
							                                        Long videoFullRecordId = (Long)callData.get("videoFullRecordId");
																	
																	VideoRecording videoRecordingModel = new VideoRecording();
								    			            		 
								    			            		videoRecordingModel.setUserName(userSession.getUserName());
								    			            		videoRecordingModel.setRoomId(roomNubmer);
								    			            		videoRecordingModel.setClientId(userSession.getClientId().intValue());
								    			            		videoRecordingModel.setFullVideo(false);
								    			            		videoRecordingModel.setScheduler(false);
								    			            		videoRecordingModel.setSourcePath("/tmp/");
								    			            		videoRecordingModel.setStarttime(new Date());
								    			            		videoRecordingModel.setVideoRecId(videoFullRecordId);
								    			            		 
								    			            		videoRecordingService.save(videoRecordingModel);
								    			            		
								    			            		callData.put("videoRecordingCurrentId", videoRecordingModel.getId());
								    			            		
																	messageToSend.addProperty("status", VideoRecordingStatus.SUCCESS.getStatus());
					                                        	} else {
					                                        		messageToSend.addProperty("status", VideoRecordingStatus.FAIL.getStatus());
						                                        	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.videoRecordRequestProgress"));
					                                        	}
					                                        	
					                                        } else {
					                                        	messageToSend.addProperty("status", VideoRecordingStatus.FAIL.getStatus());
					                                        	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.noActiveVideoCall"));
					                                        }
					                                        
														}catch(Exception e){
															messageToSend.addProperty("status", VideoRecordingStatus.FAIL.getStatus());
				                                        	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.videoRecordInitializationError"));
														} finally {
															callData.remove("videoRecordingRequestInProgress");
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
            			 																						(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														}
														
													}
													break;
	    			case CapvClientUserConstants.WS_MESSAGE_RECORD_STOP: 
														{
															JsonObject messageToSend = new JsonObject();
															messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																						CapvClientUserConstants.WS_MESSAGE_VIDEO_RECORD_STATUS);
															messageToSend.addProperty("action", "stop");
															try
															{
																Map<String, Object> callData = userSession.getCallData();
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
						                                        	
						                                        	messageToSend.addProperty("status", VideoRecordingStatus.SUCCESS.getStatus());
						                                        } else {
						                                        	messageToSend.addProperty("status", VideoRecordingStatus.FAIL.getStatus());
						                                        	messageToSend.addProperty("message", "No active video recording");
						                                        }
															} catch(Exception e){
																messageToSend.addProperty("status", VideoRecordingStatus.FAIL.getStatus());
					                                        	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.videoRecordStoppedError"));
															} finally {
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															}
														}
														break;
														
	    			case CapvClientUserConstants.WS_MESSAGE_GET_UNKNOWN_USER_FULLNAME:
									    			{
									    				if(jsonMessageObj.get("requestedUser") != null) {
									    					String requestedUserName = jsonMessageObj.get("requestedUser").getAsString();
										    				
										    				if(CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName) != null) {
										    					
										    					String userFullName = ChatUserUtil.getUserFullName(requestedUserName);
										    					
										    					if(userFullName != null) {
										    						JsonObject messageToSend = new JsonObject();
																	messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																								CapvClientUserConstants.WS_MESSAGE_UNKNOWN_USER_FULLNAME);
																	messageToSend.addProperty("userName", requestedUserName);
																	messageToSend.addProperty("userFullName", userFullName);
																	
																	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																														(userSession, messageToSend.toString());
																	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
										    					}
										    				}
									    				}
									    				
									    			}
									    			break;
									    			
	    											case CapvClientUserConstants.WS_MESSAGE_GET_USERS_FULL_DETAILS:
									    			{
									    				List<com.capv.um.model.User> userDetails = new ArrayList<>();
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_USERS_FULL_DETAILS);
														
														try {
															
															if(jsonMessageObj.get("userNames") != null) {
										    					JsonArray userNamesArrayObj = jsonMessageObj.get("userNames").getAsJsonArray();
										    					
										    					Iterator<JsonElement> userNamesObjItr = userNamesArrayObj.iterator();
										    					List<String> userNames = new ArrayList<>();
										    					
										    					while(userNamesObjItr.hasNext()) {
										    						JsonElement userNameObj = userNamesObjItr.next();
										    						userNames.add(userNameObj.getAsString());
										    					}
										    					
										    					if(userNames.size() > 0)
										    						userDetails = userService.getUsersByUserNames(userNames);
										    				}
															
														} finally {
															ObjectMapper mapper = new ObjectMapper();
															mapper.registerModule(new Hibernate4Module());
															String userDetailsString = mapper.writeValueAsString(userDetails);
															//Type objType = new TypeToken<List<com.capv.um.model.User>>() {}.getType();
															messageToSend.add("userDetails", CapvClientUserUtil.convertToJsonArray(userDetailsString));
															
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														}
														
									    				
									    			}
									    			break;
									    			
	    											case CapvClientUserConstants.WS_MESSAGE_UPDATE_USER_SETTINGS:
	    											{
	    												JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_UPDATE_USER_SETTINGS_RESPONSE);
														messageToSend.addProperty("status", "success");
														
	    												if(jsonMessageObj.get("userSettings") != null) {
	    													try {
	    														JsonArray userSettingsJson = jsonMessageObj.get("userSettings").getAsJsonArray();
	    														Type userConfigType = new TypeToken<List<UserConfig>>(){}.getType();
																List<UserConfig> userConfigList = CapvClientUserUtil.convertJsonStringToJavaObject(
																																userSettingsJson.toString(), 
																																userConfigType);
																userConfigService.updateUserConfig(userConfigList);
																CapvChatClientManager capvChatClientManager = CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName);
																ChatUserUtil.checkAndUpdateUserVCardPrivacySettings(capvChatClientManager.getChatUserConnection(), userConfigList);
	    													} catch (Exception e){
	    														messageToSend.addProperty("status", "fail");
	    													}
															
	    													try {
	    														List<UserConfig> userConfig = 
																		userConfigService.getUserConfigDetailsByUserId(userSession.getUserId());
	    														
																Type objType = new TypeToken<List<UserConfig>>() {}.getType();
																messageToSend.add("userSettings", 
																					CapvClientUserUtil.convertToJsonElement(userConfig, objType));
	    													} catch (Exception e){}
															
															List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
															
															for(UserSession userSessionObj :userSessions) {
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSessionObj, messageToSend.toString());
																userSessionObj.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															}
	    												} else {
	    													messageToSend.addProperty("status", "fail");
	    													
	    													UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    												}
	    											}
	    											break;
	    											
									    			case CapvClientUserConstants.WS_MESSAGE_CHANGE_PASSWORD :
									    			{
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_CHANGE_PASSWORD_RESPONSE);
														messageToSend.addProperty("status", "success");
														boolean passwordResetSuccess = false;
														
														try {
															if(jsonMessageObj.get("changePasswordData") != null) {
																String changePasswordData = jsonMessageObj.get("changePasswordData").toString();
																
																Type changePasswordViewType = new TypeToken<ChangePasswordView>(){}.getType();
																
																ChangePasswordView changePasswordView = CapvClientUserUtil.
																											convertJsonStringToJavaObject(
																													changePasswordData, changePasswordViewType);
																BindingResult bindingResult = new BeanPropertyBindingResult(changePasswordView, "changePasswordView");
																
																com.capv.um.model.User user = userService.getById(userSession.getUserId());
																changePasswordView.setCurrentPassword(user.getPassword());
																
																ChangePasswordValidator changePasswordValidator = new ChangePasswordValidator();
																changePasswordValidator.validate(changePasswordView, bindingResult);
																
																if(bindingResult.hasErrors()) {
																	messageToSend.addProperty("status", "fail");
																	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.invalidData"));
																	
																	List<FieldError> fieldErrors = bindingResult.getFieldErrors();
																	Type filedErrorsType = new TypeToken<List<FieldError>>(){}.getType();
																	
																	messageToSend.add("errors", CapvClientUserUtil.convertToJsonElement(fieldErrors, filedErrorsType));
																	
																} else {
																	String newEncodedPassword = CapvUtil.encodePassword(changePasswordView.getNewPassword());
																	userService.changePassword(newEncodedPassword, userName);
																	passwordResetSuccess = true;
																}
															} else {
																messageToSend.addProperty("status", "fail");
																messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.inSufficientData"));
															}
														} catch (Exception e) {
															messageToSend.addProperty("status", "fail");
														} finally {
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															
															if(passwordResetSuccess)
																CapvUtil.removeUserOAuthTokens(userSession.getClientId(), userSession.getUserName());
														}
									    			}
													break;
													
									    			case CapvClientUserConstants.WS_MESSAGE_UPDATE_PROFILE :
									    			{
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_UPDATE_PROFILE_RESPONSE);
														messageToSend.addProperty("status", "success");
														
														try {
															if(jsonMessageObj.get("updateProfileData") != null) {
																String updateProfileData = jsonMessageObj.get("updateProfileData").toString();
																
																Type updateProfileUserViewType = new TypeToken<com.capv.um.model.User>(){}.getType();
																
																com.capv.um.model.User updateUser = CapvClientUserUtil.
																											convertJsonStringToJavaObject(
																													updateProfileData, updateProfileUserViewType);
																BindingResult bindingResult = new BeanPropertyBindingResult(updateUser, "updateUserView");
																
																UserValidator userValidator = new UserValidator(ValidationType.UPDATEUSER);
																userValidator.validate(updateUser, bindingResult);
																
																if(bindingResult.hasErrors()) {
																	messageToSend.addProperty("status", "fail");
																	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.invalidData"));
																	
																	List<FieldError> fieldErrors = bindingResult.getFieldErrors();
																	Type filedErrorsType = new TypeToken<List<FieldError>>(){}.getType();
																	
																	messageToSend.add("errors", CapvClientUserUtil.convertToJsonElement(fieldErrors, filedErrorsType));
																	
																} else {
																	com.capv.um.model.User dbUser = userService.getById(userSession.getUserId(), false);
																	
																	dbUser.setFirstName(updateUser.getFirstName());
																	dbUser.setLastName(updateUser.getLastName());
																	dbUser.setName(updateUser.getFirstName() + " " + updateUser.getLastName());
																	dbUser.setEmail(updateUser.getEmail());
																	dbUser.setMobile(updateUser.getMobile());
																	userService.update(dbUser, true);
																	
																	userCacheManager.addToUserCache(dbUser);
																	
																	JsonObject updatedUserJson = new JsonObject();
																	updatedUserJson.addProperty("firstName", dbUser.getFirstName());
																	updatedUserJson.addProperty("lastName", dbUser.getLastName());
																	updatedUserJson.addProperty("name", dbUser.getName());
																	updatedUserJson.addProperty("email", dbUser.getEmail());
																	updatedUserJson.addProperty("mobile", dbUser.getMobile());
																	updatedUserJson.addProperty("userName", dbUser.getUserName());
																	
																	messageToSend.add("updatedUser", updatedUserJson);
																}
															} else {
																messageToSend.addProperty("status", "fail");
																messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.inSufficientData"));
															}
														} catch (Exception e) {
															messageToSend.addProperty("status", "fail");
														} finally {
															List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
															
															for(UserSession userSessionObj :userSessions) {
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSessionObj, messageToSend.toString());
																userSessionObj.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															}
															
															if(messageToSend.get("updatedUser") != null) {
																
																List<User> rosterUsers = getCapvChatUserRequestProcessor(userSession).getRosterUsers();
																
																for(User rosterUser :rosterUsers) {
																	List<UserSession> rosterUserSessions = 
																						UserRegistry.getUserSessionsByUserName(rosterUser.getName());
																	if(rosterUserSessions != null && rosterUserSessions.size() > 0) {
																		for(UserSession rosterUserSession: rosterUserSessions) {
																			UserWebSocketMessage rosterUserWebSocketMessage = new UserWebSocketMessage
																															(rosterUserSession, messageToSend.toString());
																			rosterUserSession.getCapvUserWebSocketMessageProcessor()
																								.processMessage(rosterUserWebSocketMessage);
																		}
																	}
																}
															}
														}
									    			}
													break;
													
									    			case CapvClientUserConstants.WS_MESSAGE_GET_USER_DETAILS :
									    			{
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_GET_USER_DETAILS_RESPONSE);
														messageToSend.addProperty("status", "success");
														
														try {
															com.capv.um.model.User user = userService.getById(userSession.getUserId(), true);
															
															JsonObject userDetailsJson = new JsonObject();
															
															userDetailsJson.addProperty("firstName", user.getFirstName());
															userDetailsJson.addProperty("lastName", user.getLastName());
															userDetailsJson.addProperty("name", user.getName());
															userDetailsJson.addProperty("email", user.getEmail());
															userDetailsJson.addProperty("mobile", user.getMobile());
															userDetailsJson.addProperty("userName", user.getUserName());
															userDetailsJson.addProperty("userId", user.getId());
															userDetailsJson.addProperty("clientId", user.getClientId());
															
															boolean videoPlaybackGridSupport = false;
															String videoPlaybackGridSupportString = CapvClientUserUtil.getConfigProperty("recordedvideoplayback.gridsupport");
															
															if(videoPlaybackGridSupportString != null) {
																try {
																	videoPlaybackGridSupport = Boolean.parseBoolean(videoPlaybackGridSupportString);
																} catch (Exception e){}
															}
															userDetailsJson.addProperty("videoPlaybackGridSupport", videoPlaybackGridSupport);
															
															List<UserConfig> userSettings = user.getUserConfig();
															Type userSettingsType = new TypeToken<List<UserConfig>>(){}.getType();
															userDetailsJson.add("userConfig", 
																					CapvClientUserUtil.convertToJsonElement(userSettings, userSettingsType));
															
															messageToSend.add("userDetails", userDetailsJson);
														} catch (Exception e) {
															messageToSend.addProperty("status", "fail");
														} finally {
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														}
														
									    			}
									    			break;
														
									    			case CapvClientUserConstants.WS_MESSAGE_UPLOAD_PROFILE_PICTURE :
									    			{
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_UPLOAD_PROFILE_PICTURE_RESPONSE);
														messageToSend.addProperty("status", "success");
														
														CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
														
														try {
															if(jsonMessageObj.get("profilePictureData") != null) {
																String profilePictureData = jsonMessageObj.get("profilePictureData").getAsString();
																
																capvChatUserRequestProcessor.uploadUserProfilePicture(profilePictureData);
															} else {
																messageToSend.addProperty("status", "fail");
																messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.invalidRequest"));
															}
														} catch (Exception e) {
															messageToSend.addProperty("status", "fail");
														} finally {
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															
															List<User> rosterUsers = capvChatUserRequestProcessor.getRosterUsers();
															
															messageToSend = new JsonObject();
															messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																						CapvClientUserConstants.WS_MESSAGE_GET_PROFILE_PICTURE_RESPONSE);
															messageToSend.addProperty("status", "success");
															messageToSend.addProperty("profilePicView", "UserFriendsView");
															messageToSend.addProperty("userName", userName);
															String profilePictureData = jsonMessageObj.get("profilePictureData").getAsString();
															
															Map<String, String> updatedProfilePicData = new HashMap<>();
															updatedProfilePicData.put("profilePic", profilePictureData);
															updatedProfilePicData.put("profilePicType", "image/jpeg");
															
															Type profilePictureType = new TypeToken<Map<String, String>>(){}.getType();
															
															messageToSend.add("profilePictureData", 
																				CapvClientUserUtil.convertToJsonElement(updatedProfilePicData, profilePictureType));
															
															List<UserSession> userAllSessions = UserRegistry.getUserSessionsByUserName(userName);
															
															for(UserSession currentUserSession :userAllSessions) {
																if(!currentUserSession.getWebSocketSession().getId().equals(session.getId())) {
																	UserWebSocketMessage rosterUserWebSocketMessage = new UserWebSocketMessage
																										(currentUserSession, messageToSend.toString());
																	currentUserSession.getCapvUserWebSocketMessageProcessor()
																										.processMessage(rosterUserWebSocketMessage);
																}
															}
															
															for(User rosterUser :rosterUsers) {
																List<UserSession> rosterUserSessions = 
																					UserRegistry.getUserSessionsByUserName(rosterUser.getName());
																if(rosterUserSessions != null && rosterUserSessions.size() > 0) {
																	for(UserSession rosterUserSession: rosterUserSessions) {
																		UserWebSocketMessage rosterUserWebSocketMessage = new UserWebSocketMessage
																														(rosterUserSession, messageToSend.toString());
																		rosterUserSession.getCapvUserWebSocketMessageProcessor()
																							.processMessage(rosterUserWebSocketMessage);
																	}
																}
															}
															
														}
									    			}
													break;
													
									    			case CapvClientUserConstants.WS_MESSAGE_GET_PROFILE_PICTURE :
									    			{
									    				if(jsonMessageObj.get("userName") != null) {
															
															try {
																String profilePicUserName = jsonMessageObj.get("userName").getAsString();
																String profilePicView = null;
																Map<String, String> profilePictureData = null;
																if(jsonMessageObj.get("profilePicView") != null)
																	profilePicView = jsonMessageObj.get("profilePicView").getAsString();
																
																profilePictureData = 
																		getCapvChatUserRequestProcessor(userSession).getUserProfilePicture(profilePicUserName);
																
																if(profilePictureData != null && profilePictureData.size() > 0) {
																	JsonObject messageToSend = new JsonObject();
																	messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																								CapvClientUserConstants.WS_MESSAGE_GET_PROFILE_PICTURE_RESPONSE);
																	messageToSend.addProperty("status", "success");
																	messageToSend.addProperty("userName", profilePicUserName);
																	if(profilePicView != null)
																		messageToSend.addProperty("profilePicView", profilePicView);
																	
																	Type profilePictureType = new TypeToken<Map<String, String>>(){}.getType();
																	
																	messageToSend.add("profilePictureData", 
																						CapvClientUserUtil.convertToJsonElement(profilePictureData, profilePictureType));
																	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																													(userSession, messageToSend.toString());
																	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
																}
																
															} catch (Exception e) {
																e.printStackTrace();
															}
									    				}
									    			}
													break;
													
									    			case CapvClientUserConstants.WS_MESSAGE_MISSED_CALL :
									    			{
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_MISSED_CALL_RESPONSE);
														messageToSend.addProperty("status", "success");
														
														CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
														
														try {
															if(jsonMessageObj.get("roomid") != null) {
																String roomId = jsonMessageObj.get("roomid").getAsString();
																UserCallState userCallState =callStatesService.callStateByRoom(roomId);
																if(userCallState!=null){
																	userCallState.setCallStatus(CallState.MISSED.getStateId());
																	callStatesService.update(userCallState);
																}else{
																	messageToSend.addProperty("status", "fail");
																	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.invalidRequest"));
																}
															} else {
																messageToSend.addProperty("status", "fail");
																messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.invalidRequest"));
															}
														} catch (Exception e) {
															messageToSend.addProperty("status", "fail");
														} finally {
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															
															/*List<UserSession> userAllSessions = UserRegistry.getUserSessionsByUserName(userName);
															for(UserSession currentUserSession :userAllSessions) {
																if(!currentUserSession.getWebSocketSession().getId().equals(session.getId())) {
																	UserWebSocketMessage rosterUserWebSocketMessage = new UserWebSocketMessage
																										(currentUserSession, messageToSend.toString());
																	currentUserSession.getCapvUserWebSocketMessageProcessor()
																										.processMessage(rosterUserWebSocketMessage);
																}
															}*/
															
															
														}
									    			}
													break;
													
									    			case CapvClientUserConstants.WS_MESSAGE_MISSED_CALL_SEEN :
									    			{
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_MISSED_CALL_SEEN_RESPONSE);
														messageToSend.addProperty("status", "success");
														
														CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
														
														try {
															if(jsonMessageObj.get("roomids") != null) {
																JsonArray roomIds = jsonMessageObj.get("roomids").getAsJsonArray();
																for(JsonElement roomid: roomIds ) {
																	UserCallState userCallState =callStatesService.callStateByRoom(roomid.getAsString());
																	if(userCallState!=null ){
																		userCallState.setCallStatus(CallState.MISSED_CALL_SEEN.getStateId());
																		callStatesService.update(userCallState);
																	}else{
																		messageToSend.addProperty("status", "fail");
																		messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.invalidRequest"));
																	}
																}
															} else if(jsonMessageObj.get("roomid") != null) {
																String roomIds = jsonMessageObj.get("roomid").getAsString();
																UserCallState userCallState =callStatesService.callStateByRoom(roomIds);
																if(userCallState!=null ){
																	userCallState.setCallStatus(CallState.MISSED_CALL_SEEN.getStateId());
																	callStatesService.update(userCallState);
																}else{
																	messageToSend.addProperty("status", "fail");
																	messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.invalidRequest"));
																}
															} else{
																messageToSend.addProperty("status", "fail");
																messageToSend.addProperty("message", CapvUtil.environment.getProperty("message.invalidRequest"));
															}
														} catch (Exception e) {
															messageToSend.addProperty("status", "fail");
														} finally {
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															
															
														}
									    			}
													break;
													
									    			case CapvClientUserConstants.WS_MESSAGE_GET_MISSED_CALLS :
									    			{
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_GET_MISSED_CALLS_RESPONSE);
														messageToSend.addProperty("status", "success");
														
														CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
														
														try {
															int resultOffset=0;
															int maxResults=20;
															
															if(jsonMessageObj.get("resultOffset")!=null){
																resultOffset=Integer.parseInt(jsonMessageObj.get("resultOffset").getAsString());
															}
															if(jsonMessageObj.get("maxResults")!=null){
																maxResults=Integer.parseInt(jsonMessageObj.get("maxResults").getAsString());
															}
															
															List<UserCallState> userCallStates=callStatesService.getMissedCallLog(userName, resultOffset, maxResults);
															
															Type objType = new TypeToken<List<UserCallState>>() {}.getType();
															
															messageToSend.add("call_states",  CapvClientUserUtil.convertToJsonElement(userCallStates, objType));
															
														} catch (Exception e) {
															e.printStackTrace();
															messageToSend.addProperty("status", "fail");
														} finally {
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession,CapvClientUserUtil
																														.convertToJsonString(messageToSend));
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															
															/*List<UserSession> userAllSessions = UserRegistry.getUserSessionsByUserName(userName);
															for(UserSession currentUserSession :userAllSessions) {
																if(!currentUserSession.getWebSocketSession().getId().equals(session.getId())) {
																	UserWebSocketMessage rosterUserWebSocketMessage = new UserWebSocketMessage
																										(currentUserSession, messageToSend.toString());
																	currentUserSession.getCapvUserWebSocketMessageProcessor()
																										.processMessage(rosterUserWebSocketMessage);
																}
															}*/
															
															
														}
									    			}
													break;
													

													
									    			case CapvClientUserConstants.WS_MESSAGE_GET_ROSTER_USERS_PROFILE_PICTURES :
									    			{
									    				JsonObject messageToSend = new JsonObject();
														messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																					CapvClientUserConstants.WS_MESSAGE_GET_ROSTER_USERS_PROFILE_PICTURES_RESPONSE);
														
														Map<String, Map<String,String>> rosterUsersProfilePictures = new HashMap<>();
															
														try {
															CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																							getCapvChatUserRequestProcessor(userSession);
															
															List<User> rosterUsers = capvChatUserRequestProcessor.getRosterUsers();
															
															for(User rosterUser :rosterUsers) {
																
																Map<String, String> profilePictureData = 
																		getCapvChatUserRequestProcessor(userSession).getUserProfilePicture(rosterUser.getName());
																
																if(profilePictureData != null && profilePictureData.size() > 0) {
																	rosterUsersProfilePictures.put(rosterUser.getName(), profilePictureData);
																}
															}
															
															Type profilePictureType = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
															messageToSend.add("rosteUsersProfilePictures", 
																	CapvClientUserUtil.convertToJsonElement(rosterUsersProfilePictures, profilePictureType));
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																											(userSession, messageToSend.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															
															
														} catch (Exception e) {
															e.printStackTrace();
														}
									    			}
													break;
													
									    			case CapvClientUserConstants.WS_MESSAGE_UPDATE_CALL_STATS: {
									    				if(jsonMessageObj.get("callStats") != null) {
									    					String callStatsString = jsonMessageObj.get("callStats").getAsJsonArray().toString();
									    					CapvClientUserUtil.updateCallStatistics(callStatsString);
									    				}
									    			}
									    			break;
									    			case CapvClientUserConstants.WS_MESSAGE_PING: {
									    				try {
									    						com.capv.um.model.User user = userService.getById(userSession.getUserId());
									    						JsonObject messageToSend = new JsonObject();
									    						messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_PONG);
									    						UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToSend.toString());
									    						userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
									    						user.setPing(new Date()); 
									    						userService.update(user, false);
									    					}catch(Exception e){
									    						log.debug("Erro while sending ping Message::"+e);
									    						e.printStackTrace();
									    					}
									    			}
									    			break;
									    			
									    			case CapvClientUserConstants.WS_MESSAGE_GET_CONFIG_PROPS: {
									    				Long clientId = 0L;
									    				String propertyName = "";
									    				try{
											    				if(jsonMessageObj.get("clientId") != null && jsonMessageObj.get("propertyName") != null){
											    						clientId = Long.parseLong(jsonMessageObj.get("clientId").getAsString());
									    								propertyName = jsonMessageObj.get("propertyName").getAsString();
											    				}
									    				
											    				Long propertyValue = Long.parseLong(CapvClientUserUtil.getClientConfigProperty(clientId, propertyName));
											    				
											    				JsonObject jsonObject = new JsonObject();
																jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GET_CONFIG_PROPS);
																
																jsonObject.addProperty("propertyName", propertyName);
																jsonObject.addProperty("propertyValue", propertyValue);
																
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession,
																												CapvClientUserUtil.convertToJsonString(jsonObject));
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
																
									    				}catch (Exception e) {
									    						log.debug("Error while fetching configuration proerties values : "+e);
									    						e.printStackTrace();
														}
									    				
									    			}
									    			break;
									    			
								    				case "test": 	
					    							{
					    								String from = jsonMessageObj.get("from").getAsString();
					    								String msg = jsonMessageObj.get("message").getAsString();
					    								
					    								
					    								JsonObject jsonObject = new JsonObject();
					    								jsonObject.addProperty("from", from);
					    								jsonObject.addProperty("message", msg);
					    								
					    								final CyclicBarrier gate = new CyclicBarrier(20);
					    								for(int i=1; i<=10000; i++) {
					    									
					    									Thread t = new Thread(){
					    										public void run(){
					    									    	try {
					    									    		gate.await();
					    									    		CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName,
					    									    										CapvClientUserUtil
					    									    											.convertToJsonString(jsonObject));
					    									    		
					    									    	} catch (Exception e){}
					    										}
					    									};
					    									t.start();
					    								}
					    								
					    								
					    							}
					    							break;
								    				
					default: break;
	    		}
	    		
	    	} else if(jsonMessageObj.get("id") != null) {
	    		
	    		System.out.println(message);
	    		String vcMsg = jsonMessageObj.get("id").getAsString();
	    		UserCallState callState = null;
	    		com.capv.um.model.User user_stat=new com.capv.um.model.User();
	    switch(vcMsg) {
	    	
	    	case CapvClientUserConstants.VC_MSG_ONE: {
	    		System.err.println("YoYoYO :"+jsonMessageObj);
	    		com.capv.um.model.User caleeUser = new com.capv.um.model.User();
	    		try{
	    			String calee = jsonMessageObj.get("callee").getAsString();
		    		 caleeUser = userService.getByUserName(calee, false);
	    		}
	    		catch (Exception e) {
	    			e.printStackTrace();
				}
	    		 String host_service="";
	    				if(jsonMessageObj.get("host")==null){
	    					host_service=CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName).getService();
	    					
	    				}else
	    				{	host_service=jsonMessageObj.get("host").getAsString();
	    					
	    				}
	    				
	    								boolean isVCServiceConnected = false;
	    								

							    		try {
											CapvVideoCallingWebSocketClientConfiguration videoCallingWebSocketClientConfiguration = 
																							CapvVideoCallingWebSocketClientConfiguration.connectionConfigurationBuilder()
																																		.setUserName(userName)
																																		.setWebSocketSessionId(session.getId())
																																		.setUserService(userService)
																																		.setCallStateService(callStatesService)
																																		.setVideoRecordingService(videoRecordingService)
																																		.settryIt(false)
																																		.build();
											Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
																			new Capv_VC_WS_ClientHandler(videoCallingWebSocketClientConfiguration);
											
											capvVideoCallingWebSocketClientEndpoint.connectToClient();
											
								        	//Session vcWebSocketSession = vcClientConnectSessionFuture.get();
								        	
								        	if(capvVideoCallingWebSocketClientEndpoint.isSessionAlive()) {
								        		isVCServiceConnected = true;
								        	}
										        	
									 		userSession.setCapvVideoCallingWebSocketClientEndpoint(capvVideoCallingWebSocketClientEndpoint);
										} catch(Exception e) {
									 		log.error("Error while initializing capv video calling session::", e);
									 	}
//									 	}
							    		
							    		if(!isVCServiceConnected) {
							    			
							    			JsonObject vcConnectErrorMessage = new JsonObject();
		    								
							    			vcConnectErrorMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																				CapvClientUserConstants.WS_MESSAGE_VC_SERVICE_CONNECT_ERROR);
											
											UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																							(userSession, vcConnectErrorMessage.toString());
											userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
											
											return;
							    		}
	    								boolean isCallerStateUpdated = false;
    									try {
					    						if(jsonMessageObj.get("callerId") == null) {
					    							
					    							com.capv.um.model.User caller = userService.getById(userSession.getUserId());
					    							String callee= jsonMessageObj.get("callee").getAsString();
					    							
					    							JsonObject obj_msg;
					    							
					    							
					    							Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
																						userSession.getCapvVideoCallingWebSocketClientEndpoint();
					    							
					    							if(caller.getCallStatus() == UserState.IDLE.getStateId()) {
					    								
					    								caller.setCallStatus(UserState.CALLING.getStateId());
						    							userService.update(caller, false);
						    							isCallerStateUpdated = true;
					    								
						    							List<UserCallState> userCalls = callStatesService.oneToOneCallsInProgressByCallers(
																											userName, callee, userSession.getClientId());
		
														if(userCalls != null && userCalls.size() > 0) {
															
															/*for(UserCallState userCall :userCalls) {
																
																JsonObject exitMessage = new JsonObject();
																exitMessage.addProperty(  "method" , "leaveRoom");
																exitMessage.addProperty("id" , 11);
																exitMessage.addProperty("jsonrpc" , "2.0");
																JsonObject exitMessageParams = new JsonObject();
																exitMessageParams.addProperty("user", userName);
																exitMessageParams.addProperty("roomName", userCall.getRoomNo());
																exitMessage.add("params", exitMessageParams);
																 
																userSession.getCapvVideoCallingWebSocketClientEndpoint().sendMessage(exitMessage.toString());
																
																if(userCall.getCalleeList() != null && 
																		userCall.getCalleeList().indexOf(userSession.getUserName()) >= 0) {
																	
																	userCall.setCallStatus(CallState.ENDED.getStateId());
																	userCall.setEndTime(new Date());
																	userCall.setUpdateTime(new Date());
																	
																	callStatesService.update(userCall);
																}
															}*/
															
															UserCallState inProgressCall = null;

															for(UserCallState userCall :userCalls) {
																String participantsStr = userCall.getCalleeList();
																String[] callParticipants = participantsStr.split(",");
																
																boolean callParticipantsFound = false;
																for(String callParticipant :callParticipants) {
																	if(!callParticipant.equals(userName)) {
																		List<UserSession> callParticipantSessions = 
																				UserRegistry.getUserSessionsByUserName(callParticipant);
																
																		if(callParticipantSessions != null) {
																			for(UserSession participantSession: callParticipantSessions) {
																				
																				if(participantSession.isCallInProgress()) {
																					
																					Map<String, Object> participantCallData = participantSession.getCallData();
																					
																					if(participantCallData != null && 
																							participantCallData.get("roomNumber") != null && 
																							((String)participantCallData.get("roomNumber")).equals(userCall.getRoomNo())) {
																						
																						callParticipantsFound = true;
																						break;
																					}
																				}
																			}
																		}
																	}
																}
																
																if(callParticipantsFound) {
																	inProgressCall = userCall;
																	break;
																} else {
																	userCall.setCallStatus(CallState.ENDED.getStateId());
																	callStatesService.update(userCall);
																}
															}
															
															if(inProgressCall != null) {
																
																JsonObject messageToSend = new JsonObject();
							                                	messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
							                                								CapvClientUserConstants.WS_MESSAGE_SET_ROOM);
								    			            	messageToSend.addProperty("roomNumber", inProgressCall.getRoomNo() );
								    			            	messageToSend.addProperty("callType", CapvClientUserConstants.VC_MSG_ONE);
								    			            	if(inProgressCall.getCallerName().equals(userName)){
								    			            		messageToSend.addProperty("isInitiator", true);
														 }else {
															messageToSend.addProperty("isInitiator", false);
														 }
								    			            	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, messageToSend.toString());
								    			            	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
								    			            	
																jsonMessageObj.remove("id");
																jsonMessageObj.addProperty(  "method" , "joinRoom");
																jsonMessageObj.addProperty("id" , 0);
																jsonMessageObj.addProperty("jsonrpc" , "2.0");
																
																JsonObject params = new JsonObject();
																params.addProperty("room" , inProgressCall.getRoomNo());
																params.addProperty("user", userName);
																params.addProperty("callMode", jsonMessageObj.get("callMode") != null ? 
																								jsonMessageObj.get("callMode").getAsString() 
																								: "video");
																params.addProperty("istryit", "disable");
																params.addProperty("rec_type", CapvClientUserUtil.getClientConfigProperty(
																		userSession.getClientId(),
																		CapvClientUserConstants.REC_TYPE));
																params.addProperty("dataChannels", true);
																jsonMessageObj.add("params", params);
																
																capvVideoCallingWebSocketClientEndpoint.sendMessage(jsonMessageObj.toString());
																
																Map<String, Object> callData = userSession.initializeCallData();
																callData.put("callType", CapvClientUserConstants.CALL_TYPE_ONE);
																callData.put("roomNumber", inProgressCall.getRoomNo());
																callData.put("callMode", inProgressCall.getCallMode());
																
																
																userSession.setCallInProgress(true);
																
								    			            	if(jsonMessageObj.get("callMode") != null && 
								    			            			jsonMessageObj.get("callMode").getAsString().equals("video")) {
								    			            		Map<String, Object> properties = new HashMap<>();
								    			            		properties.put("roomId", inProgressCall.getRoomNo());
								    			            		properties.put("isFullVideo", true);
								    			            		VideoRecording videoRecordingModel = 
								    			            				videoRecordingService.getVideoRecordingDetailsByMatchingProperties(properties);
								    			            		
								    			            		if(videoRecordingModel != null)
								    			            			callData.put("videoFullRecordId", videoRecordingModel.getId());
								    			            	}
								    			            	
																return;
															}
														
														}
					    							} else {
					    								
					    								JsonObject rej_msg = new JsonObject();
					    								
														rej_msg.addProperty("id", "callresponse");
														rej_msg.addProperty("statusType", "callerStatus");
														rej_msg.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(caller.getCallStatus()));
														
														UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(userSession, rej_msg.toString());
														userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														
														return;
					    							}
					    						
					    							caller.setCallStatus(UserState.CALLING.getStateId());
					    							userService.update(caller, false);
					    							isCallerStateUpdated = true;
					    							
					    							String roomNumber = UUID.randomUUID().toString();
					    							VideoRecording videoRecordingModel = null;
					    							if(roomNumber != null){
					    								
					    								 JsonObject messageToSend = new JsonObject();
					    								 
					                                	 messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_SET_ROOM);
						    			            	 messageToSend.addProperty("roomNumber", roomNumber );
						    			            	 messageToSend.addProperty("callType", CapvClientUserConstants.VC_MSG_ONE);
						    			            	 
						    			            	 UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(userSession, messageToSend.toString());
						    			            	 userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
						    			            	 
						    			            	 if(jsonMessageObj.get("callMode").getAsString().equalsIgnoreCase("video")) {
						    			            		 videoRecordingModel = new VideoRecording();
						    			            		 
						    			            		 videoRecordingModel.setUserName(userSession.getUserName());
						    			            		 videoRecordingModel.setRoomId(roomNumber);
						    			            		 videoRecordingModel.setClientId(userSession.getClientId().intValue());
						    			            		 videoRecordingModel.setFullVideo(true);
						    			            		 videoRecordingModel.setScheduler(false);
						    			            		 videoRecordingModel.setSourcePath("/tmp/");
						    			            		 videoRecordingModel.setStarttime(new Date());
						    			            		 
						    			            		 videoRecordingService.save(videoRecordingModel);
						    			            	 }
					    							}
					    			            	// sendMessage(session, CapvClientUserUtil.convertToJsonString(messageToSend));
					    							
						    		       		 	callState = new UserCallState();
													callState.setCallerName(userSession.getUserName());
													callState.setClientId(userSession.getClientId());
													callState.setJid(userSession.getUserName() + "@" + host_service);
													callState.setCalleeList(callee + "," + userName);
													callState.setStartTime(new Date());
													callState.setUpdateTime(new Date());
													callState.setCallType(CapvClientUserConstants.CALL_TYPE_ONE);
													if(jsonMessageObj.get("callMode").getAsString().equals("video"))
													    callState.setCallMode(1);
													else
														callState.setCallMode(0);
													callState.setRoomNo(roomNumber);
													callState.setAdmin(userSession.getUserName());
													
													obj_msg=new JsonObject();
													/*obj_msg.addProperty("id", "enter");
													obj_msg.addProperty("callerId", roomNumber);
													obj_msg.addProperty("userId", userName);*/
													
													obj_msg.addProperty(  "method" , "joinRoom");
													obj_msg.addProperty("id" , 0);
													obj_msg.addProperty("jsonrpc" , "2.0");
													JsonObject params = new JsonObject();
													params.addProperty("room" , roomNumber);
													params.addProperty("user", userName);
													params.addProperty("callMode", jsonMessageObj.get("callMode") != null ? 
																					jsonMessageObj.get("callMode").getAsString() 
																					: "video");
													params.addProperty("istryit", "disable");
													params.addProperty("rec_type", CapvClientUserUtil.getClientConfigProperty(
															userSession.getClientId(),
															CapvClientUserConstants.REC_TYPE));
													params.addProperty("dataChannels", true);
													obj_msg.add("params", params);
													
													capvVideoCallingWebSocketClientEndpoint.sendMessage(obj_msg.toString());
													
													callState.setCallStatus(CallState.STARTED.getStateId());
													callState.setDelete_vid_flag(0);
													callStatesService.save(callState);
													
													Map<String, Object> callData = userSession.initializeCallData();
													callData.put("callType", CapvClientUserConstants.CALL_TYPE_ONE);
													callData.put("roomNumber", roomNumber);
													callData.put("callMode", jsonMessageObj.get("callMode").getAsString());
													
													if(videoRecordingModel != null)
				    			            			callData.put("videoFullRecordId", videoRecordingModel.getId());
													
													userSession.setCallInProgress(true);
						    		       		 	
						    						if(userSession.getWebSocketSession()!=null) {
						    							
														user_stat = userService.getByUserName(callee, false);
														byte userCallStatus = user_stat.getCallStatus();
														if(userCallStatus != 0 && 
																(userCallStatus == UserState.IDLE.getStateId())){
														   
															/*JsonObject resp=new JsonObject();
															resp.addProperty("id", "callresponse");
															resp.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(user_stat.getCallStatus()));
															resp.addProperty("callee", callee);*/
															
															//capvChatUserRequestProcessor.sendMessage(resp.toString(),userName);
															//PUSH NOTIFICATIONS
														
															List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(callee);
															if(userSessions == null && caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("ios")){
					    							    				JsonObject scParams=new JsonObject();
					    										
					    							    				scParams.addProperty("id", "incommingcall");
					    							    				scParams.addProperty("roomnumber", roomNumber);
					    							    				scParams.addProperty("from",userName);
					    							    				scParams.addProperty("callerName", caller.getName());
					    							    				scParams.addProperty("callType", CapvClientUserConstants.CALL_TYPE_ONE);
						    									
					    							    				if(videoRecordingModel != null)
					    							    					scParams.addProperty("videoFullRecordId", videoRecordingModel.getId());
						    									
					    							    				if(jsonMessageObj.get("callMode") != null)
					    							    					scParams.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
					    							    				apnsService.pushCallNotification(caleeUser.getTokenId(),scParams.toString(),caleeUser.getClientId());
															}else if(userSessions == null && caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("android")){
																JsonObject scParams=new JsonObject();
					    										
			    							    							scParams.addProperty("id", "incommingcall");
			    							    							scParams.addProperty("roomnumber", roomNumber);
			    							    							scParams.addProperty("from1",userName);
			    							    							scParams.addProperty("callerName", caller.getName());
			    							    							scParams.addProperty("callType", CapvClientUserConstants.CALL_TYPE_ONE);
				    									
			    							    							if(videoRecordingModel != null)
			    							    								scParams.addProperty("videoFullRecordId", videoRecordingModel.getId());
				    									
			    							    							if(jsonMessageObj.get("callMode") != null)
			    							    								scParams.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
			    							    							fcmService.sendMessage(caleeUser.getTokenId(),scParams,caleeUser.getClientId());
															}
						    								if(userSessions != null){
						    									Long receiverId = null;
						    									for(UserSession receiverSession :userSessions)
						    									{
						    										if(receiverId == null)
						    											receiverId = receiverSession.getUserId();
						    										
						    										JsonObject scParams=new JsonObject();
						    										
						    										scParams.addProperty("id", "incommingcall");
							    									scParams.addProperty("roomnumber", roomNumber);
							    									scParams.addProperty("from",userName);
							    									scParams.addProperty("callerName", caller.getName());
							    									scParams.addProperty("callType", CapvClientUserConstants.CALL_TYPE_ONE);
							    									
							    									if(videoRecordingModel != null)
							    										scParams.addProperty("videoFullRecordId", videoRecordingModel.getId());
							    									
							    									if(jsonMessageObj.get("callMode") != null)
								    									scParams.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
							    									if(caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("ios")){
							    							    			if(caleeUser.getLastSigninOs().equals(receiverSession.getLastSingedOs())) {
							    							    				apnsService.pushCallNotification(caleeUser.getTokenId(),scParams.toString(),caleeUser.getClientId());
							    							    			}
							    							      	}else if(caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("android")){
							    							      		if(caleeUser.getLastSigninOs().equals(receiverSession.getLastSingedOs())) {
							    							      			JsonObject scParams1=new JsonObject();
							    							      			scParams1.addProperty("id", "incommingcall");
							    							      			scParams1.addProperty("roomnumber", roomNumber);
							    							      			scParams1.addProperty("from1",userName);
							    							      			scParams1.addProperty("callerName", caller.getName());
							    							      			scParams1.addProperty("callType", CapvClientUserConstants.CALL_TYPE_ONE);
							    							      			if(videoRecordingModel != null)
							    							      				scParams1.addProperty("videoFullRecordId", videoRecordingModel.getId());
									    									
									    									if(jsonMessageObj.get("callMode") != null)
									    										scParams1.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
						    							    				fcmService.sendMessage(caleeUser.getTokenId(),scParams1,caleeUser.getClientId());
							    							      		}
							    							      	}
							    									
							    										UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(
							    												receiverSession, scParams.toString());
							    											userSession.getCapvUserWebSocketMessageProcessor()
							    											.processMessage(userWebSocketMessage);
							    									
						    									}
						    									
						    									if(receiverId != null) {
						    										com.capv.um.model.User receiver = userService.getById(receiverId);
						    										
						    										if(receiver != null) {
						    											receiver.setCallStatus(UserState.RECEIVING.getStateId());
						    											userService.update(receiver, false);
						    										}
						    									}
							    							}
														} else {
															
															JsonObject rej_msg=new JsonObject();
															rej_msg.addProperty("id", "callresponse");
															rej_msg.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(user_stat.getCallStatus()));
															rej_msg.addProperty("statusType", "calleeStatus");
															rej_msg.addProperty("callee", callee);
															
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																											(userSession, rej_msg.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														}
															
															 //capvChatUserRequestProcessor.sendMessage(CapvClientUserUtil.convertToJsonString(rej_msg),temp + '@' + CapvClientUserUtil.getClientConfigProperty(userSession.getClientId(), 
																		//CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY));
						
															 /*capvVideoCallingWebSocketClientEndpoint.sendMessage(CapvClientUserUtil.convertToJsonString(rej_msg));*/
															
						    						}
						    						
					    						} else {
					    							
													if(userSession != null) {
														
														if(jsonMessageObj.get("userId") != null && jsonMessageObj.get("callerId") != null) {
															
															String roomNo = jsonMessageObj.get("callerId").getAsString();
															
															UserCallState roomState = callStatesService.callStateByRoom(roomNo);
															
															if(roomState != null && (roomState.getCallStatus() == CallState.STARTED.getStateId()
																	|| roomState.getCallStatus() == CallState.INPROGRESS.getStateId())) {
																
																String userName1 = jsonMessageObj.get("userId").getAsString();
																
																com.capv.um.model.User user = userService.getByUserName(userName1, false);
																
																if(user != null && (user.getCallStatus() == UserState.IDLE.getStateId() || 
																					user.getCallStatus() == UserState.RECEIVING.getStateId())) {
																	
																	Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
																										userSession.getCapvVideoCallingWebSocketClientEndpoint();
																	
																	/*jsonMessageObj.remove("id");
																	jsonMessageObj.addProperty("id", "enter")*/;
																	jsonMessageObj.remove("id");
																	jsonMessageObj.addProperty(  "method" , "joinRoom");
																	jsonMessageObj.addProperty("id" , 0);
																	jsonMessageObj.addProperty("jsonrpc" , "2.0");
																	JsonObject params = new JsonObject();
																	params.addProperty("room" , roomNo);
																	params.addProperty("user", userName);
																	params.addProperty("callMode", jsonMessageObj.get("callMode") != null ? 
																								jsonMessageObj.get("callMode").getAsString() 
																								: "video");
																	params.addProperty("istryit", "disable");
																	params.addProperty("rec_type", CapvClientUserUtil.getClientConfigProperty(
																			userSession.getClientId(),
																			CapvClientUserConstants.REC_TYPE));
																	params.addProperty("dataChannels", true);
																	jsonMessageObj.add("params", params);
															 		
																	capvVideoCallingWebSocketClientEndpoint.sendMessage(jsonMessageObj.toString());
																	
																	Map<String, Object> callData = userSession.initializeCallData();
																	callData.put("callType", CapvClientUserConstants.CALL_TYPE_ONE);
																	callData.put("roomNumber", roomNo);
																	callData.put("callMode", jsonMessageObj.get("callMode") != null ? 
																								jsonMessageObj.get("callMode").getAsString() : "");
																	
																	if(jsonMessageObj.get("callMode") != null && 
																			jsonMessageObj.get("callMode").getAsString().equalsIgnoreCase("video")) {
																		
																		Map<String, Object> properties = new HashMap<>();
									    			            		properties.put("roomId", roomNo);
									    			            		properties.put("isFullVideo", true);
									    			            		
									    			            		VideoRecording videoRecordingModel = 
									    			            				videoRecordingService.getVideoRecordingDetailsByMatchingProperties(properties);
									    			            		
																		callData.put("videoFullRecordId", videoRecordingModel.getId());
																	}
																	
																	userSession.setCallInProgress(true);
																} else {
																	
																	JsonObject rej_msg = new JsonObject();
																	
																	rej_msg.addProperty("id", "calljoinresponse");
																	rej_msg.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(user.getCallStatus()));
																	
																	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																													(userSession, rej_msg.toString());
																	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
																}
																
															} else {
																
																JsonObject rej_msg = new JsonObject();
																
																rej_msg.addProperty("id", "calljoinresponse");
																rej_msg.addProperty("roomstatus", CallState.INPROGRESS.getStateByStateId(roomState.getCallStatus()));
																
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, rej_msg.toString());
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															}
														}
													}
					    						}
					    				} catch (Exception e) {
					    					e.printStackTrace();
					    					
					    					if(callState == null || callState.getId() == null) {
					    						if(isCallerStateUpdated) {
						    						com.capv.um.model.User caller = userService.getById(userSession.getUserId());
					    							caller.setCallStatus(UserState.IDLE.getStateId());
					    							userService.update(caller, false);
						    					}
					    					}
					    				}
					    			}
					    		
			break;
	    	case CapvClientUserConstants.VC_MSG_GROUP:   {
	    		
						    		boolean isVCServiceConnected = false;
						    		String host_service="";
						    		if(jsonMessageObj.get("host")==null){
						    			host_service=CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName).getService();
				    					
				    				}else {	
				    					host_service=jsonMessageObj.get("host").getAsString();
				    				}
						    		
						    		try {
										CapvVideoCallingWebSocketClientConfiguration videoCallingWebSocketClientConfiguration = 
																						CapvVideoCallingWebSocketClientConfiguration
																							.connectionConfigurationBuilder()
																							.setUserName(userName)
																							.setUserService(userService)
																							.setWebSocketSessionId(session.getId())
																							.setCallStateService(callStatesService)
																							.setVideoRecordingService(videoRecordingService)
																							.build();
										Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
																		new Capv_VC_WS_ClientHandler(videoCallingWebSocketClientConfiguration);
										
										capvVideoCallingWebSocketClientEndpoint.connectToClient();
										
							        	if(capvVideoCallingWebSocketClientEndpoint.isSessionAlive()) {
							        		isVCServiceConnected = true;
							        	}
									        	
								 		userSession.setCapvVideoCallingWebSocketClientEndpoint(capvVideoCallingWebSocketClientEndpoint);
									} catch(Exception e) {
								 		log.error("Error while initializing capv video calling session::", e);
								 	}
						    		
						    		if(!isVCServiceConnected) {
						    			
						    			JsonObject vcConnectErrorMessage = new JsonObject();
										
						    			vcConnectErrorMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																			CapvClientUserConstants.WS_MESSAGE_VC_SERVICE_CONNECT_ERROR);
										
										UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																						(userSession, vcConnectErrorMessage.toString());
										userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
										
										return;
						    		}
    				
						    		boolean isCallerStateUpdated = false;
									try {
											if(jsonMessageObj.get("callerId") == null) {
												
												com.capv.um.model.User caller = userService.getById(userSession.getUserId());
												String callee = jsonMessageObj.get("callee").getAsString();
												String roomJid = jsonMessageObj.get("room_jid").getAsString() + 
																							"@conference." + host_service;
												
												CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
												String adminUser = capvChatUserRequestProcessor.getAdminDetails(jsonMessageObj.get("room_jid").getAsString());
												
												Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
																					userSession.getCapvVideoCallingWebSocketClientEndpoint();
												
												if(caller.getCallStatus() == UserState.IDLE.getStateId()) {
													
													caller.setCallStatus(UserState.CALLING.getStateId());
					    							userService.update(caller, false);
					    							isCallerStateUpdated = true;
					    							
					    							UserCallState groupCall = callStatesService.getLastActiveGroupCallByJid(roomJid);
					
													if(groupCall != null) {
														
														String groupParticipantsStr = groupCall.getCalleeList();
														String[] groupParticipants = groupParticipantsStr.split(",");
														
														boolean groupParticipantsFound = false;
														
														// Check group participants other than caller to verify whether they really active in the call 
														// and join the call initiator into the same call if they found in the call
														for(String groupParticipant :groupParticipants){
															
															if(!groupParticipant.equals(userName)) {
																
																List<UserSession> groupParticipantSessions = 
																				UserRegistry.getUserSessionsByUserName(groupParticipant);
																
																if(groupParticipantSessions != null) {
																	for(UserSession participantSession: groupParticipantSessions) {
																		
																		if(participantSession.isCallInProgress()) {
																			
																			Map<String, Object> participantCallData = participantSession.getCallData();
																			
																			if(participantCallData != null && 
																					participantCallData.get("roomNumber") != null && 
																					((String)participantCallData.get("roomNumber")).equals(groupCall.getRoomNo())) {
																				
																				groupParticipantsFound = true;
																				break;
																			}
																		}
																	}
																}
																
															}
														}
														
														if(groupParticipantsFound) {
															
															JsonObject messageToSend = new JsonObject();
						                                	messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_SET_ROOM);
							    			            	messageToSend.addProperty("roomNumber", groupCall.getRoomNo() );
							    			            	messageToSend.addProperty("callType", CapvClientUserConstants.VC_MSG_GROUP);
							    			            	
							    			            	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																											(userSession, messageToSend.toString());
							    			            	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
							    			            	
															jsonMessageObj.remove("id");
															jsonMessageObj.addProperty(  "method" , "joinRoom");
															jsonMessageObj.addProperty("id" , 0);
															jsonMessageObj.addProperty("jsonrpc" , "2.0");
															JsonObject params = new JsonObject();
															params.addProperty("room" , groupCall.getRoomNo());
															params.addProperty("user", userName);
															params.addProperty("callMode", jsonMessageObj.get("callMode") != null ? 
																							jsonMessageObj.get("callMode").getAsString() 
																							: "video");
															params.addProperty("istryit", "disable");
															params.addProperty("rec_type", CapvClientUserUtil.getClientConfigProperty(
																	userSession.getClientId(),
																	CapvClientUserConstants.REC_TYPE));
															params.addProperty("dataChannels", true);
															jsonMessageObj.add("params", params);
															
															capvVideoCallingWebSocketClientEndpoint.sendMessage(jsonMessageObj.toString());
															
															Map<String, Object> callData = userSession.initializeCallData();
															callData.put("callType", CapvClientUserConstants.CALL_TYPE_GROUP);
															callData.put("roomNumber", groupCall.getRoomNo());
															callData.put("callMode", groupCall.getCallMode());
															
															userSession.setCallInProgress(true);
															
							    			            	if(jsonMessageObj.get("callMode") != null && 
							    			            			jsonMessageObj.get("callMode").getAsString().equals("video")) {
							    			            		Map<String, Object> properties = new HashMap<>();
							    			            		properties.put("roomId", groupCall.getRoomNo());
							    			            		properties.put("isFullVideo", true);
							    			            		VideoRecording videoRecordingModel = 
							    			            				videoRecordingService.getVideoRecordingDetailsByMatchingProperties(properties);
							    			            		
							    			            		if(videoRecordingModel != null)
							    			            			callData.put("videoFullRecordId", videoRecordingModel.getId());
							    			            	}
															return;
														} else {
															groupCall.setCallStatus(CallState.ENDED.getStateId());
															callStatesService.update(groupCall);
														}
													}
												} else {
													JsonObject rej_msg=new JsonObject();
													rej_msg.addProperty("id", "callresponse");
													rej_msg.addProperty("statusType", "callerStatus");
													rej_msg.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(caller.getCallStatus()));
													UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																									(userSession, rej_msg.toString());
													userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
													return;
												}
											
												caller.setCallStatus(UserState.CALLING.getStateId());
												userService.update(caller, false);
												isCallerStateUpdated = true;
												
												String roomNumber = UUID.randomUUID().toString();
												VideoRecording videoRecordingModel = null;
												if(roomNumber != null){
				    								JsonObject messageToSend = new JsonObject();
				                                	messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_SET_ROOM);
					    			            	messageToSend.addProperty("roomNumber", roomNumber );
					    			            	messageToSend.addProperty("callType", CapvClientUserConstants.VC_MSG_GROUP);
					    			            	 
					    			            	UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(userSession, messageToSend.toString());
				    			            	 	userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
				    			            	 	
					    			            	if(jsonMessageObj.get("callMode").getAsString().equalsIgnoreCase("video")) {
					    			            		 videoRecordingModel = new VideoRecording();
					    			            		 videoRecordingModel.setUserName(userSession.getUserName());
					    			            		 videoRecordingModel.setRoomId(roomNumber);
					    			            		 videoRecordingModel.setClientId(userSession.getClientId().intValue());
					    			            		 videoRecordingModel.setFullVideo(true);
					    			            		 videoRecordingModel.setScheduler(false);
					    			            		 videoRecordingModel.setSourcePath("/tmp/");
					    			            		 videoRecordingModel.setStarttime(new Date());
					    			            		 videoRecordingService.save(videoRecordingModel);
					    			            	}
				    							};
					    		       		 	callState = new UserCallState();
												callState.setCallerName(userSession.getUserName());
												callState.setClientId(userSession.getClientId());
												callState.setJid(roomJid);
												callState.setCalleeList(callee + "," + userName);
												callState.setStartTime(new Date());
												callState.setUpdateTime(new Date());
												callState.setCallType(CapvClientUserConstants.CALL_TYPE_GROUP);
												if(jsonMessageObj.get("callMode").getAsString().equals("video"))
												    callState.setCallMode(1);
												else
													callState.setCallMode(0);
												callState.setRoomNo(roomNumber);
												callState.setAdmin(adminUser);
												
												jsonMessageObj.remove("id");
												jsonMessageObj.addProperty(  "method" , "joinRoom");
												jsonMessageObj.addProperty("id" , 0);
												jsonMessageObj.addProperty("jsonrpc" , "2.0");
												JsonObject params = new JsonObject();
												params.addProperty("room" , roomNumber);
												params.addProperty("user", userName);
												params.addProperty("callMode", jsonMessageObj.get("callMode") != null ? 
																				jsonMessageObj.get("callMode").getAsString() 
																				: "video");
												params.addProperty("istryit", "disable");
												params.addProperty("rec_type", CapvClientUserUtil.getClientConfigProperty(
														userSession.getClientId(),
														CapvClientUserConstants.REC_TYPE));
												params.addProperty("dataChannels", true);
												jsonMessageObj.add("params", params);
												
												capvVideoCallingWebSocketClientEndpoint.sendMessage(jsonMessageObj.toString());
												
												callState.setCallStatus(CallState.STARTED.getStateId());
												callState.setDelete_vid_flag(0);
												callStatesService.save(callState);
												
												Map<String, Object> callData = userSession.initializeCallData();
												callData.put("callType", CapvClientUserConstants.CALL_TYPE_GROUP);
												callData.put("roomNumber", roomNumber);
												callData.put("callMode", jsonMessageObj.get("callMode").getAsString());
												callData.put("callee", callee);
												callData.put("groupCallInitiator", true);
												callData.put("roomName", jsonMessageObj.get("room_jid").getAsString());
												if(videoRecordingModel != null)
			    			            			callData.put("videoFullRecordId", videoRecordingModel.getId());
												
												userSession.setCallInProgress(true);
					    		       		 	
					    						if(userSession.getWebSocketSession()!=null) {
					    							
					    							JsonObject incomingCallMessage = new JsonObject();
		    										
					    							incomingCallMessage.addProperty("id", "incommingcall");
					    							incomingCallMessage.addProperty("roomnumber", roomNumber);
					    							incomingCallMessage.addProperty("roomName", jsonMessageObj.get("room_jid").getAsString());
					    							incomingCallMessage.addProperty("callType", CapvClientUserConstants.CALL_TYPE_GROUP);
					    							incomingCallMessage.add("calleeList", jsonMessageObj.get("calleeList"));
					    							incomingCallMessage.addProperty("adminUser", adminUser);
			    									if(videoRecordingModel != null)
			    										incomingCallMessage.addProperty("videoFullRecordId", videoRecordingModel.getId());
			    									
			    									if(jsonMessageObj.get("callMode") != null)
			    										incomingCallMessage.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
					    							
					    							String[] calleeList = callee.split(",");
					    							
					    							for(String receiver :calleeList) {
					    								
					    								user_stat = userService.getByUserName(receiver, false);
					    								if(user_stat != null) {
					    									byte userCallStatus = user_stat.getCallStatus();
															if(userCallStatus != 0 && 
																	(userCallStatus == UserState.IDLE.getStateId())){
															   
																List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(receiver);
							    								if(userSessions != null){
							    									Long receiverId = null;
							    									for(UserSession receiverSession :userSessions)
							    									{
							    										if(receiverId == null)
							    											receiverId = receiverSession.getUserId();
							    										
							    										UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																															(receiverSession, incomingCallMessage.toString());
							    										receiverSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
							    									}
							    									
							    									if(receiverId != null) {
							    										com.capv.um.model.User receiver1 = userService.getById(receiverId);
							    										
							    										if(receiver1 != null) {
							    											
							    											receiver1.setCallStatus(UserState.RECEIVING.getStateId());
							    											userService.update(receiver1, false);
							    											
							    										}
							    									}
								    							}
							    								if(user_stat.getLastSigninOs()!=null && user_stat.getLastSigninOs().equals("ios")){
//						    							    		
						    							    			apnsService.pushCallNotification(user_stat.getTokenId(), incomingCallMessage.toString(),user_stat.getClientId());
							    								}else if(user_stat.getLastSigninOs()!=null && user_stat.getLastSigninOs().equals("android")){
							    									fcmService.sendMessage(user_stat.getTokenId(), incomingCallMessage,user_stat.getClientId());
							    								}
															} else {
																
																JsonObject rej_msg=new JsonObject();
																rej_msg.addProperty("id", "callresponse");
																rej_msg.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(user_stat.getCallStatus()));
																rej_msg.addProperty("statusType", "calleeStatus");
																rej_msg.addProperty("callee", receiver);
																
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, rej_msg.toString());
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															}
					    								}
					    							}
					    						}
					    						
											} else {
												
												if(userSession != null) {
													
													if(jsonMessageObj.get("userId") != null && jsonMessageObj.get("callerId") != null) {
														
														String roomNo = jsonMessageObj.get("callerId").getAsString();
														
														UserCallState roomState = callStatesService.callStateByRoom(roomNo);
														
														if(roomState != null && (roomState.getCallStatus() == CallState.STARTED.getStateId()
																|| roomState.getCallStatus() == CallState.INPROGRESS.getStateId())) {
															
															String userName1 = jsonMessageObj.get("userId").getAsString();
															
															com.capv.um.model.User user = userService.getByUserName(userName1, false);
															
															if(user != null && (user.getCallStatus() == UserState.IDLE.getStateId() || 
																				user.getCallStatus() == UserState.RECEIVING.getStateId())) {
																
																Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
																									userSession.getCapvVideoCallingWebSocketClientEndpoint();
																
																jsonMessageObj.remove("id");
																jsonMessageObj.addProperty(  "method" , "joinRoom");
																jsonMessageObj.addProperty("id" , 0);
																jsonMessageObj.addProperty("jsonrpc" , "2.0");
																JsonObject params = new JsonObject();
																params.addProperty("room" , roomNo);
																params.addProperty("user", userName);
																params.addProperty("callMode", jsonMessageObj.get("callMode") != null ? 
																								jsonMessageObj.get("callMode").getAsString() 
																								: "video");
																params.addProperty("istryit", "disable");
																params.addProperty("rec_type", CapvClientUserUtil.getClientConfigProperty(
																		userSession.getClientId(),
																		CapvClientUserConstants.REC_TYPE));
																params.addProperty("dataChannels", true);
																jsonMessageObj.add("params", params);
																
																capvVideoCallingWebSocketClientEndpoint.sendMessage(jsonMessageObj.toString());
																
																Map<String, Object> callData = userSession.initializeCallData();
																callData.put("callType", CapvClientUserConstants.CALL_TYPE_GROUP);
																callData.put("roomNumber", roomNo);
																callData.put("callMode", jsonMessageObj.get("callMode") != null ? 
																							jsonMessageObj.get("callMode").getAsString(): "");
																
																if(jsonMessageObj.get("callMode") != null && 
																		jsonMessageObj.get("callMode").getAsString().equalsIgnoreCase("video")) {
																	
																	Map<String, Object> properties = new HashMap<>();
								    			            		properties.put("roomId", roomNo);
								    			            		properties.put("isFullVideo", true);
								    			            		
								    			            		VideoRecording videoRecordingModel = 
								    			            				videoRecordingService.getVideoRecordingDetailsByMatchingProperties(properties);
								    			            		
																	callData.put("videoFullRecordId", videoRecordingModel.getId());
																}
																
																userSession.setCallInProgress(true);
															} else {
																
																JsonObject rej_msg = new JsonObject();
																
																rej_msg.addProperty("id", "calljoinresponse");
																rej_msg.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(user.getCallStatus()));
																
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, rej_msg.toString());
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															}
															
														} else {
															
															JsonObject rej_msg = new JsonObject();
															
															rej_msg.addProperty("id", "calljoinresponse");
															rej_msg.addProperty("roomstatus", CallState.INPROGRESS.getStateByStateId(roomState.getCallStatus()));
															
															UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																											(userSession, rej_msg.toString());
															userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
														}
													}
												}
											}
									} catch (Exception e) {
										e.printStackTrace();
										
										if(callState != null && callState.getId() != null && isCallerStateUpdated) {
											com.capv.um.model.User caller = userService.getById(userSession.getUserId());
											caller.setCallStatus(UserState.IDLE.getStateId());
											userService.update(caller, false);
										}
									}

    			}
	    	break;
	     	case CapvClientUserConstants.VC_MSG_ADD_PARTICIPANT: {
	    		
	    		com.capv.um.model.User caleeUser = new com.capv.um.model.User();
	    		try{
	    			String calee = jsonMessageObj.get("callee").getAsString();
		    		 caleeUser = userService.getByUserName(calee, false);
	    		}
	    		catch (Exception e) {
	    			e.printStackTrace();
				}
	    		 String host_service="";
	    				if(jsonMessageObj.get("host")==null){
	    					host_service=CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName).getService();
	    					
	    				}else
	    				{	host_service=jsonMessageObj.get("host").getAsString();
	    					
	    				}
	    				boolean isVCServiceConnected = false;
							    		try {
											
								        	if(userSession.getCapvVideoCallingWebSocketClientEndpoint()!=null) {
								        		
								        		isVCServiceConnected = true;
								        	}
									 		
										} catch(Exception e) {
									 		log.error("Error while initializing capv video calling session::", e);
									 	}
							    		
							    		if(!isVCServiceConnected) {
							    			
							    			JsonObject vcConnectErrorMessage = new JsonObject();
		    								
							    			vcConnectErrorMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																				CapvClientUserConstants.WS_MESSAGE_VC_SERVICE_CONNECT_ERROR);
											
											UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																							(userSession, vcConnectErrorMessage.toString());
											userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
											
											return;
							    		}
	    								boolean isCallerStateUpdated = false;
    									try {
					    						if(jsonMessageObj.get("callerId") == null) {
					    							
					    							com.capv.um.model.User caller = userService.getById(userSession.getUserId());
					    							String callee= jsonMessageObj.get("callee").getAsString();
					    							
					    							if(caller.getCallStatus() == UserState.INCALL.getStateId()) {
					    								
						    							Map<String, Object> callerCallData=userSession.getCallData();
						    							if(callerCallData != null && 
						    									callerCallData.get("roomNumber") != null ) {
						    									String roomNumber=(String)callerCallData.get("roomNumber");
															user_stat = userService.getByUserName(callee, false);
															byte userCallStatus = user_stat.getCallStatus();
															if(userCallStatus != 0 && (userCallStatus == UserState.IDLE.getStateId())){
																
																UserCallState addParticipant = callStatesService.getCallLogRoomList(roomNumber);
									    						
																addParticipant.setCalleeList(addParticipant.getCalleeList()+","+callee);
									    							callStatesService.update(addParticipant);
																List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(callee);
																if(userSessions == null && caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("ios")){
						    							    		JsonObject scParams=new JsonObject();
						    										
						    										scParams.addProperty("id", "incommingcall");
							    									scParams.addProperty("roomnumber", roomNumber);
							    									scParams.addProperty("from",userName);
							    									scParams.addProperty("callerName", caller.getName());
							    									scParams.addProperty("callType", CapvClientUserConstants.CALL_TYPE_ONE);
							    									
							    									if(jsonMessageObj.get("callMode") != null)
								    									scParams.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
						    							    		apnsService.pushCallNotification(caleeUser.getTokenId(),scParams.toString(),caleeUser.getClientId());
						    							    	}else if(userSessions == null && caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("android")){
						    							    		JsonObject scParams=new JsonObject();
						    										
						    										scParams.addProperty("id", "incommingcall");
							    									scParams.addProperty("roomnumber", roomNumber);
							    									scParams.addProperty("from1",userName);
							    									scParams.addProperty("callerName", caller.getName());
							    									scParams.addProperty("callType", CapvClientUserConstants.CALL_TYPE_ONE);
							    									
							    									if(jsonMessageObj.get("callMode") != null)
								    									scParams.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
						    							    		fcmService.sendMessage(caleeUser.getTokenId(),scParams,caleeUser.getClientId());
						    							    	}
							    								if(userSessions != null){
							    									Long receiverId = null;
							    									for(UserSession receiverSession :userSessions)
							    									{
							    										if(receiverId == null)
							    											receiverId = receiverSession.getUserId();
							    										
							    										JsonObject scParams=new JsonObject();
							    										
							    										scParams.addProperty("id", "incommingcall");
								    									scParams.addProperty("roomnumber", roomNumber);
								    									scParams.addProperty("from",userName);
								    									scParams.addProperty("callerName", caller.getName());
								    									scParams.addProperty("callType", CapvClientUserConstants.CALL_TYPE_ONE);
								    									
								    								
								    									if(jsonMessageObj.get("callMode") != null)
									    									scParams.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
								    									if(caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("ios")){
								    							    			if(caleeUser.getLastSigninOs().equals(receiverSession.getLastSingedOs())) {
								    							    				apnsService.pushCallNotification(caleeUser.getTokenId(),scParams.toString(),caleeUser.getClientId());
								    							    			}
								    							    		
								    							      	}else if(caleeUser.getLastSigninOs()!=null&&caleeUser.getLastSigninOs().equals("android")){
								    							      		if(caleeUser.getLastSigninOs().equals(receiverSession.getLastSingedOs())) {
								    							      			JsonObject scParams1=new JsonObject();
								    							      			scParams1.addProperty("id", "incommingcall");
								    							      			scParams1.addProperty("roomnumber", roomNumber);
								    							      			scParams1.addProperty("from1",userName);
								    							      			scParams1.addProperty("callerName", caller.getName());
										    									scParams1.addProperty("callType", CapvClientUserConstants.CALL_TYPE_ONE);
										    									if(jsonMessageObj.get("callMode") != null)
										    										scParams1.addProperty("callMode", jsonMessageObj.get("callMode").getAsString());
								    							      			fcmService.sendMessage(caleeUser.getTokenId(),scParams1,caleeUser.getClientId());
								    							      		}
								    							      		
								    							      	}
								    									
								    										UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(
								    												receiverSession, scParams.toString());
								    											userSession.getCapvUserWebSocketMessageProcessor()
								    											.processMessage(userWebSocketMessage);
								    									
							    									}
							    									
							    									if(receiverId != null) {
							    										com.capv.um.model.User receiver = userService.getById(receiverId);
							    										
							    										if(receiver != null) {
							    											receiver.setCallStatus(UserState.RECEIVING.getStateId());
							    											userService.update(receiver, false);
							    										}
							    									}
								    							}
															} else {
																
																JsonObject rej_msg=new JsonObject();
																rej_msg.addProperty("id", "callresponse");
																rej_msg.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(user_stat.getCallStatus()));
																rej_msg.addProperty("statusType", "calleeStatus");
																rej_msg.addProperty("callee", callee);
																
																UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																												(userSession, rej_msg.toString());
																userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
															}
														}
					    							}else {
					    								JsonObject rej_msg=new JsonObject();
														rej_msg.addProperty("id", "callresponse");
														rej_msg.addProperty("statusType", "Your are not in call");
														rej_msg.addProperty("callee", callee);
														
														UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																										(userSession, rej_msg.toString());
														userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
					    								
					    							}
					    							
					    							
						    						
					    						}
					    				} catch (Exception e) {
					    					e.printStackTrace();
					    					if(callState == null || callState.getId() == null) {
					    						if(isCallerStateUpdated) {
						    						com.capv.um.model.User caller = userService.getById(userSession.getUserId());
					    							caller.setCallStatus(UserState.IDLE.getStateId());
					    							userService.update(caller, false);
						    					}
					    					}
					    				}
					    			}
					    		
			break;
	    	
	    	case CapvClientUserConstants.WS_MESSAGE_CALL_REJECT: 
								    	{
								    		Long userId = userSession.getUserId();
								    		com.capv.um.model.User user = userService.getById(userId);
								    		user.setCallStatus(UserState.IDLE.getStateId());
								    		userService.update(user, false);
								    		userSession.setCallInProgress(false);
								    		
								    		if(jsonMessageObj.get("callType") != null && jsonMessageObj.get("roomNumber") != null && 
								    				jsonMessageObj.get("callType").getAsString().equals(CapvClientUserConstants.CALL_TYPE_ONE)) {
								    			
								    			String roomNumber = jsonMessageObj.get("roomNumber").getAsString();
								    			
								    			UserCallState userCallState = callStatesService.callStateByRoom(roomNumber);
								    			
								    			if(userCallState != null) {
								    				String calleeList = userCallState.getCalleeList();
								    				
								    				if(calleeList != null) {
			    										String[] calleeArray = calleeList.split(",");
			    										boolean flag=false;
			    										for(String calleeName :calleeArray) {
			    											if(calleeName.equals(userName)) {
			    												flag=true;
			    											}
			    										}
			    										if(flag) {
			    										for(String calleeName :calleeArray) {
			    											if(!calleeName.equals(userName)) {
			    												List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(calleeName);
			    												if(userSessions != null){
			    													Long receiverId = null;
			    													for(UserSession receiverSession :userSessions)
			    													{
			    														if(receiverId == null)
			    															receiverId = receiverSession.getUserId();
			    														
			    														JsonObject rejectCall = new JsonObject();
			    														
			    														rejectCall.addProperty("id", CapvClientUserConstants.WS_MESSAGE_CALL_REJECT);
			    														rejectCall.addProperty("roomNumber", roomNumber);
			    														rejectCall.addProperty("from",userName);
			    				    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(receiverSession, rejectCall.toString());
			    													 userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
			    													}}
			    											}
			    										}
			    										}
			    									 }
								    			}
								    		}
								    	}
								    	break;
								    	
	    	case CapvClientUserConstants.VC_MSG_RECONNECT: 
												try {	
													CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
								    						getCapvChatUserRequestProcessor(userSession);
								    				//String userName = jsonMessageObj.get("userId").getAsString();
								    				if(jsonMessageObj.get("callerId") == null) {
								    					
								    					String callee= jsonMessageObj.get("callee").getAsString();
								    					String jid= jsonMessageObj.get("room_jid").getAsString();
								    					String[] recv=callee.split(",");
								    					
								    					List<UserCallState> callStList=callStatesService.oneToOneCallsInProgressByCallers(
																											userName, callee, userSession.getClientId());
								    					String roomNumber = callStList.get(callStList.size()).getRoomNo();

								    					//long curr_time = System.currentTimeMillis();
								    					//Date resultdate = new Date(curr_time);
								    					
								    					JsonObject scParams=new JsonObject();
								    					if(userSession.getWebSocketSession()!=null)
								    					{
								    						for(int i=0;i<recv.length;i++){

								    							List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(recv[i]);
								    							if(userSessions != null){

								    								for(UserSession receiverSession :userSessions)
								    								{
								    									scParams.addProperty("id", "reconnecting");
								    									scParams.addProperty("roomnumber", roomNumber);
								    									scParams.addProperty("from",userName);

								    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
								    											(receiverSession, scParams.toString());
								    									userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
								    								}
								    							}
								    						}

								    						Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
								    								userSession.getCapvVideoCallingWebSocketClientEndpoint();

								    						JsonObject obj_msg=new JsonObject();
								    						obj_msg.addProperty("id", "enter");
								    						obj_msg.addProperty("callerId", roomNumber);
								    						obj_msg.addProperty("userId", userName);
								    						if(recv.length>0){
								    							for(int i=0;i<recv.length;i++){	
								    								user_stat = userService.getByUserName(recv[i], false);
								    								byte userCallStatus = user_stat.getCallStatus();
								    								if(userCallStatus != 0 && 
																			(userCallStatus == UserState.IDLE.getStateId() || 
																			userCallStatus == UserState.RECEIVING.getStateId())){

								    									capvChatUserRequestProcessor.sendGroupMessage(jid,CapvClientUserUtil.convertToJsonString(obj_msg)); 

								    									capvVideoCallingWebSocketClientEndpoint.sendMessage(CapvClientUserUtil.convertToJsonString(obj_msg));
								    									String roomName = jid + "@conference." + chatUserConnection.getServiceName();
														    			String  toJid=userName+"@"+chatUserConnection.getServiceName();
															         	long currentTimestamp = System.currentTimeMillis();
															         	OfGroupArchive groupArchiveMessage =new OfGroupArchive();
															         	groupArchiveMessage.setBody(CapvClientUserUtil.convertToJsonString(obj_msg));
															         	groupArchiveMessage.setFromJID(toJid);
															         	groupArchiveMessage.setFromJIDResource("Smack");
															         	groupArchiveMessage.setToJID(roomName);
															         	groupArchiveMessage.setToJIDResource(userName);
															         	groupArchiveMessage.setSentDate(currentTimestamp);
															         	groupArchiveMessage.setMessage_type(MessageType.MESSAGE.getTypeId());
															         	ofGroupArchiveService.save(groupArchiveMessage);
								    								}else{
								    									JsonObject rej_msg=new JsonObject();
								    									obj_msg.addProperty("id", "callresponse");
								    									obj_msg.addProperty("userstatus", UserState.NOTLOGGEDIN.getStateByStateId(user_stat.getCallStatus()));
								    									obj_msg.addProperty("callee", recv[i]);
								    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
								    											(userSession, rej_msg.toString());
								    									userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
								    								}
								    							}
								    						}
								    					}

								    				} else {

								    					if(userSession != null) {
								    						Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
								    								userSession.getCapvVideoCallingWebSocketClientEndpoint();

								    						capvVideoCallingWebSocketClientEndpoint.sendMessage(message.getPayload());
								    					}
								    				}
														
												}
												catch (Exception e) {
													e.printStackTrace();
												}
												break; 
	    	case CapvClientUserConstants.VC_MSG_EXIT: {
		    						Capv_VC_WS_ClientHandler vcwsClientEndpoint = 
																		userSession.getCapvVideoCallingWebSocketClientEndpoint();
		    						
		    						Map<String, Object> callData = userSession.getCallData();
		    						String callInititiatorRoomNumber = (String)callData.get("roomNumber");
		    						
									if(vcwsClientEndpoint != null) {
		    							
										JsonObject obj_msg=new JsonObject();
										obj_msg.addProperty(  "method" , "leaveRoom");
										obj_msg.addProperty("id" , 11);
										obj_msg.addProperty("jsonrpc" , "2.0");
										JsonObject params = new JsonObject();
										params.addProperty("user", userName);
										
										boolean updateStatistics = true;
										if(jsonMessageObj.get("updateStatistics") != null && 
												!jsonMessageObj.get("updateStatistics").getAsBoolean()) {
											updateStatistics = false;
										}
										
										params.addProperty("updateStatistics", updateStatistics);
										if(callData.get("roomNumber") != null)
											params.addProperty("roomName", (String)callData.get("roomNumber"));
										
										obj_msg.add("params", params);
										
										vcwsClientEndpoint.sendExitMessageAndCloseSession(obj_msg.toString());
		    							userSession.setCallInProgress(false);
		    						}
	    							
	    							if(callData != null && callData.get("callType") != null && 
	    									callData.get("callType").equals(CapvClientUserConstants.CALL_TYPE_GROUP) && 
	    									callData.get("groupCallInitiator") != null) {
	    								
	    								String calleeStr = (String)callData.get("callee");
	    								String[] calleeList = calleeStr.split(",");
	    								String roomName = (String)callData.get("roomName");
	    								
	    								boolean foundRoomOccupant = false;
	    								
	    								
	    								for(String calleeName: calleeList){
	    									List<UserSession> calleeUserSessions = UserRegistry.getUserSessionsByUserName(calleeName);
	    									
	    									if(calleeUserSessions != null) {
	    										for(UserSession calleeUserSession :calleeUserSessions) {
	    											Map<String, Object> calleeCallData = calleeUserSession.getCallData();
	    											
	    											if(calleeCallData != null && calleeCallData.get("roomNumber") != null &&
	    													calleeCallData.get("roomNumber").equals(callInititiatorRoomNumber)&&calleeUserSession.getCapvVideoCallingWebSocketClientEndpoint()!=null) {
	    												JsonObject obj_msg=new JsonObject();
    	    												obj_msg.addProperty(  "method" , "pause");
    	    												obj_msg.addProperty("id" , 13);
    	    												obj_msg.addProperty("jsonrpc" , "2.0");
    	    												JsonObject params = new JsonObject();
    	    												params.addProperty("user", calleeUserSession.getUserName());
    	    												obj_msg.add("params", params);
    	    												calleeUserSession.getCapvVideoCallingWebSocketClientEndpoint().sendMessage(obj_msg.toString());
	    												foundRoomOccupant = true;
	    												break;
	    											}
	    										}
	    									}
	    								}
	    								
	    								if(!foundRoomOccupant) {
	    									
	    									for(String calleeName: calleeList){
	    										
	    										JsonObject groupCallDropMessage = new JsonObject();
    											groupCallDropMessage.addProperty("id", "groupCallDrop");
    											groupCallDropMessage.addProperty("roomNumber", callInititiatorRoomNumber);
    											groupCallDropMessage.addProperty("from", userName);
    											groupCallDropMessage.addProperty("roomName", roomName);
    											
    											List<UserSession> calleeUserSessions = UserRegistry.getUserSessionsByUserName(calleeName);
		    									if(calleeUserSessions != null) {
		    										for(UserSession calleeUserSession :calleeUserSessions) {
		    											
		    	    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
		    																							(calleeUserSession, groupCallDropMessage.toString());
		    	    									calleeUserSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
		    	    										
		    										}
		    									}
		    									
		    									com.capv.um.model.User apns_user = userService.getByUserName(calleeName, false);
		    									if(apns_user!=null) {
													if(apns_user.getLastSigninOs().equals("ios"))
    							    					   		apnsService.pushCallNotification(apns_user.getTokenId(),groupCallDropMessage.toString(),apns_user.getClientId());
													else if(apns_user.getLastSigninOs().equals("android")) {
														JsonObject groupCallDropMessage1 = new JsonObject();
														groupCallDropMessage1.addProperty("id", "groupCallDrop");
														groupCallDropMessage1.addProperty("roomNumber", callInititiatorRoomNumber);
														groupCallDropMessage1.addProperty("from", userName);
														groupCallDropMessage1.addProperty("roomName", roomName);
														fcmService.sendMessage(apns_user.getTokenId(),groupCallDropMessage1,apns_user.getClientId());
													}
    							    				}
	    									}
	    								}
	    							}
	    							userSession.clearCallData();
	    							userSession.setCapvVideoCallingWebSocketClientEndpoint(null);
	    							
	    							com.capv.um.model.User user = userService.getById(userSession.getUserId());
	    							user.setCallStatus(UserState.IDLE.getStateId());
	    							userService.update(user, false);
	    							JsonObject callDetails = null;
	    							 
	    							callDetails = jsonMessageObj.get("callDetails") != null ? jsonMessageObj.get("callDetails").getAsJsonObject() : null;
	    							
	    							if(callDetails != null && callDetails.get("callType") != null && 
	    									 callDetails.get("callType").getAsString().equals(CapvClientUserConstants.CALL_TYPE_ONE) && 
	    									 callDetails.get("dropped") != null) {
	    								 
	    								 String room = null;
	    								 if(callDetails.get("roomNumber") != null)
	    									 room = callDetails.get("roomNumber").getAsString();
	    								 
	    								 if(room != null) {
	    									 String calleeList = (callStatesService.getCalleeList(room));
	    									 
	    									 if(calleeList != null) {
	    										String[] calleeArray = calleeList.split(",");
	    										
	    										for(String calleeName :calleeArray) {
	    											
	    											if(!calleeName.equals(userName)) {
	    												
	    												JsonObject scParams=new JsonObject();
														scParams.addProperty("id", "callDrop");
														scParams.addProperty("roomNumber", room);
														scParams.addProperty("from",userName);
														com.capv.um.model.User apns_user = userService.getByUserName(calleeName, false);
			    										if(apns_user!=null) {
			    											if(apns_user.getLastSigninOs().equals("ios"))
			    												apnsService.pushCallNotification(apns_user.getTokenId(),scParams.toString(),apns_user.getClientId());
			    											else if(apns_user.getLastSigninOs().equals("android")) {
			    												JsonObject scParams1=new JsonObject();
			    												scParams1.addProperty("id", "callDrop");
			    												scParams1.addProperty("roomNumber", room);
			    												scParams1.addProperty("from1",userName);
			    											 fcmService.sendMessage(apns_user.getTokenId(),scParams1,apns_user.getClientId());
			    											}
			    												
	    							    					}
	    												List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(calleeName);
	    												if(userSessions != null){
	    													for(UserSession receiverSession :userSessions){
	    				    									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(receiverSession, scParams.toString());
	    														userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    													}
	    													
	    												}
	    											}
	    										}
	    									 }
	    								 }
	    							}
								
    							}
	    						break;
	    	case CapvClientUserConstants.USER_STATE_ID: 
	    							try {	
	    								
					 							if(userSession != null && 
					 										jsonMessageObj.get("status") != null && 
					 										jsonMessageObj.get("userName") != null) {
					 								
					 								String userState = jsonMessageObj.get("status").getAsString();
					 								String userName1 = jsonMessageObj.get("userName").getAsString();
					 								/*if(jsonMessageObj.get("callStatus").getAsString() != null){
					 									System.out.println("you got the point");
					 								}*/
					 								user_stat = userService.getByUserName(userName1, false);

					 								if(user_stat != null) {
					 									user_stat.setLastUpdated(new Date(System.currentTimeMillis()));
						 								user_stat.setCallStatus(UserState.valueOf(userState.toUpperCase()).getStateId());
						 								System.out.println(UserState.valueOf(userState.toUpperCase()).getStateId()+" UserState.valueOf(userState.toUpperCase()).getStateId()");
						 								userService.update(user_stat, false);
					 								}
					 							}
					 					}
	    							catch (Exception e) {
	    								e.printStackTrace();
									}
	    							break; 
	    	case CapvClientUserConstants.VC_PARTICIPANT_TOGGLE_MEDIA: {
	    		
	    		if(jsonMessageObj.get("to") != null && jsonMessageObj.get("from") != null) {
	    			
	    			JsonArray toUsers = jsonMessageObj.get("to").getAsJsonArray();
	    			
	    			Iterator<JsonElement> toUsersItr = toUsers.iterator();
	    			while(toUsersItr.hasNext()) {
	    				String toUser = toUsersItr.next().getAsString();
	    				
	    				List<UserSession> toUserSessions = UserRegistry.getUserSessionsByUserName(toUser);
	    				if(toUserSessions != null) {
	    					for(UserSession toUserSession :toUserSessions) {
		    					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(toUserSession, jsonMessageObj.toString());
		    					toUserSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
		    				}
	    				}
	    			}
	    		}
	    	}
	    	break;
			case CapvClientUserConstants.GET_PARTICIPANT_MEDIA_STATUS: {
				if (jsonMessageObj.get("from") != null) {
					String fromUser = jsonMessageObj.get("from").getAsString();

					List<UserSession> fromUserSessions = UserRegistry.getUserSessionsByUserName(fromUser);

					if (fromUserSessions != null) {
						for (UserSession fromUserSession : fromUserSessions) {
							UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(fromUserSession, jsonMessageObj.toString());
							fromUserSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
						}
					}
				}
			}
	    	break;
	    	
	    	case CapvClientUserConstants.PARTICIPANT_MEDIA_STATUS: {
	    		
	    		if(jsonMessageObj.get("to") != null) {
	    			String toUser = jsonMessageObj.get("to").getAsString();
	    			
	    			List<UserSession> toUserSessions = UserRegistry.getUserSessionsByUserName(toUser);
	    			if(toUserSessions != null) {
	    				for(UserSession toUserSession :toUserSessions) {
	    					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(toUserSession, jsonMessageObj.toString());
	    					toUserSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    				}
	    			}
	    		}
	    	}
	    	break;
	    	
	    	case "onIceCandidate":
	    	{
	    		try{
	    			Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
							userSession.getCapvVideoCallingWebSocketClientEndpoint();
		    		JsonObject candidate = jsonMessageObj.get("candidate").getAsJsonObject();
		    		JsonObject obj_msg = new JsonObject();
		    		obj_msg.addProperty("method" , "onIceCandidate");
		    		obj_msg.addProperty("id" , 3);
		    		JsonObject params = new JsonObject();
		    		params.add("candidate", candidate.get("candidate"));
		    		params.add("sdpMLineIndex" , candidate.get("sdpMLineIndex"));
		    		params.add("sdpMid" , candidate.get("sdpMid"));
		    		params.addProperty("endpointName", jsonMessageObj.get("userId").getAsString());
		    		obj_msg.add("params", params);
		    		capvVideoCallingWebSocketClientEndpoint.sendMessage(obj_msg.toString());
	    		}
	    		catch(Exception e){
	    			System.out.println(e +"Exception");
	    		}
	    	}
	    	break;
	    	case CapvClientUserConstants.WS_MESSAGE_RECORD_START:
	    	{
	    		try{
	    			String host_service="";
	    			
		    		if(jsonMessageObj.get("host")==null)
		    				host_service=CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName).getService();
    				else
    						host_service=jsonMessageObj.get("host").getAsString();
		    		
	    			
	    			JsonObject obj_msg=new JsonObject();
					obj_msg.addProperty(  "method" , "record");
					obj_msg.addProperty("id" , 13);
					obj_msg.addProperty("jsonrpc" , "2.0");
					
					if(jsonMessageObj.get("callType").getAsString().equals("one-one")) {
					
	    				Map<String, Object> userCallData = userSession.getCallData();
	    				
	    				if(userCallData.get("roomNumber")!=null){
	    					String roomNumber=(String) userCallData.get("roomNumber");
	    						UserCallState groupCall = callStatesService.getCallLogRoomList(roomNumber);
	    						String calleeList=groupCall.getCalleeList();
	    						groupCall.setDelete_vid_flag(3);
	    						callStatesService.update(groupCall);
	    						String[] oneoneParticipants = calleeList.split(",");
	    						
	    						for(String oneoneParticipant :oneoneParticipants){    
	    							List<UserSession> getOneOneSessions = UserRegistry.getUserSessionsByUserName(oneoneParticipant);
	    					
	    							if(getOneOneSessions != null) {
	    								for(UserSession participantSession: getOneOneSessions) {
	    									if(participantSession.isCallInProgress()) {
	    										Map<String, Object> participantCallData = participantSession.getCallData();
	    										if(participantCallData != null && participantCallData.get("roomNumber") != null && 
	    												((String)participantCallData.get("roomNumber")).equals(groupCall.getRoomNo())&&
	    												participantSession.getCapvVideoCallingWebSocketClientEndpoint()!=null) {
	    											JsonObject params = new JsonObject();
	    											params.addProperty("user", oneoneParticipant);
	    											obj_msg.add("params", params);
	    											participantSession.getCapvVideoCallingWebSocketClientEndpoint().sendMessage(obj_msg.toString());
	    										}
	    									}
	    								}
	    							}
	    						}
	    				}
	    			}else {
	    				
	    					String roomJid=jsonMessageObj.get("room_jid").getAsString()+"@conference." + host_service;
	    					UserCallState groupCall = callStatesService.getLastActiveGroupCallByJid(roomJid);
	    					groupCall.setDelete_vid_flag(3);
	    					callStatesService.update(groupCall);
	    					if(groupCall != null) {
						
	    						String groupParticipantsStr = groupCall.getCalleeList();
	    						String[] groupParticipants = groupParticipantsStr.split(",");
					
	    						for(String groupParticipant :groupParticipants){         
	    							List<UserSession> groupParticipantSessions = 
											UserRegistry.getUserSessionsByUserName(groupParticipant);
							
	    							if(groupParticipantSessions != null) {
	    								for(UserSession participantSession: groupParticipantSessions) {
									
	    									if(participantSession.isCallInProgress()) {
										
	    										Map<String, Object> participantCallData = participantSession.getCallData();
										
	    										if(participantCallData != null && participantCallData.get("roomNumber") != null && 
												((String)participantCallData.get("roomNumber")).equals(groupCall.getRoomNo())&&participantSession.getCapvVideoCallingWebSocketClientEndpoint()!=null) {
											JsonObject params = new JsonObject();
											params.addProperty("user", groupParticipant);
											obj_msg.add("params", params);
											participantSession.getCapvVideoCallingWebSocketClientEndpoint().sendMessage(obj_msg.toString());
										}
									}
								}
							
							}
						}
					}
	    			}
					JsonObject messageToSend = new JsonObject();
					messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_VIDEO_RECORD_STATUS);
					messageToSend.addProperty("action", "start");	
					messageToSend.addProperty("status", VideoRecordingStatus.SUCCESS.getStatus());
					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToSend.toString());
					userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
					
	    		}
	    		catch(Exception e){
	    			System.out.println(e +"Exception");
	    		}
	    	}
	    	break;
	    	case CapvClientUserConstants.WS_MESSAGE_RECORD_STOP:
	    	{
	    		try{
	    			String host_service="";
    				if(jsonMessageObj.get("host")==null)
	    				host_service=CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName).getService();
    				else
						host_service=jsonMessageObj.get("host").getAsString();
    				JsonObject obj_msg=new JsonObject();
					obj_msg.addProperty(  "method" , "pause");
					obj_msg.addProperty("id" , 13);
					obj_msg.addProperty("jsonrpc" , "2.0");
    				
	    			if(jsonMessageObj.get("callType").getAsString().equals("one-one")) {
	    				Map<String, Object> userCallData = userSession.getCallData();
	    				if(userCallData.get("roomNumber")!=null){
	    					String roomNumber=(String) userCallData.get("roomNumber");
	    					UserCallState groupCall = callStatesService.getCallLogRoomList(roomNumber);
    						String calleeList=groupCall.getCalleeList();
    						String[] oneoneParticipants = calleeList.split(",");
    						for(String oneoneParticipant :oneoneParticipants){    
    							List<UserSession> getOneOneSessions = UserRegistry.getUserSessionsByUserName(oneoneParticipant);
    					
    							if(getOneOneSessions != null) {
    								for(UserSession participantSession: getOneOneSessions) {
    									if(participantSession.isCallInProgress()) {
    										Map<String, Object> participantCallData = participantSession.getCallData();
    										if(participantCallData != null && participantCallData.get("roomNumber") != null && 
    												((String)participantCallData.get("roomNumber")).equals(groupCall.getRoomNo())&&
    												participantSession.getCapvVideoCallingWebSocketClientEndpoint()!=null) {
    											JsonObject params = new JsonObject();
    											params.addProperty("user", oneoneParticipant);
    											obj_msg.add("params", params);
    											participantSession.getCapvVideoCallingWebSocketClientEndpoint().sendMessage(obj_msg.toString());
    										}
    									}
    								}
    							}
    						}
    						
	    					/*List<UserSession> getOneOneSessions = UserRegistry.getUserSessionsByUserName(jsonMessageObj.get("callee").getAsString());
	    				
	    					if(userCallData != null && userSession.getCapvVideoCallingWebSocketClientEndpoint()!=null&& getOneOneSessions!=null) {
								
	    						JsonObject params = new JsonObject();
								
	    						params.addProperty("user", userSession.getUserName());
								
	    						obj_msg.add("params", params);
								
	    						userSession.getCapvVideoCallingWebSocketClientEndpoint().sendMessage(obj_msg.toString());
	    						if(getOneOneSessions.get(0).getCapvVideoCallingWebSocketClientEndpoint()!=null) {
	    						getOneOneSessions.get(0).getCapvVideoCallingWebSocketClientEndpoint().sendMessage(obj_msg.toString());
	    						}
	    					
						
	    					}*/
	    				}
	    			}else {

		    		String roomJid=jsonMessageObj.get("room_jid").getAsString()+"@conference." + host_service;;
	    			
	    			
		    		
		    		UserCallState groupCall = callStatesService.getLastActiveGroupCallByJid(roomJid);
					
					if(groupCall != null) {
						
						String groupParticipantsStr = groupCall.getCalleeList();
						String[] groupParticipants = groupParticipantsStr.split(",");
					
					for(String groupParticipant :groupParticipants){         
							List<UserSession> groupParticipantSessions = 
											UserRegistry.getUserSessionsByUserName(groupParticipant);
							
							if(groupParticipantSessions != null) {
								for(UserSession participantSession: groupParticipantSessions) {
									
									if(participantSession.isCallInProgress()) {
										
										Map<String, Object> participantCallData = participantSession.getCallData();
										
										if(participantCallData != null && 
												participantCallData.get("roomNumber") != null && 
												((String)participantCallData.get("roomNumber")).equals(groupCall.getRoomNo())&&participantSession.getCapvVideoCallingWebSocketClientEndpoint()!=null) {
											JsonObject params = new JsonObject();
											params.addProperty("user", groupParticipant);
											obj_msg.add("params", params);
											participantSession.getCapvVideoCallingWebSocketClientEndpoint().sendMessage(obj_msg.toString());
										}
									}
								}
							
							}
						}
					}
	    			}
	    			
	    			JsonObject messageToSend = new JsonObject();
					messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_VIDEO_RECORD_STATUS);
					messageToSend.addProperty("action", "stop");
					messageToSend.addProperty("status", VideoRecordingStatus.SUCCESS.getStatus());
					UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToSend.toString());
					userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
	    		}
	    		catch(Exception e){
	    			System.out.println(e +"Exception");
	    		}
	    	}
	    	break;
	    	case "changeBitRate":
	    	{
	    		try{
	    			Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = 
							userSession.getCapvVideoCallingWebSocketClientEndpoint();
	    				String s=message.getPayload();
	    				s=s.replace("a=mid:video", "a=mid:video\\r\\n b=AS:150\\r\\n");
	    				s=s.replace("a=mid:audio", "a=mid:audio\\r\\n b=AS:30\\r\\n");
		    		   capvVideoCallingWebSocketClientEndpoint.sendMessage(s);
	    		}
	    		catch(Exception e){
	    			log.debug(e.getMessage());
	    		}
	    	}
	    	break;
	    	case CapvClientUserConstants.VC_MSG_ADD_GROUP_PARTICIPANT:{
	    		if(jsonMessageObj.has("groupName") && !jsonMessageObj.get("groupName").getAsString().isEmpty()
	    				&& jsonMessageObj.has("userName") && !jsonMessageObj.get("userName").getAsString().isEmpty()) {
	    			String groupName = jsonMessageObj.get("groupName").getAsString();
	    			String adminUser = jsonMessageObj.get("userName").getAsString();
		    		addGroupParticipent(userSession, adminUser, groupName+"@conference."+chatUserConnection.getServiceName());
	    		}
	    	}
	    	break;
	    	case CapvClientUserConstants.VC_MSG_SCHEDULE_MESSAGE_RESPONSE:{
	    		if(jsonMessageObj.has("groupName") && !jsonMessageObj.get("groupName").getAsString().isEmpty()
	    				&& jsonMessageObj.has("userName") && !jsonMessageObj.get("userName").getAsString().isEmpty()) {
	    						chatGroupMessage(jsonMessageObj, session, chatUserConnection, userSession);
	    		}
	    	}
	    	
	    	
	    	break;
			default: 
				 {
					 if(userSession != null) {
						
						 Capv_VC_WS_ClientHandler vcwsClientEndpoint = 
													userSession.getCapvVideoCallingWebSocketClientEndpoint();
						 if(encryption.equals("enabled")) {
							 vcwsClientEndpoint.sendMessage(CapvUtil.decrypt(key, initVector,message.getPayload()));
				        }else {
				        		vcwsClientEndpoint.sendMessage(message.getPayload());
				        }
						 
					 }
				 }
			 break;
	    		}
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * This method invoked after user successfully established websocket connection between server and client
	 * <p>
	 * This method used to initialize chat service and video calling service connections 
	 * after user has been successfully authenticated and established websocket connection with capv
	 * 
	 * @param session	This parameter refers to the WebSocketSession of the connected user 
	 * 					and provides the information about connected user which is required 
	 * 					to initialize the connections with chat and video calling services
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		KeyGenerator keyGenerator = KeyGenerator.getInstance(CapvClientUserConstants.ALGORITHM);
		keyGenerator.init(CapvClientUserConstants.AES_128);
		
		if(session.getAttributes().get("userName") != null && 
				session.getAttributes().get("password") != null &&
				session.getAttributes().get("clientId") != null) {
			
			String userName = (String)session.getAttributes().get("userName");
			String password = (String)session.getAttributes().get("password");
			Long clientId = (Long)session.getAttributes().get("clientId");
			Long userId = (Long)session.getAttributes().get("userId");
			//System.out.println("Attributes :"+session.getAttributes());
			String lastSignInOs = (String)session.getAttributes().get("lastSigninOs");
			String tokenId = (String)session.getAttributes().get("tokenId");
			String accessToken = (String)session.getAttributes().get("accessToken");
			
			tokenId = tokenId.replace("<", "");
			tokenId = tokenId.replace(">", "");
			
				//Generate Key
    				SecretKey key = keyGenerator.generateKey();
    				//Initialization vector
    				SecretKey IV = keyGenerator.generateKey();
    				String key_str= Base64.getEncoder().encodeToString(key.getEncoded()); 
            		String IV_str=Base64.getEncoder().encodeToString(IV.getEncoded());
            		
			String requestSource = null;
			
			if(session.getAttributes().get("requestSource") != null)
				requestSource = (String)session.getAttributes().get("requestSource");
			
			CapvUserWebSocketMessageProcessor capvUserWebSocketMessageProcessor = new CapvUserWebSocketMessageProcessor();
			UserSession userSession;
			if(lastSignInOs!=null) {
			userSession = new UserSession(userName, userId, clientId, session, capvUserWebSocketMessageProcessor,lastSignInOs,key_str,IV_str);
			UserRegistry.addUserSession(userName, session.getId(), userSession);
			}else {
				 userSession = new UserSession(userName, userId, clientId, session, capvUserWebSocketMessageProcessor,"web",key_str,IV_str);
				UserRegistry.addUserSession(userName, session.getId(), userSession);
			}
			com.capv.um.model.User user = userService.getById(userId, true);
			// update user table 
			if(user != null && (user.getCallStatus() == 0 ||
					user.getCallStatus() == UserState.NOTLOGGEDIN.getStateId())) {
				user.setCallStatus(UserState.IDLE.getStateId());
				user.setLastSigninOs(lastSignInOs);
				user.setTokenId(tokenId);
				userService.update(user, false);
			}
			
        		
    			JsonObject messageToSend = new JsonObject();
				messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GET_KYES);
				messageToSend.addProperty("key", key_str);
				messageToSend.addProperty("IV", IV_str);
				//;
    			UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, CapvUtil.encrypt(accessToken, accessToken.substring(4, 20), messageToSend.toString()));
			userSession.getCapvUserWebSocketMessageProcessor().processMessage1(userWebSocketMessage);
			
			if(requestSource == null || 
					!requestSource.equals(CapvClientUserConstants.CLIENT_REQUEST_SOURCE_ANDROID_APP)) {
				
				try {
					
					int chatServerPort = CapvClientUserConstants.XMPP_SERVER_DEFAULT_PORT;
					
					String chatServiceHost = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_HOST_NAME_KEY);
					try {
						chatServerPort = Integer.parseInt(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_PORT_KEY));
					} catch (Exception e) {
						e.printStackTrace();
					}
					String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
					
					CapvChatClientManager capvChatClientManager = CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName);
					
					CapvChatClientConfiguration capvChatClientConfiguration = CapvChatClientConfiguration
																					.connectionConfigurationBuilder()
																						.setHost(chatServiceHost)
																						.setPort(chatServerPort)
																						.setService(chatServerServiceName)
																						.setUserName(userName)
																						.setPassword(password)
																						.setWebSocketSessionId(session.getId())
																						.setUserService(userService)
																						.build();
					
					if(capvChatClientManager == null) {
						
						capvChatClientManager = new CapvChatClientManager(capvChatClientConfiguration);
						capvChatClientManager.initializeConnection();
						
					} else {
						if(CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName).isChatConnectionAlive()) {
							userSession.setChatClientAuthStatus(ChatClientAuthStatus.SUCCESS);
							
							CapvChatUserRequestProcessor capvChatUserRequestProcessor = CapvChatClientManagerRegistry
																							.getCapvChatClientManagerByUser(userName)
																							.getCapvChatUserRequestProcessor();
							 
							capvChatUserRequestProcessor.changeUserPresence(Presence.Mode.available.toString(), "Online");
							userSession.setPresence(Presence.Mode.available.toString());
		
						} else {
							try {
								capvChatClientManager.reconnectChatConnection();
							} catch (Exception e) {
								//If chat client reconnect fails, Try to connect with the updated configuration built with the updated user details
								CapvChatClientManagerRegistry.removeUserChatManagerConnection(userName);
								capvChatClientManager = new CapvChatClientManager(capvChatClientConfiguration);
								capvChatClientManager.initializeConnection();
							}
						}
					}
					
					if(capvChatClientManager != null) {
						try {
							ChatUserUtil.checkAndUpdateUserVCard(capvChatClientManager.getChatUserConnection(), user);
						} catch (Exception e){}
					}
						
					/*Timer webSocketPingTImer = new Timer();
					webSocketPingTImer.schedule(new CapvWebSocketPingTimerTask(userSession), 100000, 1000000);
			        userSession.setWebSocketPingTimer(webSocketPingTImer);*/
				} catch (Exception e) {
	        		log.error("Error while initializing capv chat session::", e);
	        		userSession.setChatClientAuthStatus(ChatClientAuthStatus.FAIL);
	        		userSession.setVideoCallingClientConnectStatus(VideoCallingClientConnectStatus.FAIL);
	        		CloseStatus closeStatus = new CloseStatus(1011, CapvUtil.environment.getProperty("message.connectionEstablishServerError"));
	        		session.close(closeStatus);
	        		return;
				}
				
			} else {
				userSession.setChatClientAuthStatus(ChatClientAuthStatus.SUCCESS);
				userSession.setConnectionSource(CapvClientUserConstants.CLIENT_REQUEST_SOURCE_ANDROID_APP);
			}
			
		}
	}

	/**
	 * 
	 * This method invoked after websocket connection get disconnected with the user client
	 * 
	 * @param session	This is the first parameter of the method which refers the user websocket session which is get disconnected.
	 * @param status	This is the second parameter of the method which refers the status code of the websocket connection closed 
	 * 					which is used to check whether websocket closed normally or closed due to error
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		
		String userName = UserRegistry.getUserNameBySessionId(session.getId());
		com.capv.um.model.User user = userService.getByUserName(userName, false);
		user.setLogged_in_state((byte)1);

		
		//here we are removing session of user from user registry
		System.out.println("Session closed. Close status::"+status.getCode()+"\t Close Reason::"+status.getReason());
	/*	String userName = UserRegistry.getUserNameBySessionId(session.getId());
		com.capv.um.model.User user = userService.getByUserName(userName, false);*/
		/** code written not to remove session of IOS users when app is minimized or killed**/
	/*	if("ios".equalsIgnoreCase(user.getLastSigninOs())){
			if("logout".equalsIgnoreCase(status.getReason())){
		          UserRegistry.removeUserSession(session.getId(), userService, 
										callStatesService, videoRecordingService,status);
			}
		}
		else{*/
			 UserRegistry.removeUserSession(session.getId(), userService, 
						callStatesService, videoRecordingService,status);
	/*	}*/
		
	}
	
	@Override
	protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
		UserSession userSession = UserRegistry.getUserSessionBySessionId(session.getId());
		userSession.setLastPongMessageReceived(System.currentTimeMillis());
	}
	

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if (exception instanceof IOException) {
            // if connection lost, call this
			session.close();
        }
	}
	

	
	/*public List<UserChatRoom> joinedTRooms(String userName,String serviceName,Long clientId,CapvChatUserRequestProcessor capvChatUserRequestProcessor) throws NoResponseException, XMPPErrorException, NotConnectedException {
	    	
	    	List<UserChatRoom> userChatRooms = new ArrayList<>();
	    		Integer propertyValue = Integer.parseInt(CapvClientUserUtil.getClientConfigProperty(clientId, "capv.video.groupsize"));
	    		String fetchUserName=userName;
	    		List<String> userGroups=groupService.getUserGroup(fetchUserName);
	    		for(int i=0;i<userGroups.size();i++) {
	    		
	    			UserChatRoom userChatRoom = null;
	    		 	Map<String, String> userChatRoomOccupants = new HashMap<>();
	    			userChatRoom = new UserChatRoom();
	        		userChatRoom.setName(userGroups.get(i));
	        		userChatRoom.setJid(userGroups.get(i)+"@conference."+serviceName);

    				try {
						capvChatUserRequestProcessor.joinRoom(userGroups.get(i)+"@conference."+serviceName);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		//userChatRoom.setProgress(false);
	        		UserCallState groupCall =callStatesService.getLastActiveGroupCallByJid(userGroups.get(i)+"@conference."+serviceName);
					if(groupCall!=null) {
						userChatRoom.setProgress(true);
					}
	    			if(userGroups.get(i)!=null) {
	    				
	    				List<String> grpAdminMember=groupService.getGroupUsers(userGroups.get(i));
	    				if(grpAdminMember!=null) {
	    					for(int j=0;j<grpAdminMember.size();j++) {
	    						if(userName.equals(grpAdminMember.get(j))) {
   								userChatRoom.setAdmin(true);
   							}else {
   							userChatRoom.setAdmin(false);
   							String temp=grpAdminMember.get(j).substring(0, grpAdminMember.get(j).indexOf("@"));
   							userChatRoomOccupants.put(temp, ChatUserUtil.getUserFullName(temp));

   							}
	    					}
	    				}
	    				
	    			}
	    			if(userChatRoomOccupants.size()>propertyValue) {
		        		userChatRoom.setVideoEnable(false);
		        	}else {
		        		userChatRoom.setVideoEnable(true);
		        	}
	    			userChatRoom.setOccupants(userChatRoomOccupants);
	        		userChatRoom.setOccupantsLength(userChatRoomOccupants.size());
	        		userChatRooms.add(userChatRoom);
		    		
	    		}
		
	    	  	log.debug("User Chat Rooms  :{}",userChatRooms);
		
	    	return userChatRooms;
	    	
	    }
	 
	 public UserChatRoom getTUserChatRoomDetails(String roomJid,String userName,Long cliendId) throws Exception {
	    	
	    	UserChatRoom userChatRoom = null;
	    		Map<String, String> userChatRoomOccupants = new HashMap<>();
	    	 	Integer propertyValue = Integer.parseInt(CapvClientUserUtil.getClientConfigProperty(cliendId, "capv.video.groupsize"));
	    		
	    		    String roomName=roomJid.substring(0, roomJid.indexOf("@"));
				userChatRoom = new UserChatRoom();
				userChatRoom.setName(roomJid.substring(0, roomJid.indexOf("@")));
				userChatRoom.setJid(roomJid);
				
					List<String> grpAdminMember=groupService.getGroupUsers(roomName);
					if(grpAdminMember!=null) {
						for(int j=0;j<grpAdminMember.size();j++) {
							String temp=grpAdminMember.get(j).substring(0, grpAdminMember.get(j).indexOf("@"));
							if(userName.equals(temp)) {
								userChatRoom.setAdmin(true);
							}else {
								userChatRoom.setAdmin(false);
								userChatRoomOccupants.put(temp, ChatUserUtil.getUserFullName(temp));
							}
						}
					}
					userChatRoom.setOccupants(userChatRoomOccupants);
	        			userChatRoom.setOccupantsLength(userChatRoomOccupants.size());
	        			if(userChatRoomOccupants.size()>propertyValue) {
			        		userChatRoom.setVideoEnable(false);
			        	}else {
			        		userChatRoom.setVideoEnable(true);
			        	}
	    	return userChatRoom;
	    }*/
	 public void deleteGroupMember(JsonArray friendsList,String roomname,UserSession userSession,XMPPTCPConnection chatUserConnection,String userName) {
		 try {
			 CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);

			 JsonObject createRoomResponse = new JsonObject();
			 createRoomResponse.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,CapvClientUserConstants.WS_MESSAGE_DELETE_GROUP_FRIENDS);
			 try {
				 for (JsonElement friend : friendsList) {
					 capvChatUserRequestProcessor.deleteMemberFromRoom(roomname, friend.getAsString());
					 List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(friend.getAsString().substring(0, friend.getAsString().indexOf("@")));
					 if (userSessions != null) {
						 Long receiverId = null;
						 for (UserSession receiverSession : userSessions) {
							 if (receiverId == null)
								 receiverId = receiverSession.getUserId();
								 JsonObject messageToSend = new JsonObject();
								 messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_DELETE_GROUP_FRIENDS);
								 messageToSend.addProperty("message", "Member has been removed From Group " + roomname);
								 messageToSend.add("removedMemberList", friendsList);
								 messageToSend.addProperty("status", "success");
								 UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(receiverSession, messageToSend.toString());
								 receiverSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
							 
						 }
					 }
					 UserRoomRequest deleteRoomRequest=userRoomRequestService.getUniqeRecord(friend.getAsString(),roomname + "@conference." + chatUserConnection.getServiceName());
					 if (deleteRoomRequest!=null) {
						 userRoomRequestService.delete(deleteRoomRequest);
					 }
				 }
			 } catch (Exception e) {
				 e.printStackTrace();
				 log.debug("Exception in deleting request room request in user_room_request", e.getMessage());
			 }
			
			
			 createRoomResponse.addProperty("message", "Deleted Friends successfully to Group " + roomname);
			
			 createRoomResponse.addProperty("room", roomname);

			 Iterator<JsonElement> friendList = friendsList.iterator();

			 while (friendList.hasNext()) {

				 String friendJid = friendList.next().getAsString();
				 String friendName = friendJid.substring(0, friendJid.indexOf("@"));

				 com.capv.um.model.User friend = userService.getByUserName(friendName, false);

				 if (friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("ios")) {
					 JsonObject groupRequestAPNSMessage = new JsonObject();
					 groupRequestAPNSMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,CapvClientUserConstants.WS_MESSAGE_DELETE_GROUP_FRIENDS);
					 groupRequestAPNSMessage.addProperty("room", roomname);
					 apnsService.pushCallNotification(friend.getTokenId(), groupRequestAPNSMessage.toString(),
							 friend.getClientId());
				 } else if (friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("android")) {
					 JsonObject groupRequestAPNSMessage = new JsonObject();
					 groupRequestAPNSMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,CapvClientUserConstants.WS_MESSAGE_DELETE_GROUP_FRIENDS);
					 groupRequestAPNSMessage.addProperty("room", roomname);
					 fcmService.sendMessage(friend.getTokenId(), groupRequestAPNSMessage, friend.getClientId());
				 }
			 }
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
	 }
	 public void transferPrivilage(String roomname,UserSession userSession,XMPPTCPConnection chatUserConnection,String userName,String transferOwnerName) {
		 CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
		 try {
	
			 String serviceName="@"+chatUserConnection.getServiceName();
				capvChatUserRequestProcessor.transferPrivilageMemberFromRoom(roomname, transferOwnerName+serviceName, userName+serviceName);
					
			
			JsonObject transferPriviliage=new JsonObject();
			transferPriviliage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,CapvClientUserConstants.WS_MESSAGE_TRANSFER_PRIVILEGE);
			transferPriviliage.addProperty("message","Admin privileges transferred  successfully");
			transferPriviliage.addProperty("status", "success");
			UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, transferPriviliage.toString());
			userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
			
			JsonObject transferPriviliage1=new JsonObject();
			transferPriviliage1.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,CapvClientUserConstants.WS_MESSAGE_TRANSFER_PRIVILEGE);
			transferPriviliage1.addProperty("message","Admin privileges transferred  to");
			transferPriviliage1.addProperty("status", "success");
			transferPriviliage1.addProperty("roomname", roomname);
			transferPriviliage1.addProperty("transferOwnerName", transferOwnerName);
			List<UserSession> toUserSessions = UserRegistry.getUserSessionsByUserName(transferOwnerName);
			if(toUserSessions != null) {
				for(UserSession toUserSession :toUserSessions) {
					UserWebSocketMessage remoteWebSocketMessage = new UserWebSocketMessage(toUserSession, transferPriviliage1.toString());
					toUserSession.getCapvUserWebSocketMessageProcessor().processMessage(remoteWebSocketMessage);
				}
			}
			List<UserSession> fromUserSessions = UserRegistry.getUserSessionsByUserName(userName);
			if(fromUserSessions != null) {
				for(UserSession fromUserSession :fromUserSessions) {
					UserWebSocketMessage remoteWebSocketMessage = new UserWebSocketMessage(fromUserSession, transferPriviliage1.toString());
					fromUserSession.getCapvUserWebSocketMessageProcessor().processMessage(remoteWebSocketMessage);
				}
			}
		} catch (XMPPErrorException |SmackException e) {
			log.debug(e.getMessage());
		} 
	 }
	 
	private void addGroupParticipent(UserSession userSession, String userName, String groupName) {
		boolean isVCServiceConnected = false;
		try {
			if (userSession.getCapvVideoCallingWebSocketClientEndpoint() != null) {
				isVCServiceConnected = true;
			}
			if (!isVCServiceConnected) {
				JsonObject vcConnectErrorMessage = new JsonObject();
				vcConnectErrorMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY,
						CapvClientUserConstants.WS_MESSAGE_VC_SERVICE_CONNECT_ERROR);
				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, vcConnectErrorMessage.toString());
				userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
				return;
			}
			UserCallState groupCall = callStatesService.getLastActiveGroupCallByJid(groupName);
			if (groupCall != null) {
				String[] calleList = groupCall.getCalleeList().split(",");
				String roomNumber = groupCall.getRoomNo();
				String callMode = groupCall.getCallMode() == 1 ? "video" : "audio";
				String callType = groupCall.getCallType();
				for (String p : calleList) {
					com.capv.um.model.User participant = userService.getByUserName(p, false);
					byte userCallStatus = participant.getCallStatus();
					if (userCallStatus != 0 && (userCallStatus == UserState.IDLE.getStateId())) {
						List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(p);
						if (userSessions == null && participant.getLastSigninOs() != null && participant.getLastSigninOs().equals("ios")) {
							JsonObject inCommingCall = getIncomingCallJsonObject(roomNumber, userName, callType, callMode,groupName);
							apnsService.pushCallNotification(participant.getTokenId(), inCommingCall.toString(), participant.getClientId());
						} else if (userSessions == null && participant.getLastSigninOs() != null && participant.getLastSigninOs().equals("android")) {
							JsonObject inCommingCall = getIncomingCallJsonObject(roomNumber, userName, callType, callMode, groupName);
							fcmService.sendMessage(participant.getTokenId(), inCommingCall, participant.getClientId());
						}
						if (userSessions != null) {
							Long receiverId = null;
							for (UserSession receiverSession : userSessions) {
								if (receiverId == null)
									receiverId = receiverSession.getUserId();

								JsonObject inCommingCall = getIncomingCallJsonObject(roomNumber, userName, callType, callMode, groupName);

								if (participant.getLastSigninOs() != null && participant.getLastSigninOs().equals("ios")) {
									if (participant.getLastSigninOs().equals(receiverSession.getLastSingedOs())) {
										apnsService.pushCallNotification(participant.getTokenId(), inCommingCall.toString(),
												participant.getClientId());
									}

								} else if (participant.getLastSigninOs() != null && participant.getLastSigninOs().equals("android")) {
									if (participant.getLastSigninOs().equals(receiverSession.getLastSingedOs())) {
										fcmService.sendMessage(participant.getTokenId(), inCommingCall, participant.getClientId());
									}
								}
								UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(receiverSession, inCommingCall.toString());
								userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
							}
							if (receiverId != null) {
								com.capv.um.model.User receiver = userService.getById(receiverId);
								if (receiver != null) {
									receiver.setCallStatus(UserState.RECEIVING.getStateId());
									userService.update(receiver, false);
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Error while initializing capv video calling session::", e);
		}
	}
	
	private JsonObject getIncomingCallJsonObject(String roomNumber, String userName, String callType, String callMode, String roomName) {
		JsonObject inCommingCallJson = new JsonObject();
		inCommingCallJson.addProperty("id", "incommingcall");
		inCommingCallJson.addProperty("roomnumber", roomNumber);
		inCommingCallJson.addProperty("from1", userName);
		inCommingCallJson.addProperty("callerName", userName);
		inCommingCallJson.addProperty("callType", callType);
		inCommingCallJson.addProperty("callMode", callMode);
		inCommingCallJson.addProperty("roomName", roomName.split("@")[0]);
		return inCommingCallJson;
	}
	
	public void chatGroupMessage(JsonObject jsonMessageObj, WebSocketSession session, XMPPTCPConnection chatUserConnection, UserSession userSession)
			throws Exception {

		String userName = userSession.getUserName();

		String room = jsonMessageObj.get("groupName").getAsString();
		String msg = "";
		try {
			msg = jsonMessageObj.get("message").getAsString();
			if (msg.isEmpty()) {
				JsonObject errorToUser = new JsonObject();
				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_SEND);
				errorToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ERROR, CapvClientUserConstants.WS_MESSAGE_CHAT_EMPTY_MESSAGE);
				UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession,
						CapvClientUserUtil.convertToJsonString(errorToUser));
				userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);

			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
	
		String roomName = room + "@conference." + chatUserConnection.getServiceName();
		String toJid = userName + "@" + chatUserConnection.getServiceName();

		long currentTimestamp = System.currentTimeMillis();
		OfGroupArchive groupArchiveMessage = new OfGroupArchive();
		groupArchiveMessage.setBody(msg);
		groupArchiveMessage.setFromJID(toJid);
		groupArchiveMessage.setFromJIDResource("Smack");
		groupArchiveMessage.setToJID(roomName);
		groupArchiveMessage.setToJIDResource(userName);
		groupArchiveMessage.setSentDate(currentTimestamp);
		groupArchiveMessage.setIsEdited(0);
		groupArchiveMessage.setIsDeleted(0);

		groupArchiveMessage.setMessage_type(MessageType.MESSAGE.getTypeId());
		Long msgId = ofGroupArchiveService.save(groupArchiveMessage);
	
		String msgType = "1";
		capvChatUserRequestProcessor.sendGroupMessage(room, msgId + ":" + msgType + ":" + msg);

		JsonObject messageToUser = new JsonObject();
		messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_SEND);
		messageToUser.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_ID_KEY, msgId);

		UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(userSession, messageToUser.toString());

		userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);

		CapvChatClientMessagesDeviceSyncHandler.syncChatMessageToSenderDevices(jsonMessageObj, userSession.getUserName(), session.getId());

		String service = CapvClientUserUtil.getClientConfigProperty(userSession.getClientId(), CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);

		List<Item> occupants = capvChatUserRequestProcessor.getOccupantsByRoom(room + "@conference." + service);

		JsonObject messageToSend = new JsonObject();

		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_RECEIVE);
		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, userName);
		messageToSend.addProperty("room", room);
		messageToSend.addProperty("stamp", new Date().toString());
		messageToSend.addProperty("messageId", msgId);
		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, msg);


		for (Item item : occupants) {
			String occupant = item.getEntityID().toString().split("/")[1];
			if (!occupant.equals(userName)) {
				com.capv.um.model.User friend = userService.getByUserName(occupant, false);
				if (friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("ios")) {
					apnsService.pushCallNotification(friend.getTokenId(), messageToSend.toString(), friend.getClientId());
				} else if (friend != null && friend.getLastSigninOs() != null && friend.getLastSigninOs().equals("android")) {
					fcmService.sendMessage(friend.getTokenId(), messageToSend, friend.getClientId());
				}
			}
		}

		JsonObject messageToSendAdmin = new JsonObject();
		messageToSendAdmin.addProperty("messageId", msgId);
		messageToSendAdmin.addProperty(CapvClientUserConstants.WS_MESSAGE_SCHEDULE_CHAT_MESSAGE_KEY, "message sent");
		com.capv.um.model.User adminUser = userService.getByUserName(userName, false);

		if (adminUser != null && adminUser.getLastSigninOs() != null && adminUser.getLastSigninOs().equals("ios")) {
			apnsService.pushCallNotification(adminUser.getTokenId(), messageToSend.toString(), adminUser.getClientId());
		} else if (adminUser != null && adminUser.getLastSigninOs() != null && adminUser.getLastSigninOs().equals("android")) {
			fcmService.sendMessage(adminUser.getTokenId(), messageToSendAdmin, adminUser.getClientId());
		}

	}

}

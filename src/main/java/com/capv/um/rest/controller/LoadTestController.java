package com.capv.um.rest.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.chat.CapvChatClientConfiguration;
import com.capv.client.user.chat.CapvChatClientManager;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserRequestProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.constants.CapvClientUserConstants.UserState;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.model.User;
import com.capv.um.service.CallStateService;
import com.capv.um.service.UserService;
import com.capv.um.service.VideoRecordingService;
import com.capv.um.util.CapvUtil;

/**
 * <h1>UserController</h1>
 * this class is used to perform custom user operations 
 * @author narendra.muttevi
 * @version 1.0
 */
@Controller
@RequestMapping("/loadtest")
public class LoadTestController {
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private CallStateService callStateService;
	
	@Autowired
	private VideoRecordingService videoReordingService;
	
	private String chatMessages[] = {
								"Hi, How r u?",
								"Hope everything is going well.",
								"This is a test message for CapV application.",
								"Test message from CapV chat.",
								"This is a test message triggrered by CapV chat load test.",
								"The greatest obstacle to discovery is not ignorance; it is the illusion of knowledge.",
								"The pessimist complains about the wind; the optimist expects it to change; the realist adjusts the sails.",
								"If you don’t make mistakes, you’re not working on hard enough problems. And that’s a big mistake.",
								"You can never get enough of what you don’t really need.",
								"Do not confuse motion and progress. A rocking horse keeps moving but does not make any progress.",
								"There is a great difference between worry and concern. A worried person sees a problem, and a concerned person solves a problem.",
								"Success consists of going from failure to failure without loss of enthusiasm.",
								"If it weren’t for my lawyer, I’d still be in prison. It went a lot faster with two people digging.",
								"Acquaintance, n.: A person whom we know well enough to borrow from, but not well enough to lend to.",
								"I like long walks, especially when they are taken by people who annoy me.",
								"Education is what remains after one has forgotten what one has learned in school.",
								"The reasonable man adapts himself to the world; the unreasonable one persists to adapt the world to himself. Therefore all progress depends on the unreasonable man.",
								"The difference between the right word and the almost right word is the difference between lightning and a lightning bug.",
								"Simple, clear purpose and principles give rise to complex and intelligent behavior. Complex rules and regulations give rise to simple and stupid behavior.",
								"In preparing for battle I have always found that plans are useless, but planning is indispensable."
							};
	
	private boolean doChat = false;
	
	@RequestMapping( value = "/createUsers", method = RequestMethod.POST)
	public void createUsers(HttpServletRequest request, HttpServletResponse response) {
		
		int noOfUsers = 100;
		int usersStartIndex = 1;
		
		String clientIdStr = environment.getProperty("oauth.client.id");
		Long clientId = Long.parseLong(clientIdStr.substring(0, clientIdStr.indexOf('@')));
		
		String noOfUsersStr = request.getParameter("noOfUsers");
		String usersStartIndexStr = request.getParameter("usersStartIndex");
		
		if(noOfUsersStr != null) {
			try {
				noOfUsers = Integer.parseInt(noOfUsersStr);
			} catch (Exception e){}
		}
		
		if(usersStartIndexStr != null) {
			try {
				usersStartIndex = Integer.parseInt(usersStartIndexStr);
			} catch (Exception e){}
		}
		
		for(int i=1; i<=noOfUsers; i++) {
			try {
				User user = new User();
				
				user.setUserName("user"+usersStartIndex);
				user.setPassword(CapvUtil.encodePassword("Password@99"));
				user.setClientId(clientId);
				user.setName("User"+usersStartIndex);
				user.setMobile("991234567"+usersStartIndex);
				user.setEmail("user"+usersStartIndex+"@gmail.com");
				user.setCallStatus(UserState.NOTLOGGEDIN.getStateId());
				user.setCreatedDate(new Date());
				user.setLastUpdated(new Date());
				user.setActive(true);
				
				usersStartIndex++;
				
				userService.save(user);
			} catch (Exception e){}
		}
		
	}
	
	@RequestMapping( value = "/loginUsers", method = RequestMethod.POST)
	public void loginUsers(HttpServletRequest request, HttpServletResponse response) {
		
		int noOfUsers = 100;
		int usersStartIndex = 1;
		
		String clientIdStr = environment.getProperty("oauth.client.id");
		Long clientId = Long.parseLong(clientIdStr.substring(0, clientIdStr.indexOf('@')));
		
		String noOfUsersStr = request.getParameter("noOfUsers");
		String usersStartIndexStr = request.getParameter("usersStartIndex");
		
		if(noOfUsersStr != null) {
			try {
				noOfUsers = Integer.parseInt(noOfUsersStr);
			} catch (Exception e){}
		}
		
		if(usersStartIndexStr != null) {
			try {
				usersStartIndex = Integer.parseInt(usersStartIndexStr);
			} catch (Exception e){}
		}
		
		List<User> userList = userService.getUsersByPagination(usersStartIndex, noOfUsers);
		
		for(User user :userList) {
			
			String userName = user.getUserName();
			
			String sessionId = UUID.randomUUID().toString();
			UserSession userSession = new UserSession(user.getUserName(), user.getId(), user.getClientId(), null, null,"web","","");
			UserRegistry.addUserSession(user.getUserName(), sessionId, userSession);
			
			int chatServerPort = CapvClientUserConstants.XMPP_SERVER_DEFAULT_PORT;
			
			String chatServiceHost = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_HOST_NAME_KEY);
			try {
				chatServerPort = Integer.parseInt(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_PORT_KEY));
			} catch (Exception e) {
				e.printStackTrace();
			}
			String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
			
			try {
				
				CapvChatClientManager capvChatClientManager = null;
				if(CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName) == null || 
						!CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName).isChatConnectionAlive()) {
					CapvChatClientConfiguration capvChatClientConfiguration = CapvChatClientConfiguration
																					.connectionConfigurationBuilder()
																						.setHost(chatServiceHost)
																						.setPort(chatServerPort)
																						.setService(chatServerServiceName)
																						.setUserName(userName)
																						.setPassword("Password@99")
																						.setWebSocketSessionId(sessionId)
																						.build();
					
					capvChatClientManager = new CapvChatClientManager(capvChatClientConfiguration);
					capvChatClientManager.initializeConnection();
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	@RequestMapping( value = "/logoutUsers", method = RequestMethod.POST)
	public void logoutUsers(HttpServletRequest request, HttpServletResponse response) {
		
		doChat = false;
		Map<String, Map<String, UserSession>> userSessions = UserRegistry.getAllSessions();
		
		for(String userName :userSessions.keySet()) {
			Map<String, UserSession> userSessionsBySessionId = userSessions.get(userName);
			
			for(String userSessionId :userSessionsBySessionId.keySet()) {
				
				//UserRegistry.removeUserSession(userSessionId, userService, callStateService, videoReordingService);
			}
		}
	}
	
	@RequestMapping( value = "/sendFriendRequests", method = RequestMethod.POST)
	public void sendFriendRequests(HttpServletRequest request, HttpServletResponse response) {
		
		Map<String, Map<String, UserSession>> userSessions = UserRegistry.getAllSessions();
		
		try {
			
			if(userSessions.size() > 0) {
				List<Map<String, UserSession>> userSessionsList = new ArrayList<>(userSessions.values());
				
				int currentIndex = 0;
					
				while(currentIndex < (userSessionsList.size() / 2)) {
					
					UserSession currentUser = (UserSession)userSessionsList.get(currentIndex).values().toArray()[0];
					CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
															CapvChatClientManagerRegistry
																.getCapvChatUserRequestProcessorByUserName(currentUser.getUserName());
					
					List<Map<String, UserSession>> userListToSendFriendRequests = 
							userSessionsList.subList((currentIndex + 1), userSessionsList.size());
					
					
					for(Map<String, UserSession> userMapToSendFriendRequest :userListToSendFriendRequests) {
						UserSession userToSendFriendRequest = (UserSession)userMapToSendFriendRequest.values().toArray()[0];

						capvChatUserRequestProcessor.addBuddy(userToSendFriendRequest.getUserName(), null);
						
						
					}
					currentIndex++;
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@RequestMapping( value = "/doChat", method = RequestMethod.POST)
	public void doChatBetweenUsers(HttpServletRequest request, HttpServletResponse response) {
		
		int noOfParllelChats = 10;
		int noOfChatMinutes = 10;
		
		if(request.getParameter("noOfParllelChats") != null && 
				request.getParameter("noOfParllelChats").trim().length() > 0) {
			try {
				noOfParllelChats = Integer.parseInt(request.getParameter("noOfParllelChats").trim());
			} catch (Exception e){}
		}
		
		if(request.getParameter("noOfChatMinutes") != null && 
				request.getParameter("noOfChatMinutes").trim().length() > 0) {
			try {
				noOfChatMinutes = Integer.parseInt(request.getParameter("noOfChatMinutes").trim());
			} catch (Exception e){}
		}
		
		int noOfChatMinutesUpdated = noOfChatMinutes;
		
		doChat = true;
		
		for(int i=1; i<=noOfParllelChats; i++) {
			Thread t = new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					Map<String, Map<String, UserSession>> userSessions	= UserRegistry.getAllSessions();
					List<Map<String, UserSession>> userSessionsList		= new ArrayList<>(userSessions.values());
					
					long startTime = System.currentTimeMillis();
					
					try {
						while(doChat && ((System.currentTimeMillis() - startTime == 0) || 
										((((System.currentTimeMillis() - startTime)/1000)/60) < noOfChatMinutesUpdated))) {
							
							for(Map<String, UserSession> userSessionMap :userSessionsList) {
								UserSession currentUser = (UserSession)userSessionMap.values().toArray()[0];
								
								CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
																	CapvChatClientManagerRegistry
																		.getCapvChatUserRequestProcessorByUserName(currentUser.getUserName());
								
								List<com.capv.client.user.User> friendList = capvChatUserRequestProcessor.getUsers();
								
								for(com.capv.client.user.User friend :friendList) {
									
									String message = chatMessages[new Random().nextInt(chatMessages.length)];
									
									try {
										capvChatUserRequestProcessor.sendMessage(message, friend.getName());
									} catch (Exception e) {}
									
									CapvChatUserRequestProcessor friendCapvChatUserRequestProcessor = 
																		CapvChatClientManagerRegistry
																			.getCapvChatUserRequestProcessorByUserName(friend.getName());
									
									if(friendCapvChatUserRequestProcessor != null) {
										message = chatMessages[new Random().nextInt(chatMessages.length)];
										
										try {
											friendCapvChatUserRequestProcessor.sendMessage(message, currentUser.getUserName());
										} catch (Exception e) {}
									}
								}
							}
				
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
			}, "ChatThread"+i);
			
			t.start();
		}
		
	}

}

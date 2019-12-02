package com.capv.um.rest.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.websocket.UserWebSocketMessage;
import com.capv.um.model.ScheduleChat;
import com.capv.um.service.APNSService;
import com.capv.um.service.FCMService;
import com.capv.um.service.ScheduleChatService;
import com.capv.um.service.UserService;
import com.google.gson.JsonObject;

@Component
public class AutoChatScheduler {

	private static final Logger log = LoggerFactory.getLogger(WebSocketPingScheduler.class);

	@Autowired
	private ScheduleChatService chatService;

	@Autowired
	private UserService userService;

	@Autowired
	private FCMService fcmService;

	@Autowired
	private APNSService apnsService;

	@Scheduled(fixedRate = 60000)
	public void checkAutoChatPing() {
		try {
			List<ScheduleChat> scheduleChatList = chatService.getTodayScheduledChat();
			for (ScheduleChat scheduleChat : scheduleChatList) {
				DateFormat dfCurrenttimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				if (dfCurrenttimestamp.format(scheduleChat.getScheduleStartDate()).equals(dfCurrenttimestamp.format(new Date()))) {
					JsonObject chatmessage = new JsonObject();
					chatmessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.VC_MSG_SCHEDULE_MESSAGE);
					chatmessage.addProperty("room", scheduleChat.getRoomName());
					chatmessage.addProperty("message", scheduleChat.getChatMesage());
					chatmessage.addProperty("userName", scheduleChat.getUserName());
					com.capv.um.model.User adminUser = userService.getByUserName(scheduleChat.getUserName(), false);
					if (adminUser != null) {
						List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(adminUser.getUserName());
						if (userSessions != null && !userSessions.isEmpty()) {
							UserSession receiverSession = userSessions.get(0);
							UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(receiverSession, chatmessage.toString());
							receiverSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
						} else if (adminUser != null && adminUser.getLastSigninOs() != null && adminUser.getLastSigninOs().equals("ios")) {
							apnsService.pushCallNotification(adminUser.getTokenId(), chatmessage.toString(), adminUser.getClientId());
						} else if (adminUser != null && adminUser.getLastSigninOs() != null && adminUser.getLastSigninOs().equals("android")) {
							fcmService.sendMessage(adminUser.getTokenId(), chatmessage, adminUser.getClientId());
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}

}

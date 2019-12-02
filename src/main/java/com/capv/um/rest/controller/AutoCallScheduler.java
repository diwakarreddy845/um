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
import com.capv.um.model.ScheduleCall;
import com.capv.um.model.User;
import com.capv.um.service.APNSService;
import com.capv.um.service.FCMService;
import com.capv.um.service.ScheduleCallService;
import com.capv.um.service.UserService;
import com.google.gson.JsonObject;

@Component
public class AutoCallScheduler {

	private static final Logger log = LoggerFactory.getLogger(WebSocketPingScheduler.class);

	@Autowired
	private ScheduleCallService scheduleCallService;

	@Autowired
	private FCMService fcmService;
	@Autowired
	private APNSService apnsService;

	@Autowired
	private UserService userService;

	@Scheduled(fixedRate = 60000)
	public void checkAUtoCalling() {
		try {
			List<ScheduleCall> scheduleCallList = scheduleCallService.getAllByDate(new java.sql.Date(0), 0);
			for (ScheduleCall scheduleCall : scheduleCallList) {
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				if (df.format(scheduleCall.getScheduleStartDate()).equals(df.format(new Date()))) {
					User adminUser = userService.getByUserName(scheduleCall.getUserName(), false);
					JsonObject incomingCallMessage = new JsonObject();
					incomingCallMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.SCHEDULE_CALL);
					incomingCallMessage.addProperty("roomName", scheduleCall.getRoomName());
					incomingCallMessage.addProperty("callType", scheduleCall.getCallType());
					incomingCallMessage.addProperty(CapvClientUserConstants.MAX_RETRY, scheduleCall.getMaxNumberOfRetry());
					if (adminUser != null && adminUser.getLastSigninOs() != null && adminUser.getLastSigninOs().equals("ios")) {
						apnsService.pushCallNotification(adminUser.getTokenId(), incomingCallMessage.toString(), adminUser.getClientId());
					} else if (adminUser != null && adminUser.getLastSigninOs() != null && adminUser.getLastSigninOs().equals("android")) {
						fcmService.sendMessage(adminUser.getTokenId(), incomingCallMessage, adminUser.getClientId());
					}
					if (adminUser != null) {
						List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(adminUser.getUserName());
						if (userSessions != null & !userSessions.isEmpty()) {
							UserSession receiverSession = userSessions.get(0);
							UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage(receiverSession, incomingCallMessage.toString());
							receiverSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
						}
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage());
		}

	}

}
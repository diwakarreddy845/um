package com.capv.client.user.websocket;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.capv.client.user.TryItUserSession;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.video_calling.CapvVideoCallingWebSocketClientConfiguration;
import com.capv.client.user.video_calling.Capv_VC_WS_ClientHandler;
import com.capv.um.model.TryItRoom;
import com.capv.um.service.EmailService;
import com.capv.um.service.TryItService;
import com.capv.um.util.CapvUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

/**
 * <h1>Capv WebSocket Endpoint</h1>
 * 
 * This class is used to handle WebSocket messages sent by WebSocket clients and process them as per the instructions sent by client as part
 * of message
 * 
 * @author sharath.yeturu
 * @version 1.0
 *
 */
public class TryItWebSocketEndpoint extends TextWebSocketHandler {

	@Autowired
	private EmailService email;

	@Autowired
	private TryItService tryit;

	private static final Logger log = LoggerFactory.getLogger(TryItWebSocketEndpoint.class);

	private static final Gson gson = new GsonBuilder().create();

	private static Map<String, TryItUserSession> tryItResgiter = new ConcurrentHashMap<>();

	private static Map<String, TryItUserSession> tryItUserResgiter = new ConcurrentHashMap<>();

	private static Map<String, Map<String, TryItUserSession>> roomSessions = new ConcurrentHashMap<>();

	public static Map<String, Map<String, TryItUserSession>> getRoomSessions() {
		return roomSessions;
	}

	public TryItWebSocketEndpoint() {
		log.info("tryitRoom method ended");
	}

	public static TryItUserSession getTryItUserSessionBySessionId(String webSocketSessionId) {
		return tryItResgiter.get(webSocketSessionId);
	}

	public static void removeUserFromRoom(String userName, String room) {
		if (roomSessions.get(room) != null) {
			roomSessions.get(room).remove(userName);
		}
	}

	private String getRoomValidity(String roomNumber) {
		String validity = null;
		TryItRoom tryitRoom = tryit.fetchUniqueRoomRecord(roomNumber);
		if (tryitRoom != null) {
			Date createdTIme = tryitRoom.getCreatedTimestamp();
			Date currentTime = new Date(CapvUtil.getUTCTimeStamp());
			Date validityTime = new Date(createdTIme.getTime() + (tryitRoom.getValidity() * 60 * 60 * 1000));
			if (validityTime.compareTo(currentTime) > 0) {
				long validitySeconds = (validityTime.getTime() - currentTime.getTime()) / 1000;
				if (validitySeconds >= 60) {
					long validityMinutes = validitySeconds / 60;
					if (validityMinutes >= 60) {
						long validityHours = validityMinutes / 60;
						validityMinutes = validityMinutes - (validityHours * 60);
						validitySeconds = validitySeconds - ((validityHours * 60 * 60) + (validityMinutes * 60));
						validity = (validityHours < 10 ? "0" + validityHours : validityHours) + ":"
								+ (validityMinutes < 10 ? "0" + validityMinutes : validityMinutes) + ":"
								+ (validitySeconds < 10 ? "0" + validitySeconds : validitySeconds);
					} else {
						validitySeconds = validitySeconds - (validityMinutes * 60);
						validity = "00:" + (validityMinutes < 10 ? "0" + validityMinutes : validityMinutes) + ":"
								+ (validitySeconds < 10 ? "0" + validitySeconds : validitySeconds);
					}
				} else
					validity = "00:00:" + (validitySeconds < 10 ? "0" + validitySeconds : validitySeconds);
			} else
				validity = "Room Expired";
		} else
			validity = "Invalid Room";
		return validity;
	}

	private boolean checkRoomValidity(String roomNo) {
		TryItRoom tryitRoom = tryit.fetchUniqueRoomRecord(roomNo);
		if (tryitRoom != null) {
			Date created = tryitRoom.getCreatedTimestamp();
			Date now = new Date(CapvUtil.getUTCTimeStamp());
			long diff = now.getTime() - created.getTime();
			long diffHours = diff / (60 * 60 * 1000) % 24;
			if (diffHours >= tryitRoom.getValidity()) {
				return false;
			} else {
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * This method is used to process the user messages sent through the user websocket session
	 * <p>
	 * This method process all the chat messages, user friend request messages, group handling messages and video calling messages
	 * <p>
	 * This method expects <b>capv_msg</b> as a key in JSON message and corresponding value of this key differentiates the action to be taken
	 * for chat, friend requests and group management This method expects <b>id</b> as a key in JSON message and corresponding value of this key
	 * used process call management of the user
	 * 
	 * @param session This is the first parameter of the method refers the WebSocketSession
	 * @param message This is the second parameter of the method which holds user message sent by websocket client
	 */
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			final JsonObject jsonMessageObj = gson.fromJson(message.getPayload(), JsonObject.class);
			log.debug("JSON Message::" + jsonMessageObj);
			TryItUserSession tryItUserSession = tryItResgiter.get(session.getId());
			String userName = tryItUserSession.getUserName();
			if (jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_ID_KEY) != null) {
				switch (jsonMessageObj.get(CapvClientUserConstants.WS_MESSAGE_ID_KEY).getAsString()) {
				case CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND: {
					if (tryItUserSession != null) {
						// CapvChatUserRequestProcessor capvChatUserRequestProcessor = getCapvChatUserRequestProcessor(userSession);
					}
				}
					break;
				case CapvClientUserConstants.WS_MESSAGE_UPDATE_CALL_STATS: {
					if (jsonMessageObj.get("callStats") != null) {
						String callStatsString = jsonMessageObj.get("callStats").getAsJsonArray().toString();
						CapvClientUserUtil.updateCallStatistics(callStatsString);
					}
				}
					break;
				}
			} else if (jsonMessageObj.get("id") != null) {
				String vcMsg = jsonMessageObj.get("id").getAsString();
				switch (vcMsg) {
				case CapvClientUserConstants.GET_TRY_IT_ROOM: {
					JsonObject roomMessage = new JsonObject();
					roomMessage.addProperty("id", CapvClientUserConstants.TRY_IT_ROOM);
					roomMessage.addProperty("room", UUID.randomUUID().toString());
					String clientIdStr = CapvClientUserUtil.getConfigProperty("client.id");
					Long clientId = Long.parseLong(clientIdStr);
					String validity = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.TRYIT_ROOM_VALIDITY_KEY);
					TryItRoom tryit_room = new TryItRoom(roomMessage.get("room").getAsString(), Integer.parseInt(validity), 0);
					tryit.save(tryit_room);
					tryItUserSession.processMessage(roomMessage.toString());
				}
					break;
				case CapvClientUserConstants.GET_TRY_IT_ROOM_VALIDITY: {
					JsonObject roomMessage = new JsonObject();
					roomMessage.addProperty("id", CapvClientUserConstants.TRY_IT_ROOM_VALIDITY);
					if (jsonMessageObj.get("room") != null) {
						String roomNumber = jsonMessageObj.get("room").getAsString();
						roomMessage.addProperty("room", CapvClientUserConstants.TRY_IT_ROOM_VALIDITY);
						roomMessage.addProperty("validity", getRoomValidity(roomNumber));
					} else
						roomMessage.addProperty("validity", "Invalid Request");
					tryItUserSession.processMessage(roomMessage.toString());
				}
					break;
				case CapvClientUserConstants.CHECK_TRY_IT_ROOM_USER: {
					JsonObject roomMessage = new JsonObject();
					roomMessage.addProperty("id", CapvClientUserConstants.CHECK_TRY_IT_ROOM_USER_RESPONSE);
					if (jsonMessageObj.get("room") != null && jsonMessageObj.get("userName") != null) {
						if (checkRoomValidity(jsonMessageObj.get("room").getAsString())) {
							String room = jsonMessageObj.get("room").getAsString();
							String roomUserName = jsonMessageObj.get("userName").getAsString();
							if (roomSessions.get(room) != null && roomSessions.get(room).containsKey(roomUserName))
								roomMessage.addProperty("result", "User occupied");
							else
								roomMessage.addProperty("result", "User not occupied");
							tryItUserSession.processMessage(roomMessage.toString());
						} else {
							roomMessage.addProperty("result", "Room Expired");
							tryItUserSession.processMessage(roomMessage.toString());
						}
					}
				}
					break;
				case CapvClientUserConstants.VC_TRY_IT: {
					if (jsonMessageObj.get("room") != null) {
						if (checkRoomValidity(jsonMessageObj.get("room").getAsString())) {
							String room = jsonMessageObj.get("room").getAsString();
							if (roomSessions.get(room) != null) {
								if (roomSessions.get(room).size() >= 15) {
									JsonObject rej_msg = new JsonObject();
									rej_msg.addProperty("id", "CallJoinError");
									rej_msg.addProperty("error", "RoomOverflow");
									tryItUserSession.processMessage(rej_msg.toString());
									return;
								}
								if (roomSessions.get(room).containsKey(tryItUserSession.getUserName())) {
									JsonObject rej_msg = new JsonObject();
									rej_msg.addProperty("id", "CallJoinError");
									rej_msg.addProperty("error", "DuplicateUserName");
									tryItUserSession.processMessage(rej_msg.toString());
									return;
								}
							}
							boolean isVCServiceConnected = false;
							try {
								CapvVideoCallingWebSocketClientConfiguration videoCallingWebSocketClientConfiguration = CapvVideoCallingWebSocketClientConfiguration
										.connectionConfigurationBuilder().setUserName(userName).setWebSocketSessionId(session.getId()).settryIt(true)
										.build();
								Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = new Capv_VC_WS_ClientHandler(
										videoCallingWebSocketClientConfiguration);
								capvVideoCallingWebSocketClientEndpoint.connectToClient();
								if (capvVideoCallingWebSocketClientEndpoint.isSessionAlive()) {
									isVCServiceConnected = true;
								}
								tryItUserSession.setCapvVideoCallingWebSocketClientEndpoint(capvVideoCallingWebSocketClientEndpoint);
							} catch (Exception e) {
								log.error("Error while initializing capv video calling session::", e);
							}
							if (!isVCServiceConnected) {
								JsonObject vcConnectErrorMessage = new JsonObject();
								vcConnectErrorMessage.addProperty("id", "CallJoinError");
								vcConnectErrorMessage.addProperty("error", "VCConnectError");
								tryItUserSession.processMessage(vcConnectErrorMessage.toString());
								return;
							}
							JsonObject obj_msg;
							Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = tryItUserSession
									.getCapvVideoCallingWebSocketClientEndpoint();
							obj_msg = new JsonObject();
							obj_msg.addProperty("method", "joinRoom");
							obj_msg.addProperty("id", 0);
							obj_msg.addProperty("jsonrpc", "2.0");
							JsonObject params = new JsonObject();
							params.addProperty("room", room);
							params.addProperty("user", userName);
							params.addProperty("callMode", "video");
							params.addProperty("istryit", "enable");
							params.addProperty("dataChannels", true);
							obj_msg.add("params", params);
							capvVideoCallingWebSocketClientEndpoint.sendMessage(obj_msg.toString());
							if (roomSessions.get(room) == null)
								roomSessions.put(room, new ConcurrentHashMap<String, TryItUserSession>());
							roomSessions.get(room).put(userName, tryItUserSession);
							session.getAttributes().put("vc_room", room);
							session.getAttributes().put("isCallInProgress", true);
							return;
						} else {
							JsonObject rej_msg = new JsonObject();
							rej_msg.addProperty("id", "CallJoinError");
							rej_msg.addProperty("error", "RoomExpired");
							tryItUserSession.processMessage(rej_msg.toString());
						}
					} else {
						JsonObject rej_msg = new JsonObject();
						rej_msg.addProperty("id", "CallJoinError");
						rej_msg.addProperty("error", "RoomIDInvalid");
						tryItUserSession.processMessage(rej_msg.toString());
					}
				}
					break;
				case CapvClientUserConstants.VC_MSG_EXIT: {
					session.getAttributes().remove("isCallInProgress");
					Capv_VC_WS_ClientHandler vcwsClientEndpoint = tryItUserSession.getCapvVideoCallingWebSocketClientEndpoint();
					JsonObject obj_msg = new JsonObject();
					obj_msg.addProperty("method", "leaveRoom");
					obj_msg.addProperty("id", 11);
					obj_msg.addProperty("jsonrpc", "2.0");
					JsonObject params = new JsonObject();
					params.addProperty("user", userName);
					params.addProperty("roomName", (String) session.getAttributes().get("vc_room"));
					boolean updateStatistics = true;
					if (jsonMessageObj.get("updateStatistics") != null && !jsonMessageObj.get("updateStatistics").getAsBoolean()) {
						updateStatistics = false;
					}
					params.addProperty("updateStatistics", updateStatistics);
					obj_msg.add("params", params);
					if (vcwsClientEndpoint != null)
						vcwsClientEndpoint.sendExitMessageAndCloseSession(obj_msg.toString());
					if (session.getAttributes().get("vc_room") != null) {
						if (roomSessions.get(session.getAttributes().get("vc_room")) != null) {
							roomSessions.get(session.getAttributes().get("vc_room")).remove(tryItUserSession.getUserName());
							if (roomSessions.get(session.getAttributes().get("vc_room")).isEmpty())
								roomSessions.remove(session.getAttributes().get("vc_room"));
						}
					}
				}
					break;
				case CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND: {
					if (jsonMessageObj.get("message") != null && session.getAttributes().get("vc_room") != null) {
						String vcRoom = (String) session.getAttributes().get("vc_room");
						if (roomSessions.get(vcRoom) != null) {
							JsonObject messageObject = new JsonObject();
							messageObject.addProperty("id", CapvClientUserConstants.WS_MESSAGE_MESSAGE_RECEIVE);
							messageObject.addProperty("message", jsonMessageObj.get("message").getAsString());
							messageObject.addProperty("sender", userName);
							Map<String, TryItUserSession> roomUsers = roomSessions.get(vcRoom);
							for (String roomUserName : roomUsers.keySet()) {
								if (!roomUserName.equals(userName)) {
									TryItUserSession roomUserSession = roomUsers.get(roomUserName);
									roomUserSession.processMessage(messageObject.toString());
								}
							}
						}
					}
				}
					break;
				case CapvClientUserConstants.SENDMAIL: {
					if (jsonMessageObj.get("link") != null && jsonMessageObj.get("emails") != null) {
						String email_id = jsonMessageObj.get("emails").getAsString();
						JsonObject rej_msg = new JsonObject();
						rej_msg.addProperty("id", "EmailInvitationResponse");
						try {
							String[] to = email_id.split(",");
							email.sendEmail(to, " Capv Video Calling Invite ",
									"Please Click on the Invite link to Join Call " + jsonMessageObj.get("link").getAsString());
							rej_msg.addProperty("message", "Email invite sent sucessfully");
						} catch (Exception e) {
							rej_msg.addProperty("message", "Email invitation failed due to server error");
						} finally {
							tryItUserSession.processMessage(rej_msg.toString());
						}
					} else {
						JsonObject rej_msg_error = new JsonObject();
						rej_msg_error.addProperty("id", "EmailInvitationResponse");
						rej_msg_error.addProperty("message", "Invalid invitation input");
						tryItUserSession.processMessage(rej_msg_error.toString());
					}
				}
					break;
				case CapvClientUserConstants.VC_PARTICIPANT_TOGGLE_MEDIA: {
					if (jsonMessageObj.get("room") != null && jsonMessageObj.get("from") != null) {
						String room = jsonMessageObj.get("room").getAsString();
						String fromUser = jsonMessageObj.get("from").getAsString();
						if (roomSessions.get(room) != null) {
							Map<String, TryItUserSession> roomUserSessions = roomSessions.get(room);
							for (String roomUser : roomUserSessions.keySet()) {
								if (!roomUser.equals(fromUser)) {
									TryItUserSession roomUserSession = roomUserSessions.get(roomUser);
									roomUserSession.processMessage(jsonMessageObj.toString());
								}
							}
						}
					}
				}
					break;
				case CapvClientUserConstants.GET_PARTICIPANT_MEDIA_STATUS: {
					if (jsonMessageObj.get("from") != null) {
						String fromUser = jsonMessageObj.get("from").getAsString();
						if (tryItUserResgiter.get(fromUser) != null) {
							TryItUserSession tryItFromUserSession = tryItUserResgiter.get(fromUser);
							tryItFromUserSession.processMessage(jsonMessageObj.toString());
						}
					}
				}
					break;
				case CapvClientUserConstants.PARTICIPANT_MEDIA_STATUS: {
					if (jsonMessageObj.get("to") != null) {
						String toUser = jsonMessageObj.get("to").getAsString();
						if (tryItUserResgiter.get(toUser) != null) {
							TryItUserSession tryItToUserSession = tryItUserResgiter.get(toUser);
							tryItToUserSession.processMessage(jsonMessageObj.toString());
						}
					}
				}
					break;
				case "onIceCandidate": {
					try {
						Capv_VC_WS_ClientHandler capvVideoCallingWebSocketClientEndpoint = tryItUserSession
								.getCapvVideoCallingWebSocketClientEndpoint();
						JsonObject candidate = jsonMessageObj.get("candidate").getAsJsonObject();
						JsonObject obj_msg = new JsonObject();
						obj_msg.addProperty("method", "onIceCandidate");
						obj_msg.addProperty("id", 3);
						JsonObject params = new JsonObject();
						params.add("candidate", candidate.get("candidate"));
						params.add("sdpMLineIndex", candidate.get("sdpMLineIndex"));
						params.add("sdpMid", candidate.get("sdpMid"));
						params.addProperty("endpointName", jsonMessageObj.get("userId").getAsString());
						obj_msg.add("params", params);
						capvVideoCallingWebSocketClientEndpoint.sendMessage(obj_msg.toString());
					} catch (Exception e) {
						log.error("Error while sending ICECandidates::", e);
					}
				}
					break;
				default: {
					if (tryItUserSession != null) {
						Capv_VC_WS_ClientHandler vcwsClientEndpoint = tryItUserSession.getCapvVideoCallingWebSocketClientEndpoint();
						vcwsClientEndpoint.sendMessage(message.getPayload());
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
	 * This method used to initialize chat service and video calling service connections after user has been successfully authenticated and
	 * established websocket connection with capv
	 * 
	 * @param session This parameter refers to the WebSocketSession of the connected user and provides the information about connected user
	 *                which is required to initialize the connections with chat and video calling services
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String userName = (String) session.getAttributes().get("userName");
		TryItUserSession userSession = new TryItUserSession(userName, session);
		tryItResgiter.put(session.getId(), userSession);
		tryItUserResgiter.put(userName, userSession);
		Timer webSocketPingTImer = new Timer();
		webSocketPingTImer.schedule(new TryItWebSocketPingTimerTask(userSession), 1000, 10000);
		userSession.setWebSocketPingTimer(webSocketPingTImer);
	}

	/**
	 * 
	 * This method invoked after websocket connection get disconnected with the user client
	 * 
	 * @param session This is the first parameter of the method which refers the user websocket session which is get disconnected.
	 * @param status  This is the second parameter of the method which refers the status code of the websocket connection closed which is used
	 *                to check whether websocket closed normally or closed due to error
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		System.out.println("Session closed. Close status::" + status.getCode() + "\t Close Reason::" + status.getReason());
		TryItUserSession tryItUserSession = tryItResgiter.get(session.getId());
		String userName = tryItUserSession.getUserName();
		Timer webSocketPingTimer = tryItUserSession.getWebSocketPingTimer();
		if (webSocketPingTimer != null) {
			webSocketPingTimer.cancel();
			tryItUserSession.setWebSocketPingTimer(null);
		}
		if (session.getAttributes().get("isCallInProgress") != null) {
			JsonObject obj_msg = new JsonObject();
			obj_msg.addProperty("method", "leaveRoom");
			obj_msg.addProperty("id", 11);
			obj_msg.addProperty("jsonrpc", "2.0");
			JsonObject params = new JsonObject();
			params.addProperty("user", userName);
			params.addProperty("roomName", (String) session.getAttributes().get("vc_room"));
			boolean updateStatistics = true;
			params.addProperty("updateStatistics", updateStatistics);
			obj_msg.add("params", params);
			if (tryItResgiter.get(session.getId()) != null && tryItResgiter.get(session.getId()).getCapvVideoCallingWebSocketClientEndpoint() != null)
				tryItResgiter.get(session.getId()).getCapvVideoCallingWebSocketClientEndpoint().sendExitMessageAndCloseSession(obj_msg.toString());
		}
		tryItResgiter.remove(session.getId());
		tryItUserResgiter.remove(tryItUserSession.getUserName());
		if (session.getAttributes().get("vc_room") != null && roomSessions.get(session.getAttributes().get("vc_room")) != null) {
			roomSessions.get(session.getAttributes().get("vc_room")).remove(tryItUserSession.getUserName());
			if (roomSessions.get(session.getAttributes().get("vc_room")).isEmpty())
				roomSessions.remove(session.getAttributes().get("vc_room"));
		}
	}

	@Override
	protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
		TryItUserSession tryItUserSession = tryItResgiter.get(session.getId());
		tryItUserSession.setLastPongMessageReceived(System.currentTimeMillis());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if (exception instanceof IOException) {
			// if connection lost, call this
			session.close();
		}
	}
}

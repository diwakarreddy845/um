package com.capv.client.user.chat.listener;

import java.util.List;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.muc.MUCAffiliation;
import org.jivesoftware.smackx.muc.packet.MUCUser;

import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.websocket.UserWebSocketMessage;
import com.google.gson.JsonObject;
/**
 * <h1>Listener for user subscription requests</h1>
 * 
 * Provides a mechanism to listen for user subscription or friend requests, process the requests and deliver them to respective user
 * 
 * @author narendra.muttevi
 * @version 1.0.
 */
public class CapvGroupUserSubscribeListener implements StanzaListener {
	
	private String userName;
	
	/**
	 * this parameterized constructor is used to initialize the CapvChatManagerListener
	 * @param capvChatUserRequestProcessor
	 * 
	 */
	public CapvGroupUserSubscribeListener(String userName) {
		this.userName = userName;
	}

	/**
	 * This method is used to process the user subscription requests
	 * @param packet	The user subscription request packet
	 * @throws NotConnectedException	if the connection is not established or broken with the server
	 */
	@Override
	public void processPacket(Stanza packet) throws NotConnectedException {
		if(packet instanceof Presence && MUCUser.from(packet) != null) {
			
			Presence presence = (Presence)packet;
			MUCUser mucUser = MUCUser.from(packet);
			
			if(mucUser.getDestroy() == null) {
				
				if(mucUser.getItem() != null && mucUser.getItem().getAffiliation().equals(MUCAffiliation.member)) {
					
					String fromUser =  presence.getFrom().substring(presence.getFrom().lastIndexOf("/")+1);
					String toUser = presence.getTo().substring(0, presence.getTo().indexOf("@"));
					if(mucUser.getItem().getReason()!=null&&mucUser.getItem().getReason().contains("removed")) {
						if(!fromUser.equals(toUser)) {
							String memberJoinedRoom = presence.getFrom().substring(0, presence.getFrom().indexOf("@"));
							
							List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
							if(userSessions != null){
								Long receiverId = null;
								for(UserSession receiverSession :userSessions)
								{
									if(receiverId == null)
										receiverId = receiverSession.getUserId();
									
									JsonObject messageToSend = new JsonObject();
									messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_DELETE_GROUP_FRIENDS);
									messageToSend.addProperty("message", fromUser.substring(0,fromUser.lastIndexOf("_"))+" Member has been removed From Group");
									messageToSend.addProperty("room", memberJoinedRoom);
									messageToSend.addProperty("deleteUser", fromUser);
									messageToSend.addProperty("status", "success");
									UserWebSocketMessage userWebSocketMessage = new UserWebSocketMessage
																					(receiverSession, messageToSend.toString());
									receiverSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
								}
							}
						}
					}else {
					if(!fromUser.equals(toUser)) {
						String memberJoinedRoom = presence.getFrom().substring(0, presence.getFrom().indexOf("@"));
						
						List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
						if(userSessions != null){
							Long receiverId = null;
							for(UserSession receiverSession :userSessions)
							{
								if(receiverId == null)
									receiverId = receiverSession.getUserId();
								
								JsonObject messageToSend = new JsonObject();
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_ACCEPT_ROOM_REQUEST_RESPONSE);
								messageToSend.addProperty("message", fromUser+" accepted room request successfully");
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
				
			} else {
				String roomJid = mucUser.getDestroy().getJid();
				
				String room = roomJid.substring(0, roomJid.indexOf("@"));
				
				JsonObject groupDeleteSuccessMessage = new JsonObject();
 				groupDeleteSuccessMessage.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
 															CapvClientUserConstants.WS_MESSAGE_ROOM_DELETE_RESPONSE);
 				groupDeleteSuccessMessage.addProperty("room", room);
 				
 				List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
					
					if(userSessions != null) {
						for(UserSession userSession :userSessions) {
							UserWebSocketMessage userWebSocketMessage = 
								new UserWebSocketMessage(userSession, groupDeleteSuccessMessage.toString());
							userSession.getCapvUserWebSocketMessageProcessor().processMessage(userWebSocketMessage);
						}
					}
				
			}
		}
		System.out.println("CapvChatGroupUserSubscribeListener::"+packet);
	}

}

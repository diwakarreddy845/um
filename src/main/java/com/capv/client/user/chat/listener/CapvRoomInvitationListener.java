package com.capv.client.user.chat.listener;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.google.gson.JsonObject;
/**
 * <h1> Listener for room invitations </h1>
 * A listener that is fired anytime an invitation to join a MUC room is received.
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvRoomInvitationListener implements InvitationListener {
	
	private String userName;
	/**
	 * this parameterized constructor is used to initialize the CapvChatManagerListener
	 * @param capvChatUserRequestProcessor
	 * 
	 */
	public CapvRoomInvitationListener(String userName){
		this.userName = userName;
	}

	/**
	 * Called when the an invitation to join a MUC room is received
	 * @param conn the XMPPConnection that received the invitation.
	 * @param room the room that invitation refers to.
	 * @param inviter the inviter that sent the invitation. (e.g. crone1@shakespeare.lit).
	 * @param reason the reason why the inviter sent the invitation.
	 * @param password the password to use when joining the room.
	 * @param message the message used by the inviter to send the invitation.
	 */
	@Override
	public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason,
									String password, Message message) {
		
	//	System.out.println("got RoomInvitationListener "+room+":"+inviter+":"+reason);
		
		JsonObject messageToSend = new JsonObject();
		
		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_REQUEST);
		//messageToSend.addProperty("room", reason.substring(reason.indexOf(":")+1, reason.length()).trim()+room.getRoom().substring(room.getRoom().indexOf("@"), room.getRoom().length()));
		messageToSend.addProperty("room",room.getRoom().toLowerCase());
		messageToSend.addProperty("inviter", inviter);
		messageToSend.addProperty("reason", reason);
		
		String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
		
		CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
		
	}
	

}

package com.capv.client.user.chat.listener;

import java.util.Collection;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.RosterListener;

import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.google.gson.JsonObject;
/**
 * <h1> Listener for user presence changes </h1>
 * 
 * A listener that is fired any time a roster is changed or the presence of
 * a user in the roster is changed.
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvChatUserPresenceListener implements RosterListener {
	
	private String userName;
	
	/**
	 * This parameterized constructor is used to initialize the CapvChatManagerListener
	 * @param capvChatUserRequestProcessor	The reference of CapvChatUserRequestProcessor
	 * 										@see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 * 
	 */
	public CapvChatUserPresenceListener(String userName) {
		this.userName = userName;
	}

	/**
	 * This method called when roster entries are added
	 * @param addresses -- the XMPP addresses of the contacts that have been added to the roster
	 */
	@Override
	public void entriesAdded(Collection<String> addresses) {
	}

	/**
	 * This method called when a roster entries are updated.
	 * @param addresses -- the XMPP addresses of the contacts whose entries have been updated.
	 */
	@Override
	public void entriesUpdated(Collection<String> addresses) {
	}

	/**
	 * This method called when a roster entries are removed.
	 * @param addresses	the XMPP addresses of the contacts that have been removed from the roster.
	 */
	@Override
	public void entriesDeleted(Collection<String> addresses) {
	}

	/**
	 * This method called when the presence of a roster entry is changed
	 * 
	 * @param presence the presence that changed.
	 */
	@Override
	public void presenceChanged(Presence presence) {
		if(presence != null && presence.getType() != null) {
			
			JsonObject messageToSend = new JsonObject();
			
			if(presence.getType() == Presence.Type.available || 
					presence.getType() == Presence.Type.unavailable) {
				
				String fromUser = presence.getFrom().substring(0, presence.getFrom().indexOf("@"));
				
				messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_BUDDY_STATUS);
				messageToSend.addProperty("status", presence.getStatus());
				if(presence.getType() == Presence.Type.available)
					messageToSend.addProperty("presence", presence.getMode().toString());
				else
					messageToSend.addProperty("presence", presence.getType().toString());
				messageToSend.addProperty("from", fromUser);
		   	 
				String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
				
				CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
			}
			
		}
	}

}

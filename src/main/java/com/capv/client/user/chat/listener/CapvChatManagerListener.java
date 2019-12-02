package com.capv.client.user.chat.listener;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManagerListener;

import com.capv.client.user.chat.CapvChatClientManagerRegistry;
/**
 * <h1> ChatManagerListener </h1>
 *  A listener for create chat message listeners on behalf of user and manage them until the chat user connection alive
 * 
 * @author narendra.muttevi
 * @version 1.0
 */

public class CapvChatManagerListener implements ChatManagerListener {
	
	private CapvChatMessageListener capvChatMessageListener;
	private String userName;
	
	/**
	 * this parameterized constructor is used to initialize the CapvChatManagerListener
	 * 
	 * @param capvChatUserRequestProcessor	The reference of CapvChatUserRequestProcessor
	 * 
	 */
	public CapvChatManagerListener(String userName, CapvChatMessageListener capvChatMessageListener) {
		this.userName = userName;
		this.capvChatMessageListener = capvChatMessageListener;
	}

	/**
     * Event fired when a new chat is created.
     *
     * @param chat				the chat that was created.
     * @param createdLocally	true if the chat was created by the local user and false if it wasn't.
     */
	@Override
	public void chatCreated(Chat chat, boolean createdLocally) {
		chat.addMessageListener(capvChatMessageListener);
		CapvChatClientManagerRegistry
				.getCapvChatUserRequestProcessorByUserName(userName)
					.getCapvUserChatHandler()
						.addToChatsByThreadId(chat);
	}

}

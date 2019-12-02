package com.capv.client.user.chat.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.listener.CapvChatConnectionClosingListener;
import com.capv.client.user.chat.listener.CapvChatManagerListener;
import com.capv.client.user.chat.listener.CapvChatMessageListener;
/**
 * <h1>Handler for processing user chat messages</h1>
 * 
 * This class is used to process user chat messages
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvUserChatHandler {
	
	private String userName;
	private ChatManager chatManager;
    private ChatManagerListener chatManagerListener;
    private CapvChatMessageListener capvChatMessageListener;
    private CapvChatConnectionClosingListener capvChatConnectionClosingListener;
    private Map<String, Chat> chatsMap = new ConcurrentHashMap<>();
    private Map<String, List<Chat>> chatsByThreadId = new ConcurrentHashMap<>();
    
    private static final Logger log = LoggerFactory.getLogger(CapvUserChatHandler.class);
	/**
	 * 
	 * This is the parameterized constructor used to initialize the CapvChatUserHandler
	 * 
	 * @param capvChatUserRequestProcessor	The reference of CapvChatUserRequestProcessor
	 * 										@see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 * 
	 */
	public CapvUserChatHandler(String userName) {
		
		this.userName = userName;
		initializeChatListeners();
	}
	/**
	 *
	 * This method is used to initialize chat listeners to listen chat messages and deliver them to the user
	 * 
	*/
	@SuppressWarnings("unchecked")
	private void initializeChatListeners() {
		
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
			capvChatMessageListener = new CapvChatMessageListener(userName);
			chatManagerListener = new CapvChatManagerListener(userName, capvChatMessageListener);
			
			chatManager = ChatManager.getInstanceFor(chatUserConnection);
	        chatManager.addChatListener(chatManagerListener);
	        
	        capvChatConnectionClosingListener = (XMPPTCPConnection chatConnection) -> {
	        										chatsMap.clear();
	        										for(String threadId :chatsByThreadId.keySet()) {
	        											List<Chat> chatList = chatsByThreadId.get(threadId);
	        											for(Chat chat :chatList){
	        												chat.close();
	        											}
	        											chatList.clear();
	        										}
	        										chatsByThreadId.clear();
	        										chatManager.removeChatListener(chatManagerListener);
	        										
	        										try {
														final Field cm_INSTANCES = ChatManager.class.getDeclaredField("INSTANCES");
														cm_INSTANCES.setAccessible(true);
														
														final Map<XMPPConnection, ChatManager> cm_INSTANCES_Map = 
																				(Map<XMPPConnection, ChatManager>) cm_INSTANCES.get(null);
														cm_INSTANCES_Map.remove(chatConnection);
													} catch(Exception e){
														log.error("Exception  occured while initializing ChatListeners:",e);
													}
												};
			
			CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName)
											.registerChatConnectionClosingListener(
														capvChatConnectionClosingListener);
		}
		
	}
	
	/**
	 * This method is used to send chat message to receiver
	 * 
	 * @param message	chat message need to be send to receiver
	 * @param receiver	receiver of the message
	 * 
	 * @throws throws exception if it is unable to deliver the message
	 * 
	 */
	public void sendMessage(String message, String receiver) throws Exception {
		String buddyJID = receiver + "@" + CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName).getService();
        log.debug("Sending message with message  :{}     buddyJID  :{}",message,buddyJID);
        
        Chat chat = null;
        
        if(chatsMap.get(buddyJID) == null) {
        	chat = chatManager.createChat(buddyJID);
        	chatsMap.put(buddyJID, chat);
        } else
        	chat = chatsMap.get(buddyJID);
        
        chat.sendMessage(message);
    }
	
	public void addToChatsByThreadId(Chat chat) {
		if(chat != null) {
			List<Chat> chatList = null;
			if(chatsByThreadId.get(chat.getThreadID()) != null)
				chatList = chatsByThreadId.get(chat.getThreadID());
			else
				chatList = new ArrayList<>();
			chatList.add(chat);
			chatsByThreadId.put(chat.getThreadID(), chatList);
		}
	}

}

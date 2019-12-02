package com.capv.client.user.chat.handler;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.listener.CapvChatConnectionClosingListener;
import com.capv.client.user.chat.listener.CapvGroupMessageListener;
/**
 * <h1>Handler class for group chat</h1>
 * This handler class is used to handle group chat messages
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvGroupChatHandler {
	
	private String userName;
	
	private CapvChatConnectionClosingListener capvChatConnectionClosingListener;
	
	private static final Logger log = LoggerFactory.getLogger(CapvGroupChatHandler.class);
	/**
	 * this parameterized constructor is used to initialize the CapvGroupChatHandler
	 * @param capvChatUserRequestProcessor	The reference of CapvChatUserRequestProcessor
	 * 										@see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 * 
	 */
	public CapvGroupChatHandler(String userName) {
		
		this.userName = userName;
		initializeChatListeners();
	}
	
	/** 
	 * This method used to initialize the group chat listener to receive message from the group users and deliver them to the user
	 */
	private void initializeChatListeners() {
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
			
			CapvGroupMessageListener capvGroupMessageListener = new CapvGroupMessageListener(userName);
			chatUserConnection.addAsyncStanzaListener(capvGroupMessageListener, 
      	      												new StanzaTypeFilter(Message.class));
			
			capvChatConnectionClosingListener = (XMPPTCPConnection chatConnection) -> {
														chatConnection.removeAsyncStanzaListener(capvGroupMessageListener);
													};
			
			CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName)
											.registerChatConnectionClosingListener(
														capvChatConnectionClosingListener);
			
		}
		
	}
	/** 
	 * 
	 * This method used to send group message to group 
	 * @param room		The room name
	 * @param message	The message
	 * 
	 * @throws NoResponseException		if unable to receive response from the server
	 * @throws XMPPErrorException		if server throws exception during the message sending
	 * @throws NotConnectedException	if the connection is not established or broken with the server
	 */
	public void sendGroupMessage(String room,String message) throws NoResponseException, XMPPErrorException, NotConnectedException{
        
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	if(room != null && room.indexOf("@conference."+chatUserConnection.getServiceName()) < 0)
	    		room = room + "@conference." + chatUserConnection.getServiceName();
	    	
	    	Message msg = new Message(room,Message.Type.groupchat);
	        msg.setBody(message);
	        log.debug("message sent successfully    :{}",message);
	        try {
	        	chatUserConnection.sendStanza(msg);
			} catch (NotConnectedException e) {
				log.error("NotConnectedException occured while sending group message :",e);
				//e.printStackTrace();
			}
		}
    }

}

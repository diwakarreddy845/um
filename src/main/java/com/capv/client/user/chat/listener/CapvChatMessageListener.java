package com.capv.client.user.chat.listener;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.google.gson.JsonObject;

/**
 * <h1> CapvChatMessageListener </h1>
 * 
 *  A listener for processing user chat messages
 *  
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvChatMessageListener implements ChatMessageListener {
	
	private String userName;
	
	/**
	 * this parameterized constructor is used to initialize the CapvChatManagerListener
	 * @param capvChatUserRequestProcessor
	 * 
	 */
	public CapvChatMessageListener(String userName){
		
		this.userName = userName;
	}

	/**
	 * This method is used to receive the messages from receiver and deliver them to the users with their WebSocket connection
	 * @param Chat		A chat is a series of messages sent between two users
	 * @param message	the message
	 * 
	 */
	@Override
	public void processMessage(Chat chat, Message message) {
		
		String from = message.getFrom();
        String body = message.getBody();
        
        if(from != null && body != null) {
        	
        	String fromUser = from.substring(0, from.indexOf('@'));
        	if(body.contains("/getFile?fileName")) {
        		JsonObject messageToSend = new JsonObject();
    		
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_RECEIVE);
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, fromUser);
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, body);
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_FILE_SHARE, CapvClientUserConstants.WS_MESSAGE_FILE_REQUEST);
        		String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
    		
			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
    		
        }
       else {
        		JsonObject messageToSend = new JsonObject();
    		
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_RECEIVE);
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, fromUser);
        		String[] split_msg=body.split(":");
				 String msgid=split_msg[0];
				 String msg_type=split_msg[1];
				 String messageSplit=body.substring(msgid.length()+msg_type.length()+2);
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, messageSplit);
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_ID_KEY, msgid);
        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE, msg_type);
			 if(msg_type.equals("2")) {
				// String replyMessage=body.substring(msgid.length()+msg_type.length()+2);;
				 String[] split_msg2=messageSplit.split("##");
				 String reply_message_body=split_msg2[0];
				 messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY, reply_message_body);
				 messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, body.substring(msgid.length()+msg_type.length()+reply_message_body.length()+4));
			 }else {
				 messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, messageSplit);
			 }
        		
        		
        		String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
    		
			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
        	
        }
        }

	}

}

package com.capv.client.user.chat.listener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.delay.packet.DelayInformation;

import com.capv.client.user.chat.CapvChatClientConfiguration;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.google.gson.JsonObject;
/**
 * <h1>CapvGroupMessageListener </h1>
 * 
 * Provides a mechanism to listen for group messages and deliver them to the user
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvGroupMessageListener implements StanzaListener {

	private String userName;
	
	/**
	 * this parameterized constructor is used to initialize the CapvChatManagerListener
	 * @param capvChatUserRequestProcessor	The reference of CapvChatUserRequestProcessor
	 * 										@see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 * 
	 */
	public CapvGroupMessageListener(String userName){
		
		this.userName = userName;
	}
	
	/**
	 * This method is used to process the group message and deliver it to user 
	 * 
	 * @param packet the message packet to process and send deliver it to user.
	 * 
	 * @throws NotConnectedException if the connection is not established or broken with the server
	 */
	@Override
	public void processPacket(Stanza packet) throws NotConnectedException {
		
		Message message = (Message) packet;
		String from = message.getFrom();
		String to = packet.getTo();
		String body = message.getBody();
		/*System.out.println("from"+from+",to:"+to);
		System.out.println(message);
		System.out.println(message.getExtensions());
		System.out.println(message.getExtension("delay", "urn:xmpp:delay"));*/
		Date stamp = new Date();
		
		 DelayInformation delayInformation = message.getExtension("delay", "urn:xmpp:delay");
		 
		 if(delayInformation != null && delayInformation.getStamp() != null)
			 stamp = delayInformation.getStamp();
		 
		 CapvChatClientConfiguration capvChatClientConfiguration = CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName);
		 
		 if(capvChatClientConfiguration != null) {
			 if((packet.getFrom().contains("@conference."+capvChatClientConfiguration.getService()))){
					
					if(from.substring(from.indexOf('/')+1).equals(to.substring(0, to.indexOf('@')))){
					
						if(message.getExtension("delay", "urn:xmpp:delay") != null){
						
							/*System.out.println("from : "+packet.getFrom());
					        System.out.println("to : "+packet.getTo());
					        System.out.println("msg : "+message.getBody());
					        //System.out.println(packet.M);
			        		
			        		JsonObject messageToSend = new JsonObject();
			        		
			        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_RECEIVE);
			        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, from.substring(from.indexOf('/')+1));
			        		messageToSend.addProperty("room", from.substring(0, from.indexOf('@')));
			        		messageToSend.addProperty("stamp", stamp != null ? stamp.toString() : new Date().toString());
			        		messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, body);
			        		
			        		String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);*/
			        		
			        		//CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
						
						}
					} else {
						if(message.getExtension("delay", "urn:xmpp:delay") == null){
							if(body.contains("/getFile?fileName")) {
								JsonObject messageToSend = new JsonObject();
		        		
								DateFormat df = new SimpleDateFormat("d/M/yyyy H:m");
		        		
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_RECEIVE);
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_FILE_SHARE, CapvClientUserConstants.WS_MESSAGE_GROUP_FILE_REQUEST);
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, from.substring(from.indexOf('/')+1));
								messageToSend.addProperty("room", from.substring(0, from.indexOf('@')));
								messageToSend.addProperty("stamp", df.format(stamp));
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, body);
								String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
		        		
								CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
							}else {
								JsonObject messageToSend = new JsonObject();
				        		
								DateFormat df = new SimpleDateFormat("d/M/yyyy H:m");
		        		
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_GROUP_MESSAGE_RECEIVE);
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, from.substring(from.indexOf('/')+1));
								messageToSend.addProperty("room", from.substring(0, from.indexOf('@')));
								messageToSend.addProperty("stamp", df.format(stamp));
								 String[] split_msg=body.split(":");
								 String msgid=split_msg[0];
								 String msg_type=split_msg[1];
								 String messageSplit=body.substring(msgid.length()+msg_type.length()+2);
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, messageSplit);
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_ID_KEY, msgid);
								messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_TYPE, msg_type);
								 if(msg_type.equals("2")) {
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
		 }
		
	}

}

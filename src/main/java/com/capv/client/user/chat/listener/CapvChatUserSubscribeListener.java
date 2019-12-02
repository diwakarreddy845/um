package com.capv.client.user.chat.listener;

import java.util.Map;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import com.capv.client.user.User;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.chat.CapvChatUserRequestProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.util.ChatUserUtil;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
/**
 * <h1>Listener for user subscription requests</h1>
 * 
 * Provides a mechanism to listen for user subscription or friend requests, process the requests and deliver them to respective user
 * 
 * @author narendra.muttevi
 * @version 1.0.
 */
public class CapvChatUserSubscribeListener implements StanzaListener {
	
	private String userName;
	
	/**
	 * this parameterized constructor is used to initialize the CapvChatManagerListener
	 * @param capvChatUserRequestProcessor
	 * 
	 */
	public CapvChatUserSubscribeListener(String userName) {
		this.userName = userName;
	}

	/**
	 * This method is used to process the user subscription requests
	 * @param packet	The user subscription request packet
	 * @throws NotConnectedException	if the connection is not established or broken with the server
	 */
	@Override
	public void processPacket(Stanza packet) throws NotConnectedException {
		
		Presence pres = (Presence) packet;
		
        if (pres.getType() != null) {
        	
        	String presenceType = pres.getType().toString();
        	
        	JsonObject messageToSend = new JsonObject();
        	
        	CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
        								CapvChatClientManagerRegistry.getCapvChatUserRequestProcessorByUserName(userName);
        	
        	switch(presenceType) {
        		
        		case "subscribe":
				        		{
				        			messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_FRIEND_REQUEST);
					               	messageToSend.addProperty("from", pres.getFrom());
					               	
					               	String fromUserName = pres.getFrom().substring(0, pres.getFrom().indexOf('@'));
					               	
					               	Map<String, String> profilePictureData = null;
					               	
					               	try {
					               		profilePictureData = capvChatUserRequestProcessor.getUserProfilePicture(fromUserName);
					               		
					               		if(profilePictureData != null) {
					               			java.lang.reflect.Type profilePictureType = new TypeToken<Map<String, String>>(){}.getType();
											
											messageToSend.add("profilePicture", 
																CapvClientUserUtil.convertToJsonElement(profilePictureData, profilePictureType));
					               		}
					               	} catch (Exception e){}
					               	
					               	messageToSend.addProperty("userFullName", ChatUserUtil.getUserFullName(fromUserName));
					               	 
					               	String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
					        			
					       			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
					       			
					       			/*XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
					       		 
					       			if(chatUserConnection != null) {
					       				Presence friendSubscription = new Presence(Presence.Type.subscribed);
					       				friendSubscription.setTo(pres.getFrom()); 
					       		        chatUserConnection.sendStanza(friendSubscription);
					       			}*/
				        		}
        						break;
        						
        		case "subscribed":
				        		{
				        			//String subscribedUserName = pres.getFrom().substring(0, pres.getFrom().indexOf("@"));
				        			
				        			XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
				        			
				        			if(chatUserConnection != null) {
				        				Roster subscriberRoster = Roster.getInstanceFor(chatUserConnection);
		        						
					        			if(subscriberRoster != null && subscriberRoster.getPresence(pres.getFrom()) != null) {
					        				
					        				if(subscriberRoster.getEntry(pres.getFrom()).getType().equals(RosterPacket.ItemType.to)) {
					        					
					        					Presence presence = new Presence(Type.subscribed);
						        				presence.setTo(pres.getFrom());
						        				chatUserConnection.sendStanza(presence);
						        				
					        					RosterEntry subscriberEntry = subscriberRoster.getEntry(pres.getFrom());
					        					
					        					if(!subscriberEntry.getType().equals(RosterPacket.ItemType.none)) {
					        						User user = new User();
					        						
					        						user.setUser(subscriberEntry.getUser());
					        						user.setName(subscriberEntry.getUser().substring(0, 
					        																	subscriberEntry.getUser().indexOf("@")));
					        						user.setFullName(subscriberEntry.getName());
					        						
					        						Presence subscribedUserPresence = subscriberRoster.getPresence(subscriberEntry.getUser());
					        						
					        						if(subscribedUserPresence.getType() == Presence.Type.available)
					        							user.setStatus(subscribedUserPresence.getMode().toString());
					        						else
					        							user.setStatus(subscribedUserPresence.getType().toString());
					        						
					        						messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
					        													CapvClientUserConstants.WS_MESSAGE_FRIEND_SUBSCRIBED);
							        				
							        				java.lang.reflect.Type objType = new TypeToken<User>() {}.getType();
													
							        				messageToSend.add("user_subscribed", CapvClientUserUtil.convertToJsonElement(user, objType));
							        				
					        						String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
									       			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
									       			
									       			/*XMPPTCPConnection subscribedUserConnection = null;
									       			
									       			if(CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(subscribedUserName) != null) {
									       				subscribedUserConnection = CapvChatClientManagerRegistry
							       														.getChatUserConnectionByUserName(subscribedUserName);
									       			}
									       			
									       			if(subscribedUserConnection != null) {
									       				Roster subscribedUserRoster = Roster.getInstanceFor(subscribedUserConnection);
									       				RosterEntry newSubscriberEntry = subscribedUserRoster.getEntry(pres.getTo());
									       				
									       				if(newSubscriberEntry != null) {
									       					
									       					user = new User();
							        						
									       					user.setUser(newSubscriberEntry.getUser());
									       					user.setName(newSubscriberEntry.getUser().substring(0, 
									       																newSubscriberEntry.getUser().indexOf("@")));
									       					user.setFullName(newSubscriberEntry.getName());
							        						
							        						Presence userPresence = subscriberRoster.getPresence(newSubscriberEntry.getUser());
							        						
							        						if(userPresence.getType() == Presence.Type.available)
							        							user.setStatus(userPresence.getMode().toString());
							        						else
							        							user.setStatus(userPresence.getType().toString());
									       					
									       					messageToSend = new JsonObject();
									       					
									       					messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
									       												CapvClientUserConstants.WS_MESSAGE_FRIEND_SUBSCRIBED);
				        				
									        				messageToSend.add("user_subscribed", CapvClientUserUtil.convertToJsonElement(user, objType));
									        				
							        						userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
											       			CapvChatUserMessageProcessor.sendChatClientMessageToUser(subscribedUserName, userMessage);
									       					
									       				}
									       				
									       			}*/
					        					}
								               	
					        				}
					        			}
				        			}
				        			
				        		}
								break;
								
        		case "unsubscribe":
        			System.out.println("Unsubscribe");
								break;
								
        		case "unsubscribed":
				        		{
				        			messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
																CapvClientUserConstants.WS_MESSAGE_FRIEND_UNSUBSCRIBED);
				        			messageToSend.addProperty("from", pres.getFrom().substring(0, pres.getFrom().indexOf("@")));
				        			
				        			String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
					       			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
					       			
					       			messageToSend = new JsonObject();
					       			
					       			messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
					       										CapvClientUserConstants.WS_MESSAGE_FRIEND_UNSUBSCRIBED);
					       			messageToSend.addProperty("from", pres.getTo().substring(0, pres.getTo().indexOf("@")));
					       			userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
					       			CapvChatUserMessageProcessor.sendChatClientMessageToUser(pres.getFrom().substring(0, pres.getFrom().indexOf("@")), 
					       																		userMessage);
				        		}
								break;
								
				default: break;
        	}
        	 
        }
	}

}

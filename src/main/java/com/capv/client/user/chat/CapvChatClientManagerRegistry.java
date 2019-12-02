package com.capv.client.user.chat;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
/**
 * <h1> CapvChatClientManager </h1>
 * this class is used initialize the user connections
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class CapvChatClientManagerRegistry {
	
	private static Map<String, CapvChatClientManager> capvUserChatClients = new HashMap<>();
	
	public static void addToClientManagerRegistry(String userName, CapvChatClientManager capvChatClientManager) {
		capvUserChatClients.put(userName, capvChatClientManager);
		
		//System.out.println("No. of successful Chat connections::"+capvUserChatClients.size());
	}
	
	public static int getActiveConnectionsCount() {
		return capvUserChatClients.size();
	}
	
     /** 
      * This method is used to get the instance of CapvChatClientManager for a given user.
      * 
      * @param userName	The userName
      * 
      * @return returns the instance of CapvChatClientManager for a given user
      * 
      */
	public static CapvChatClientManager getCapvChatClientManagerByUser(String userName) {
		return capvUserChatClients.get(userName);
	}
	/**
	 * This method is used to return instance of CapvChatUserRequestProcessor which is used to 
	 * process the user chat messages, friend requests, presence changes and group management requests etc.
	 * 
	 * @return returns the instance of CapvChatUserRequestProcessor
	 * @see capvChatUserRequestProcessor
	 * 
	 */
	
	public static CapvChatClientConfiguration getChatClientConfigurationByUserName(String userName) {
		if(capvUserChatClients.get(userName) != null) {
			return capvUserChatClients.get(userName).getChatClientConfiguration();
		}
		return null;
	}
	
	public static XMPPTCPConnection getChatUserConnectionByUserName(String userName) {
		if(capvUserChatClients.get(userName) != null) {
			return capvUserChatClients.get(userName).getChatUserConnection();
		}
		return null;
	}
	
	public static CapvChatUserRequestProcessor getCapvChatUserRequestProcessorByUserName(String userName) {
		if(capvUserChatClients.get(userName) != null) {
			return capvUserChatClients.get(userName).getCapvChatUserRequestProcessor();
		}
		return null;
	}
	
	public static void closeUserChatManagerConnection(String userName) {
		if(capvUserChatClients.get(userName) != null) {
			CapvChatClientManager capvChatClientManager = capvUserChatClients.get(userName);
			capvChatClientManager.closeConnection();
			
			capvUserChatClients.remove(userName);
			
			System.out.println("No. of Chat connections alived::"+capvUserChatClients.size());
		}
	}
	
	public static void removeUserChatManagerConnection(String userName) {
		capvUserChatClients.remove(userName);
	}

}

package com.capv.client.user.chat;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.ping.PingManager;

import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.chat.listener.CapvChatClientAuthenticationCallbackListener;
import com.capv.client.user.chat.listener.CapvChatClientConnectionListener;
import com.capv.client.user.chat.listener.CapvChatConnectionClosingListener;
import com.capv.client.user.constants.CapvClientUserConstants.ChatClientAuthStatus;
/**
 * <h1> CapvChatClientManager </h1>
 * this class is used initialize the user connections
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class CapvChatClientManager {
	
	private final int packetReplyTimeout = 5000; // millis
	
	private CapvChatClientConfiguration capvChatClientConfiguration = null;
	
    private XMPPTCPConnection chatUserConnection;
    
    private CapvChatUserRequestProcessor capvChatUserRequestProcessor = null;
    
    private CapvChatClientAuthenticationCallbackListener capvChatClientAuthenticationCallbackListener;
    
    private CapvChatClientConnectionListener capvChatClientConnectionListener;
    
    private List<CapvChatConnectionClosingListener> chatConnectionCLosingListeners = new ArrayList<>();
	
    /**
	 * this parameterized constructor is used to initialize the CapvChatClientManager
	 * @param capvChatClientConfiguration	The chat client configuration which is require to connect the chat server
	 * 										@see com.capv.client.user.chat.CapvChatClientConfiguration
	 * 
	 */
	public CapvChatClientManager(CapvChatClientConfiguration 
												capvChatClientConfiguration) {
		this.capvChatClientConfiguration = capvChatClientConfiguration;
	}
	/**
     * creates XMPPTCPConnection and initiliazes it.
     * creates session for user.
     * Roster sets default subscriptionMode.
     * 
     * @throws Exception throws exception during connection initialization
     */
	public void initializeConnection() throws Exception {
		
		XMPPTCPConnectionConfiguration connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
																	.setSecurityMode(SecurityMode.disabled)
																	.setHost(capvChatClientConfiguration.getHost())
																	.setPort(capvChatClientConfiguration.getPort())
																	.setServiceName(capvChatClientConfiguration.getService())
																	.setUsernameAndPassword(capvChatClientConfiguration.getUserName(), 
																							capvChatClientConfiguration.getPassword())
																	.setConnectTimeout(5000)
																	.build();


		chatUserConnection = new XMPPTCPConnection(connectionConfiguration);
		chatUserConnection.setPacketReplyTimeout(packetReplyTimeout);
		
		Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
		
		this.capvChatClientAuthenticationCallbackListener = 
													(ChatClientAuthStatus chatClientAuthStatus) -> {
														
														if(chatClientAuthStatus == ChatClientAuthStatus.SUCCESS) {
															
															CapvChatClientManagerRegistry.addToClientManagerRegistry(capvChatClientConfiguration.getUserName(), this);
															
															capvChatUserRequestProcessor = new CapvChatUserRequestProcessor(capvChatClientConfiguration.getUserName());
															
															try {
																capvChatUserRequestProcessor.changeUserPresence(Presence.Mode.available.toString(), "Online");
															} catch (Exception e){}
														} else {
															closeConnection();
															
															CapvChatClientManagerRegistry.removeUserChatManagerConnection(capvChatClientConfiguration.getUserName());
														}
															
														UserSession userSession = UserRegistry.getUserSessionBySessionId(
																								capvChatClientConfiguration.getWebSoketSessionId());
						
														if(userSession != null) {
															userSession.setChatClientAuthStatus(chatClientAuthStatus);
														}
													};
		
		capvChatClientConnectionListener = new CapvChatClientConnectionListener(
													capvChatClientAuthenticationCallbackListener);
		chatUserConnection.addConnectionListener(capvChatClientConnectionListener);
		
		chatUserConnection.connect();
		
	}
	/**
	 * This method is used to return instance of CapvChatUserRequestProcessor which is used to 
	 * process the user chat messages, friend requests, presence changes and group management requests etc.
	 * 
	 * @return returns the instance of CapvChatUserRequestProcessor
	 * @see capvChatUserRequestProcessor
	 * 
	 */
	public CapvChatUserRequestProcessor getCapvChatUserRequestProcessor() {
		return capvChatUserRequestProcessor;
	}
	/**
	 * checks chat connection is alive or not
	 * @return the chat connection status
	 *
	 */
	public boolean isChatConnectionAlive() {
		return (chatUserConnection != null && chatUserConnection.isConnected());
	}
	
	public XMPPTCPConnection getChatUserConnection() {
		return this.chatUserConnection;
	}
	
	public CapvChatClientConfiguration getChatClientConfiguration() {
		return this.capvChatClientConfiguration;
	}
	
	public void registerChatConnectionClosingListener(
					CapvChatConnectionClosingListener capvChatConnectionClosingListener) {
		this.chatConnectionCLosingListeners.add(capvChatConnectionClosingListener);
	}

	private void triggerConnectionClosingListeners() {
		for(CapvChatConnectionClosingListener connectionClosingListener 
											:chatConnectionCLosingListeners) {
			connectionClosingListener.processPreConnectionCloseRequest(chatUserConnection);
		}
	}
	
	protected void finalize() throws Throwable {
		chatUserConnection = null;
		capvChatClientConfiguration = null;
		capvChatClientAuthenticationCallbackListener = null;
		capvChatClientConnectionListener = null;
		capvChatUserRequestProcessor = null;
	}
	
	/**
	 * This method is used to close the chat service connection
	 * 
	 */
	@SuppressWarnings("unchecked")
	public void closeConnection() {
		
		if(chatUserConnection != null) {
			
			chatUserConnection.removeConnectionListener(this.capvChatClientConnectionListener);
			triggerConnectionClosingListeners();
			this.capvChatClientAuthenticationCallbackListener = null;
			this.capvChatClientConnectionListener = null;
			this.capvChatUserRequestProcessor = null;
			
			if(chatUserConnection.isConnected())
				chatUserConnection.disconnect();
			
			try {
			    final Field pm_INSTANCES = PingManager.class.getDeclaredField("INSTANCES");
			    pm_INSTANCES.setAccessible(true);
			    
			    final Map<XMPPConnection, PingManager> pm_INSTANCES_Map = (Map<XMPPConnection, PingManager>) pm_INSTANCES.get(null);
			    final PingManager pingManager = pm_INSTANCES_Map.remove(chatUserConnection);
			    
			    final Field rm_INSTANCES = ReconnectionManager.class.getDeclaredField("INSTANCES");
			    rm_INSTANCES.setAccessible(true);
			    
			    final Map<XMPPConnection, ReconnectionManager> rm_INSTANCES_Map = (Map<XMPPConnection, ReconnectionManager>) rm_INSTANCES.get(null);
			    rm_INSTANCES_Map.remove(chatUserConnection);
			    
			    if (pingManager != null) {
			        final Field f_executorService = PingManager.class.getDeclaredField("executorService");
			        f_executorService.setAccessible(true);
			        final ScheduledExecutorService executorService = (ScheduledExecutorService) f_executorService.get(pingManager);
			        executorService.shutdown();
			    }
			} catch (Exception e){}
			
		}
		
	}
	
	public void reconnectChatConnection() throws Exception {
		this.capvChatClientAuthenticationCallbackListener = 
						(ChatClientAuthStatus chatClientAuthStatus) -> {
							
							if(chatClientAuthStatus == ChatClientAuthStatus.SUCCESS) {
								
								CapvChatClientManagerRegistry.addToClientManagerRegistry(capvChatClientConfiguration.getUserName(), this);
								
								capvChatUserRequestProcessor = new CapvChatUserRequestProcessor(capvChatClientConfiguration.getUserName());
								
								try {
									capvChatUserRequestProcessor.changeUserPresence(Presence.Mode.available.toString(), "Online");
								} catch (Exception e){}
							}
							UserSession userSession = UserRegistry.getUserSessionBySessionId(
																	capvChatClientConfiguration.getWebSoketSessionId());
		
							if(userSession != null) {
								userSession.setChatClientAuthStatus(chatClientAuthStatus);
							}
						};
		
		capvChatClientConnectionListener = new CapvChatClientConnectionListener(
													capvChatClientAuthenticationCallbackListener);
		chatUserConnection.addConnectionListener(capvChatClientConnectionListener);
		chatUserConnection.connect();
	}

}

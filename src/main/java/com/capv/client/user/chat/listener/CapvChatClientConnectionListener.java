package com.capv.client.user.chat.listener;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import com.capv.client.user.constants.CapvClientUserConstants.ChatClientAuthStatus;
/**
 * <h1>Listener for chat server connection status</h1>
 * 
 * This class is used to listen chat server connection status
 * This class fires events upon connection established, user authentication success, connection closed, connection reestablished etc.
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvChatClientConnectionListener implements ConnectionListener {
	
	private CapvChatClientAuthenticationCallbackListener capvChatAuthenticationStatusHandler;
	
	public CapvChatClientConnectionListener(CapvChatClientAuthenticationCallbackListener 
												capvChatAuthenticationStatusHandler) {
		this.capvChatAuthenticationStatusHandler = capvChatAuthenticationStatusHandler;
	}
	
	/**
	 * 
	 * Notification that the connection has been successfully connected to the remote endpoint (e.g. the XMPP server). 
	 * Note that the connection is likely not yet authenticated and therefore only limited operations like registering an account may be possible.
	 * @param connection the XMPPConnection which successfully connected to its endpoint.
	 */
	@Override
	public void connected(XMPPConnection connection){
		
		//System.out.println("Chat connection initialized successfully");
		
		try {
			
			((XMPPTCPConnection)connection).login();
		} catch (Exception e){
			
			System.out.println("Chat connection auth request failed");
			e.printStackTrace();
			if(capvChatAuthenticationStatusHandler != null)
				capvChatAuthenticationStatusHandler.processStatus(ChatClientAuthStatus.FAIL);
		}

	}

	/**
	 * Notification that the connection has been authenticated.
	 * @param connection the XMPPConnection which successfully authenticated.
	 * @param resumed true if a previous XMPP session's stream was resumed.
	 * 
	 */
	@Override
	public void authenticated(XMPPConnection connection, boolean resumed) {
		//System.out.println("Chat connection autheticated successfully");
		if(capvChatAuthenticationStatusHandler != null)
			capvChatAuthenticationStatusHandler.processStatus(ChatClientAuthStatus.SUCCESS);
	}

	/**
	 * process to close the Chat connection.
	 */
	@Override
	public void connectionClosed() {
		System.out.println("Chat connection closed");

	}

	/**
	 * Notification that the connection was closed due to an exception. When abruptly disconnected it is possible for the connection to try reconnecting to the server.
	 * @param e the exception.
	 */
	@Override
	public void connectionClosedOnError(Exception e) {
		System.out.println("Chat connection closed due to error::"+e.getMessage());
		if(capvChatAuthenticationStatusHandler != null)
			capvChatAuthenticationStatusHandler.processStatus(ChatClientAuthStatus.FAIL);
		//e.printStackTrace();
		//capvChatAuthenticationStatusHandler.processStatus(ChatClientAuthStatus.ERROR);

	}

	/**
	 * process to reconnect the connection
	 */
	@Override
	public void reconnectionSuccessful() {
		System.out.println("Chat re-connection successful");
	}

	/**
	 * The connection will retry to reconnect in the specified number of seconds
	 * @param seconds remaining seconds before attempting a reconnection.
	 */
	@Override
	public void reconnectingIn(int seconds) {
		System.out.println("Chat connection re-connecting in " + seconds + " seconds");

	}

	/**
	 *An attempt to connect to the server has failed. The connection will keep trying reconnecting to the server in a moment
	 *@param e the exception that caused the reconnection to fail.
	 */
	@Override
	public void reconnectionFailed(Exception e) {
		System.out.println("Chat re-connection failed");
	}

}

package com.capv.client.user.chat;

import com.capv.um.service.UserService;

/**
 * This class is used to define the chat server configuration on behalf of the user to connect with chat server
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class CapvChatClientConfiguration {
	
	private String userName;
	private String password;
	private String service;
	private String host;
	private int port;
	
	private String webSoketSessionId;
	
	private UserService userService;
	
	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getService() {
		return service;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}
	
	public String getWebSoketSessionId() {
		return webSoketSessionId;
	}
	
	public UserService getUserService() {
		return userService;
	}
	
	public static ConnectionConfigurationBuilder connectionConfigurationBuilder() {
		return new ConnectionConfigurationBuilder();
	}
	
	private CapvChatClientConfiguration(ConnectionConfigurationBuilder connectionConfigurationBuilder){
		
		this.host				= connectionConfigurationBuilder.host;
		this.port				= connectionConfigurationBuilder.port;
		this.service			= connectionConfigurationBuilder.service;
		this.userName			= connectionConfigurationBuilder.userName;
		this.password			= connectionConfigurationBuilder.password;
		this.webSoketSessionId	= connectionConfigurationBuilder.webSoketSessionId;
		this.userService		= connectionConfigurationBuilder.userService;
		
	}

	/**
	 * 
	 * This class is used to build the chat server configuration with the given user and server details
	 * 
	 * @author ganesh.maganti
	 * @version 1.0
	 */
	public static class ConnectionConfigurationBuilder {
		
		private String userName;
		private String password;
		private String service;
		private String host;
		private int port;
		
		private String webSoketSessionId;
		
		private UserService userService;
		
		private ConnectionConfigurationBuilder(){}
		
		public ConnectionConfigurationBuilder setUserName(String userName) {
			this.userName = userName;
			return this;
		}

		public ConnectionConfigurationBuilder setPassword(String password) {
			this.password = password;
			return this;
		}

		public ConnectionConfigurationBuilder setService(String service) {
			this.service = service;
			return this;
		}

		public ConnectionConfigurationBuilder setHost(String host) {
			this.host = host;
			return this;
		}

		public ConnectionConfigurationBuilder setPort(int port) {
			this.port = port;
			return this;
		}
		
		public ConnectionConfigurationBuilder setWebSocketSessionId(String webSocketSessionId) {
			this.webSoketSessionId = webSocketSessionId;
			return this;
		}
		
		public ConnectionConfigurationBuilder setUserService(UserService userService) {
			this.userService = userService;
			return this;
		}
		
		public CapvChatClientConfiguration build() {
			return new CapvChatClientConfiguration(this);
		}
		
	}

}

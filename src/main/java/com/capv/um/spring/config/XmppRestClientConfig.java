package com.capv.um.spring.config;

import org.igniterealtime.restclient.RestApiClient;
import org.igniterealtime.restclient.entity.AuthenticationToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XmppRestClientConfig {

	@Value("${xmpp.url}")
	private String url;
	
	@Value("${xmpp.port}")
	private Integer port;
	
	@Value("${xmpp.user}")
	private String user;
	
	@Value("${xmpp.pass}")
	private String password;
	
	@Bean
	public RestApiClient restApiClient() {
		
		AuthenticationToken authnticationToken = new AuthenticationToken(user, password);
		
		RestApiClient restApiClient = new RestApiClient(url, port, authnticationToken);
		
		return restApiClient;
	}
}

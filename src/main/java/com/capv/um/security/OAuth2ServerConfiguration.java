/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.capv.um.security;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

import com.capv.um.constants.CapvConstants;
import com.capv.um.util.CapvUtil;
import com.capv.um.web.filter.RequestPreProcessFilter;

@Configuration
public class OAuth2ServerConfiguration {

	private static final String RESOURCE_ID = "restservices";

	@Configuration
	@EnableResourceServer
	public static class ResourceServerConfiguration extends
			ResourceServerConfigurerAdapter {
		
		@Autowired
		private RequestPreProcessFilter requestPreProcessFilter;
		@Override
		public void configure(ResourceServerSecurityConfigurer resources) {
			// @formatter:off
			resources
				.resourceId(RESOURCE_ID);
			// @formatter:on
		}

		@Override
		public void configure(HttpSecurity http) throws Exception {
			// @formatter:off
			http
				.addFilterBefore(requestPreProcessFilter, RequestHeaderAuthenticationFilter.class)
				.authorizeRequests()
					 /* UserController services */
					.antMatchers(HttpMethod.GET, "/user/getOAuthClientDetails").permitAll()
					.antMatchers(HttpMethod.GET, "/user/all/pagination/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/user/resetPassword/**").permitAll()
					.antMatchers(HttpMethod.GET, "/user/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.POST, "/user/register").permitAll()
					.antMatchers(HttpMethod.POST, "/user/registerUser").permitAll()
					.antMatchers(HttpMethod.PUT, "/user/update").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.POST, "/user/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.PUT, "/user/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.DELETE, "/user/**").hasRole(CapvConstants.USER_ROLE)
					
					/* AppController services */
					.antMatchers(HttpMethod.POST, "/sendContactUsMail").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.POST, "/sendEmailToAdmin").permitAll()
					.antMatchers(HttpMethod.POST, "/sendInvite").permitAll()
				
					/* OfMessageArchiveController services */
					.antMatchers(HttpMethod.GET, "/client/getChatHistory/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/client/getArchiveHistory/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/client/getArchiveGroupHistory/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/client/getLastMessage/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.POST, "/client/registerOfMessageArchive").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/client/getOfMessageArchive/**").hasRole(CapvConstants.USER_ROLE)
					
					/* ProfileImagesFetchController services */
					.antMatchers(HttpMethod.POST, "/profileFetch/getRosterUsersProfilePictures").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.POST, "/profileFetch/getTotalUsers").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.POST, "/profileFetch/searchUser").hasRole(CapvConstants.USER_ROLE)
					
					/* TurnController services */
					.antMatchers(HttpMethod.POST, "/turn").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.POST, "/saveClientNetworkDetails").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/downloadFile").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/uploadFile").hasRole(CapvConstants.USER_ROLE)
					
					/* VideoRecodringController services */
					.antMatchers(HttpMethod.GET, "/roomList/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.POST, "/updateS3path/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/video/getVideo/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.PUT, "/updateSchedular/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/video/playback/gridSupport").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/video/getRecVideo/**").hasRole(CapvConstants.USER_ROLE)
					/* ScheduleMeeting Services*/
					.antMatchers(HttpMethod.POST, "/scheduleMeeting/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.PUT, "/pushMeetingProperties/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/getScheduleMeetings/**").hasRole(CapvConstants.USER_ROLE)
					
					/* CallDataStatsController services */
					.antMatchers(HttpMethod.GET, "/callDataStats/byUserCallStatsReport/").hasRole(CapvConstants.USER_ROLE)
					
					/*meeting contollers*/
					.antMatchers(HttpMethod.POST, "/createTryItRoom/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/getTryItRoom/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/getValidTryItRooms/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/deleteTryItRoom/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/timezone/getTimeZone").permitAll()
					.antMatchers(HttpMethod.GET, "/validateTryItRoomSession/**").permitAll()
					
					/*File share controller services*/
					.antMatchers(HttpMethod.POST, "/uploads/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/chunksdone/**").hasRole(CapvConstants.USER_ROLE)
					.antMatchers(HttpMethod.GET, "/getFile/**").permitAll()
					/*Configuration OnDemand controller services*/
					.antMatchers(HttpMethod.GET, "/conf/reLoadConfiguration/**").hasRole(CapvConstants.USER_ROLE);
			
				
			// @formatter:on
		}

	}

	@Configuration
	@EnableAuthorizationServer
	public static class AuthorizationServerConfiguration extends
			AuthorizationServerConfigurerAdapter {
		
		@Autowired
		TokenStore tokenStore;

		@Autowired
		@Qualifier("authenticationManagerBean")
		private AuthenticationManager authenticationManager;

		@Autowired
		private UserDetailsService userDetailsService;
		

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints)
				throws Exception {
			
			endpoints
				.tokenStore(this.tokenStore)
				.authenticationManager(this.authenticationManager)
				.userDetailsService(userDetailsService);
		}

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			
			clients
				.withClientDetails(oauthClientDetailsService())
				.build();
				/*.inMemory()
					.withClient("capv")
						.authorizedGrantTypes("password", "refresh_token")
						.authorities("USER")
						.scopes("read", "write")
						.resourceIds(RESOURCE_ID)
						.secret("capv")
						.accessTokenValiditySeconds(1800);*/
		}
		
		@Bean
		@Primary
		public DefaultTokenServices tokenServices() {
			
			DefaultTokenServices tokenServices = new DefaultTokenServices();
			tokenServices.setSupportRefreshToken(true);
			tokenServices.setTokenStore(this.tokenStore);
			
			return tokenServices;
		}
		
		@Bean
		public TokenStore tokenStore() {
			return new InMemoryTokenStore();
		}
		
		@Bean
		public OAuthClientDetailsService oauthClientDetailsService() {
			return new OAuthClientDetailsService();
		}
		
		@PostConstruct
		public void setCapvTokenStore() {
			CapvUtil.setTokenStore(tokenStore);
		}
		
	}

}

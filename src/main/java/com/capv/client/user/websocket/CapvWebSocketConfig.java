package com.capv.client.user.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 
 * <h1>Capv WebSocket Config</h1>
 * 
 * This class is used as spring configuration class for the WebSocket implementation
 * This class implements the org.springframework.web.socket.config.annotation.WebSocketConfigurer
 * which is used to configure WebScocket implementation
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
@Configuration
@EnableWebSocket
public class CapvWebSocketConfig implements WebSocketConfigurer {

	/**
	 * This method used to configure CapvWebSocketEndpoint as a spring bean
	 * 
	 * @return CapvWebSocketEndpoint	returns the spring bean reference to the CapvWebSocketEndpoint
	 * 									@see com.capv.client.user.websocket.CapvWebSocketEndpoint
	 */
	@Bean
	public WebSocketHandler capvWebSocketEndpoint() {
		return new CapvWebSocketEndpoint();
	}
	
	/*@Autowired
	@Qualifier("tryItWebSocketEndpoint")
	WebSocketHandler tryItWebSocketEndpoint;*/
	
	@Bean
	public WebSocketHandler tryItWebSocketEndpoint() {
		return new TryItWebSocketEndpoint();
	}
	
	/**
	 * This method used to configure WebSocketHandshakeInterceptor as a spring bean
	 * 
	 * @return WebSocketHandshakeInterceptor	returns the spring bean reference to the WebSocketHandshakeInterceptor
	 * 											@see com.capv.client.user.websocket.WebSocketHandshakeInterceptor
	 */
	@Bean
	public WebSocketHandshakeInterceptor webSocketHandshakeInterceptor() {
		return new WebSocketHandshakeInterceptor();
	}
	
	/*@Bean
	public CapvSystemStatisticsWebSocketEndpoint capvSystemStatisticsWebSocketEndpoint() {
		return new CapvSystemStatisticsWebSocketEndpoint();
	}*/

	/**
	 * This method used to register websocket handlers for websocket message processing
	 * 
	 * @param registry	This parameter injected by spring container as dependency 
	 * 					and used to add handler for process websocket messages
	 */
	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		System.out.println("****************Register WebSocket Handlers************");
		registry.addHandler(capvWebSocketEndpoint(), "/capv")
					.setAllowedOrigins("*")
					.addInterceptors(webSocketHandshakeInterceptor());
		registry.addHandler(tryItWebSocketEndpoint(), "/tryit")
		.setAllowedOrigins("*")
		.addInterceptors(webSocketHandshakeInterceptor());
				/*.addHandler(capvSystemStatisticsWebSocketEndpoint(), "/capv/systemstats")
					.setAllowedOrigins("*");*/
	}
}

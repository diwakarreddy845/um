package com.capv.client.user.websocket;

import java.util.Date;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.cache.UserCacheManager;
import com.capv.um.model.AuditLog;
import com.capv.um.model.User;
import com.capv.um.security.UserRepositoryUserDetails;
import com.capv.um.service.AuditlogService;
import com.capv.um.service.UserService;

/**
 * <h1> WebSocket Handshake Interceptor </h1>
 * 
 * This class used to validate user authentication with OAuth token which is passing 
 * as part of WebSocket URL during connection open from the client
 * <p>
 * This class extends org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor 
 * to provide the support for intercepting WebSocket open request before connection established
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class WebSocketHandshakeInterceptor extends HttpSessionHandshakeInterceptor{
	
	@Autowired
	TokenStore tokenStore;
	@Autowired
	private Environment environment;
	@Autowired
	private UserService userService;
	@Autowired
	private AuditlogService auditlogService;
	
	@Autowired
	private UserCacheManager userCacheManager;
	/**
	 * This method invoked before the handshake is processed.
	 * 
	 * @param request the current request
	 * @param response the current response
	 * @param wsHandler the target WebSocket handler
	 * @param attributes from the HTTP handshake to associate with the WebSocket session; the provided attributes are copied, the original map is not used.
	 */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
            ServerHttpResponse response, WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {
    	
    	boolean result = true;
    	
        super.beforeHandshake(request, response, wsHandler, attributes);
        
        
        if(request instanceof ServletServerHttpRequest) {
        	
        	HttpServletRequest httpRequest = ((ServletServerHttpRequest)request).getServletRequest();
        	
        	if(httpRequest.getServletPath().endsWith("/tryit")) {
        		String userName		= httpRequest.getParameter("userName");
        		
        		if(userName != null && userName.trim().length() > 0)
        			attributes.put("userName", userName);
        		else
        			return false;
        		return result;
        	}
        	
        	String accessToken		= httpRequest.getParameter("token");
        	String requestSource	= httpRequest.getParameter("requestSource");
        	String tokenId= httpRequest.getParameter("tokenId");
        	String lastSigninOs = httpRequest.getParameter("lastSigninOs");
        String loggedInIP=	httpRequest.getRemoteAddr();
     
            if(accessToken != null) {
            	OAuth2AccessToken oauth2AccessToken;
            	
            	oauth2AccessToken = tokenStore.readAccessToken(accessToken);
                
            	if(oauth2AccessToken == null || oauth2AccessToken.isExpired()) {
            		response.setStatusCode(HttpStatus.UNAUTHORIZED);
                	return false;
            	} else {
            		OAuth2Authentication oauth2Authentication = tokenStore.readAuthentication(oauth2AccessToken);
            		
            		if(oauth2Authentication != null && oauth2Authentication.getUserAuthentication() != null && 
        					oauth2Authentication.getUserAuthentication().getPrincipal() != null && 
        					oauth2Authentication.getUserAuthentication().getPrincipal() instanceof UserRepositoryUserDetails) {
            			
            			UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails)oauth2Authentication.getUserAuthentication().getPrincipal();
            			
            			OAuth2RefreshToken refreshToken = oauth2AccessToken.getRefreshToken();
            			OAuth2Authentication refreshTokenAuthentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
            			
            			String userName = userDetails.getUsername();
            			String password = (String)refreshTokenAuthentication.getUserAuthentication().getCredentials();
            			Long clientId	= userDetails.getClientId();
            			
            			attributes.put("userId", userDetails.getId());
                		attributes.put("userName", userName);
                		attributes.put("password", password);
                		attributes.put("clientId", clientId);
                		attributes.put("lastSigninOs", lastSigninOs);
                		attributes.put("tokenId", tokenId);
                		attributes.put("accessToken", accessToken.replaceAll("-", "")); 
                		
                		
                		if(CapvClientUserUtil.getClientConfigProperty(clientId, "capv.encryption")!=null) {
                			String propertyValue = CapvClientUserUtil.getClientConfigProperty(clientId, "capv.encryption");
                			attributes.put("encrytion", propertyValue); 
                		}else {
                			attributes.put("encrytion", "disabled"); 
                		}
                		
                		Long maxPingPongTime=Long.parseLong(environment.getProperty("pingpong"));
                		long currentTimestamp = System.currentTimeMillis();
            		    
            		    
                		User user=userService.getById(userDetails.getId());
                		user.setLogged_in_state((byte)0);
                		if(lastSigninOs!=null&&tokenId!=null) {
                			user.setLastSigninOs(lastSigninOs);
                			user.setTokenId(tokenId);
                		}
                		if(user.getPing()!=null) {
                			long lastPongMessageReceived = user.getPing().getTime();
                			if((currentTimestamp - lastPongMessageReceived) > maxPingPongTime ) {
                				user.setCallStatus((byte)2);
                			}
                		}
                		user.setPing(new Date());
                		user.setLastSigninOs(lastSigninOs);
                		userService.update(user, false);
                		 AuditLog  logger  = new AuditLog(httpRequest.getRequestURI(), "Login", userName,"", new Date(), loggedInIP,"Oauth/Login");
                		 auditlogService.saveLog(logger);
                		userCacheManager.addToUserCache(user);
                		if(requestSource != null)
                			attributes.put("requestSource", requestSource);
            		}
            	}
            }else {
            	response.setStatusCode(HttpStatus.UNAUTHORIZED);
            	return false;
            }
        } else {
        	return false;
        }
        
        
        return result;
    }

    /**
	 * This method invoked after the handshake is done. The response status and headers indicate
	 * the results of the handshake, i.e. whether it was successful or not.
	 * @param request the current request
	 * @param response the current response
	 * @param wsHandler the target WebSocket handler
	 * @param ex an exception raised during the handshake, or {@code null} if none
	 */
    @Override
    public void afterHandshake(ServerHttpRequest request,
            ServerHttpResponse response, WebSocketHandler wsHandler,
            Exception ex) {
        System.out.println("After Handshake");
        super.afterHandshake(request, response, wsHandler, ex);
    }

}
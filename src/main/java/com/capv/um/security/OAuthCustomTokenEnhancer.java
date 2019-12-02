package com.capv.um.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class OAuthCustomTokenEnhancer implements TokenEnhancer {

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		
		Map<String, Object> additionalInfo = new HashMap<>();
		Authentication userAuthentication = authentication.getUserAuthentication();
		
		if(userAuthentication instanceof UsernamePasswordAuthenticationToken)
			additionalInfo.put("client-id", 1000);
		
		return accessToken;
	}

}

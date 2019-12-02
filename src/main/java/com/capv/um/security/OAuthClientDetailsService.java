package com.capv.um.security;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.NoSuchClientException;

import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;

public class OAuthClientDetailsService implements ClientDetailsService {

	@Override
	public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
		
		ClientDetails clientDetails = CapvClientUserUtil.getOAuthClientDetails(
																CapvClientUserConstants.GET_OAUTH_CLIENT_DETAILS_BY_CLIENT_ID, 
																clientId);
		
		if (clientDetails == null) {
			throw new NoSuchClientException("No client with requested id: " + clientId);
	    }
		
		return clientDetails;
	}

}

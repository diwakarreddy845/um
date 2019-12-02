package com.capv.um.web.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.endpoint.TokenEndpointAuthenticationFilter;

public class CustomTokenEndpointAuthenticationFilter extends TokenEndpointAuthenticationFilter {

	public CustomTokenEndpointAuthenticationFilter(AuthenticationManager authenticationManager,
			OAuth2RequestFactory oAuth2RequestFactory) {
		super(authenticationManager, oAuth2RequestFactory);
	}
	
	@Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        super.doFilter(req, res, chain);
    }
	
	protected void onSuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			Authentication authResult) throws IOException {
		
		System.out.println("Authentication Success::"+authResult.getName());
		System.out.println("Authentication success password::"+request.getParameter("password"));
	}

	protected void onUnsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException {
		
		System.out.println("Authentication Failure::"+failed.getMessage());
	}

}

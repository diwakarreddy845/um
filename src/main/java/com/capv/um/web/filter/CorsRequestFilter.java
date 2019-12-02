package com.capv.um.web.filter;

import java.io.IOException;



import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CorsRequestFilter implements Filter {

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;
		
		String origin = request.getHeader("Origin");
		
		response.setHeader("Access-Control-Allow-Origin", origin);
		
		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			
			response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
			response.setHeader("Access-Control-Max-Age", "1");
			response.setHeader("Access-Control-Allow-Headers", "x-requested-with, Authorization, Content-Type");
			
		    response.setStatus(HttpServletResponse.SC_OK);
		} else {
		    chain.doFilter(req, res);
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}

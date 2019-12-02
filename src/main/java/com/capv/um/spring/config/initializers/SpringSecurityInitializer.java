package com.capv.um.spring.config.initializers;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

import com.capv.um.web.filter.CorsRequestFilter;

public class SpringSecurityInitializer extends AbstractSecurityWebApplicationInitializer 
{
	@Override
	protected void beforeSpringSecurityFilterChain(ServletContext servletContext) {
		FilterRegistration corsFilterReg = servletContext.addFilter("corsRequestFilter", new CorsRequestFilter());
		corsFilterReg.addMappingForUrlPatterns(null, false, "/*");
	}
}
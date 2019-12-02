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

package com.capv.um.rest.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.capv.um.model.User;
import com.capv.um.security.UserRepositoryUserDetails;
import com.capv.um.service.UserService;

/**
 * <h1>CustomUserDetailsService</h1>
 * this class is used to perform user custom operations
 * @author narendra.muttevi
 * @version 1.0
 */
@Service
public class CustomUserDetailsService implements UserDetailsService, ApplicationListener<AuthenticationSuccessEvent> {

	@Autowired
	private UserService userService;
	
	/**
	 *this class is used to get the record by userName
	 *@param userName the userName
	 */
	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		
		/*ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		
		HttpServletRequest request = requestAttributes.getRequest();*/
		
		User user = userService.getByUserName(userName, false);
		if (user == null) {
			throw new UsernameNotFoundException(String.format("User %s does not exist!", userName));
		}
		
		UserRepositoryUserDetails userDetails = new UserRepositoryUserDetails(user);
		
		return userDetails;
	}

	@Override
	public void onApplicationEvent(AuthenticationSuccessEvent event) {
		
		/*if(event.getAuthentication() != null && event.getAuthentication().getPrincipal() != null && 
				event.getAuthentication().getPrincipal() instanceof UserRepositoryUserDetails) {
			
			UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails)event.getAuthentication().getPrincipal();
			
			if(userDetails.getCallStatus() == null || 
					userDetails.getCallStatus() == 0 || 
					UserState.NOTLOGGEDIN.getStateByStateId(userDetails.getCallStatus()).equals(UserState.NOTLOGGEDIN.getState())) {
				
				User user = userService.getById(userDetails.getId());
				
				if(user != null) {
					user.setCallStatus(UserState.IDLE.getStateId());
					userService.update(user);
				}
				
			}
		}*/
		
	}
	
}

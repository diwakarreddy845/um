package com.capv.um.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.capv.um.model.User;

public final class UserRepositoryUserDetails extends User implements UserDetails {

	private static final long serialVersionUID = 1L;
	
	public UserRepositoryUserDetails(User user) {
		super(user);
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		GrantedAuthority grantedAuthority = new GrantedAuthority() {
			
			private static final long serialVersionUID = 1L;

			@Override
			public String getAuthority() {
				return "ROLE_USER";
			}
		};
		
		List<GrantedAuthority> roles = new ArrayList<GrantedAuthority>();
		roles.add(grantedAuthority);
		
		return roles;
	}

	@Override
	public String getUsername() {
		return getUserName();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return super.getActive();
	}

	@Override
	public String getPassword() {
		return super.getPassword();
	}

}

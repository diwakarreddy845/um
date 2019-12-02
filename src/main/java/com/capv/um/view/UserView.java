package com.capv.um.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserView {
	
	private Long clientId;
	private String userName;
	private String password;
	private String name;
	private String email;
	private String mobile;
	
	
	public UserView(){
		
	}
	
	public UserView(Long clientId, String userName, 
					String password, String name, 
					String email, String mobile) {
		
		this.clientId = clientId;
		this.userName = userName;
		this.password = password;
		this.name = name;
		this.email = email;
		this.mobile = mobile;
	}
	
	public Long getClientId() {
		return clientId;
	}
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

}

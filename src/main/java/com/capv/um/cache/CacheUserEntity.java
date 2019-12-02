package com.capv.um.cache;

public class CacheUserEntity {
	
	private Long id;
	private String userName;
	private String firstName;
	private String lastName;
	private String email;
	private String mobile;
	private String registrationSource;
	private String clientId;
	
	public CacheUserEntity(Long id, String userName, String firstName, String lastName, 
							String email, String mobile, String registrationSource, String clientId) {
		super();
		this.id = id;
		this.userName = userName;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.mobile = mobile;
		this.registrationSource = registrationSource;
		this.clientId =  clientId; 
	}
	
	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
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
	public String getRegistrationSource() {
		return registrationSource;
	}
	public void setRegistrationSource(String registrationSource) {
		this.registrationSource = registrationSource;
	}
	
}
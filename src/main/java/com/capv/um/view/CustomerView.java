package com.capv.um.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerView {

	private Long customerId;
	private String userName;
	private String password;
	private String email;
	private String regType;
	private String osType;
	private String deviceId;
	private String osVersion;
	
	public CustomerView() {
	
	}

	public CustomerView(Long customerId, String userName, String password, String email, String regType, String osType,
			String deviceId, String osVersion) {
		super();
		this.customerId = customerId;
		this.userName = userName;
		this.password = password;
		this.email = email;
		this.regType = regType;
		this.osType = osType;
		this.deviceId = deviceId;
		this.osVersion = osVersion;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRegType() {
		return regType;
	}

	public void setRegType(String regType) {
		this.regType = regType;
	}

	public String getOsType() {
		return osType;
	}

	public void setOsType(String osType) {
		this.osType = osType;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getOsVersion() {
		return osVersion;
	}

	public void setOsVersion(String osVersion) {
		this.osVersion = osVersion;
	}

	
}

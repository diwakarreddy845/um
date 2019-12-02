package com.capv.client.user;

import java.util.Map;

import com.google.gson.JsonObject;

/**
 * <h1> Model class for user friend entry </h1>
 * This class is defined as a model class for user friends with their presence status and send it as the response
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class User {
	
	private String name;
	private String user;
	private String fullName;
	private String email;
	private String mobile;
	private Boolean statusPending = false;
	private String status;
	private String registrationSource;
	private Map<String, String> profilePicture;
	private JsonObject privacy;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Boolean getStatusPending() {
		return statusPending;
	}
	public void setStatusPending(Boolean statusPending) {
		this.statusPending = statusPending;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public Map<String, String> getProfilePicture() {
		return profilePicture;
	}
	public void setProfilePicture(Map<String, String> profilePicture) {
		this.profilePicture = profilePicture;
	}
	public String getRegistrationSource() {
		return registrationSource;
	}
	public void setRegistrationSource(String registrationSource) {
		this.registrationSource = registrationSource;
	}
	public JsonObject getPrivacy() {
		return privacy;
	}
	public void setPrivacy(JsonObject privacy) {
		this.privacy = privacy;
	}
}

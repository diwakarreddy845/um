package com.capv.um.model;
 
/**
  *  <h1> UserConfigDTO </h1>
  *   This class is used to represent client configuration as well as client configuration property details such as id, userId,
  *    userPropName, propValue, and configPropertyId of client.
  *   @author caprusit
  *   @version 1.0
  */
public class UserConfigDTO {

	private Integer id;
	private Long userId;
	private String userName;
	private String userPropName;
	private String propValue;
	private Integer configPropertyId;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long clientId) {
		this.userId = clientId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String clientName) {
		this.userName = clientName;
	}

	public String getUserPropName() {
		return userPropName;
	}

	public void setUserPropName(String clientPropName) {
		this.userPropName = clientPropName;
	}

	public String getPropValue() {
		return propValue;
	}

	public void setPropValue(String propValue) {
		this.propValue = propValue;
	}

	public Integer getConfigPropertyId() {
		return configPropertyId;
	}

	public void setConfigPropertyId(Integer configPropertyId) {
		this.configPropertyId = configPropertyId;
	}

	@Override
	public String toString() {
		return "ClientConfigDTO [id=" + id + ", userId=" + userId + ", clientName=" + userName
				+ ", userPropName=" + userPropName + ", propValue=" + propValue + ", configPropertyId="
				+ configPropertyId + "]";
	}

}

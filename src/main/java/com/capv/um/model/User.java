package com.capv.um.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

@Entity
@Table(name = "user")
@JsonIgnoreProperties(value = { "handler", "hibernateLazyInitializer" })
public class User extends AbstractModel {

	@Column(name = "user_name")
	private String userName;

	@Column(name = "password")
	@JsonProperty(access = Access.WRITE_ONLY)
	private String password;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String email;

	@Column(name = "mobile")
	private String mobile;

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "created_date", updatable = false)
	private Date createdDate;

	@Column(name = "last_updated")
	private Date lastUpdated;

	@Column(name = "active")
	private Boolean active;

	@Column(name = "registration_source")
	private String registrationSource;

	@Column(name = "call_status")
	private Byte callStatus;

	@Column(name = "os_type")
	private String osType;

	@Column(name = "device_id")
	private String deviceId;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private List<UserConfig> userConfig;

	@Column(name = "last_signin_os")
	private String lastSigninOs;

	@Column(name = "token_id")
	private String tokenId;

	@Column(name = "ping")
	private Date ping;

	@Column(name = "logged_in_state")
	private Byte logged_in_state;

	@Column(name = "billing_cycle_date")
	private String billingCycleDate;

	public User() {
	}

	public User(User user) {
		super();
		this.setId(user.getId());
		this.userName = user.userName;
		this.password = user.password;
		this.firstName = user.firstName;
		this.lastName = user.lastName;
		this.name = user.name;
		this.email = user.email;
		this.mobile = user.mobile;
		this.clientId = user.clientId;
		this.active = user.active;
		this.createdDate = user.createdDate;
		this.lastUpdated = user.lastUpdated;
		this.callStatus = user.callStatus;
		this.userConfig = user.userConfig;
		this.lastSigninOs = user.lastSigninOs;
		this.tokenId = user.tokenId;
	}

	public Byte getLogged_in_state() {
		return logged_in_state;
	}

	public void setLogged_in_state(Byte logged_in_state) {
		this.logged_in_state = logged_in_state;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getRegistrationSource() {
		return registrationSource;
	}

	public void setRegistrationSource(String registrationSource) {
		this.registrationSource = registrationSource;
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

	public Byte getCallStatus() {
		return callStatus;
	}

	public void setCallStatus(Byte callStatus) {
		this.callStatus = callStatus;
	}

	public List<UserConfig> getUserConfig() {
		return userConfig;
	}

	public void setUserConfig(List<UserConfig> userConfig) {
		this.userConfig = userConfig;
	}

	public String getLastSigninOs() {
		return lastSigninOs;
	}

	public void setLastSigninOs(String lastSigninOs) {
		this.lastSigninOs = lastSigninOs;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public Date getPing() {
		return ping;
	}

	public void setPing(Date ping) {
		this.ping = ping;
	}

	public String getBillingCycleDate() {
		return billingCycleDate;
	}

	public void setBillingCycleDate(String billingCycleDate) {
		this.billingCycleDate = billingCycleDate;
	}

	@Override
	public String toString() {
		return "User [userName=" + userName + ", password=" + password + ", firstName=" + firstName + ", lastName="
				+ lastName + ", name=" + name + ", email=" + email + ", mobile=" + mobile + ", clientId=" + clientId
				+ ", createdDate=" + createdDate + ", lastUpdated=" + lastUpdated + ", active=" + active
				+ ", registrationSource=" + registrationSource + ", callStatus=" + callStatus + ", osType=" + osType
				+ ", deviceId=" + deviceId + ", lastSigninOs=" + lastSigninOs
				+ ", tokenId=" + tokenId + ", ping=" + ping + ", logged_in_state=" + logged_in_state
				+ ", billingCycleDate=" + billingCycleDate + "]";
	}

}

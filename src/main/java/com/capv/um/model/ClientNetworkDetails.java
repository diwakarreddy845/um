package com.capv.um.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "client_network_details")
@JsonIgnoreProperties(value = { "handler", "hibernateLazyInitializer" })
public class ClientNetworkDetails extends AbstractModel {

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "ip_address")
	private String ipAddress;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "download_speed")
	private String downloadSpeed;

	@Column(name = "upload_speed")
	private String uploadSpeed;

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getDownloadSpeed() {
		return downloadSpeed;
	}

	public void setDownloadSpeed(String downloadSpeed) {
		this.downloadSpeed = downloadSpeed;
	}

	public String getUploadSpeed() {
		return uploadSpeed;
	}

	public void setUploadSpeed(String uploadSpeed) {
		this.uploadSpeed = uploadSpeed;
	}

}

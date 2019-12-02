package com.capv.um.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "schedule_chat")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleChat {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "room_name")
	private String roomName;

	@Column(name = "room_no")
	private String roomNumber;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "client_Id")
	private String clientId;

	@Column(name = "created_date", updatable = false)
	private Date createdDate;

	@Column(name = "schedule_start_date")
	private Date scheduleStartDate;

	@Column(name = "schedule_end_date")
	private Date scheduleEndDate;

	@Column(name = "status")
	private int status;

	@Column(name = "recurrence")
	private boolean recurrence;

	@Column(name = "chat_message")
	private String chatMesage;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getRoomNumber() {
		return roomNumber;
	}

	public void setRoomNumber(String roomNumber) {
		this.roomNumber = roomNumber;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getChatMesage() {
		return chatMesage;
	}

	public void setChatMesage(String chatMesage) {
		this.chatMesage = chatMesage;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getScheduleStartDate() {
		return scheduleStartDate;
	}

	public void setScheduleStartDate(Date scheduleStartDate) {
		this.scheduleStartDate = scheduleStartDate;
	}

	public Date getScheduleEndDate() {
		return scheduleEndDate;
	}

	public void setScheduleEndDate(Date scheduleEndDate) {
		this.scheduleEndDate = scheduleEndDate;
	}

	public boolean isRecurrence() {
		return recurrence;
	}

	public void setRecurrence(boolean recurrence) {
		this.recurrence = recurrence;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}

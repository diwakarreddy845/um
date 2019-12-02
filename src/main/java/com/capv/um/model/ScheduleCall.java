package com.capv.um.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Auditlog Persistence object will store data in auditlog table
 * 
 * @author caprusit
 * @version 1.0
 */
@Entity
@Table(name = "schedule_call")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleCall {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "room_name")
	private String roomName;

	@Column(name = "room_no")
	private String roomNumber;

	@Column(name = "client_Id")
	private String clientId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "call_type")
	private String callType;

	@Column(name = "created_date", updatable = false)
	private Date createdDate;

	@Column(name = "schedule_start_date")
	private Date scheduleStartDate;

	@Column(name = "schedule_end_date")
	private Date scheduleEndDate;

	@Column(name = "max_number_of_retry")
	private int maxNumberOfRetry;

	@Column(name = "status")
	private int status;

	@Column(name = "recurrence")
	private boolean recurrence;

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

	public int getMaxNumberOfRetry() {
		return maxNumberOfRetry;
	}

	public void setMaxNumberOfRetry(int maxNumberOfRetry) {
		this.maxNumberOfRetry = maxNumberOfRetry;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public boolean isRecurrence() {
		return recurrence;
	}

	public void setRecurrence(boolean recurrence) {
		this.recurrence = recurrence;
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

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

}
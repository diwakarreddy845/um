package com.capv.um.model;

import java.util.Date;

public class TryItMeet {

	private String roomName;
	private String clientId;
	private String userName;
	private String isRecoding;
	private Integer isValid;
	private Byte scheduleMeetingRecurringFlag;
	private String meetingDuration;
	private Long timeZoneId;
	private Date meetingDate;
	private String location;
	private String agenda;
	private Date endDate;
	private String attendees;
	private String meetingType;

	public String getAttendees() {
		return attendees;
	}

	public void setAttendees(String attendees) {
		this.attendees = attendees;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getAgenda() {
		return agenda;
	}

	public void setAgenda(String agenda) {
		this.agenda = agenda;
	}

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getIsRecoding() {
		return isRecoding;
	}

	public void setIsRecoding(String isRecoding) {
		this.isRecoding = isRecoding;
	}

	public Integer getIsValid() {
		return isValid;
	}

	public void setIsValid(Integer isValid) {
		this.isValid = isValid;
	}

	public Byte getScheduleMeetingRecurringFlag() {
		return scheduleMeetingRecurringFlag;
	}

	public void setScheduleMeetingRecurringFlag(Byte scheduleMeetingRecurringFlag) {
		this.scheduleMeetingRecurringFlag = scheduleMeetingRecurringFlag;
	}

	public String getMeetingDuration() {
		return meetingDuration;
	}

	public void setMeetingDuration(String meetingDuration) {
		this.meetingDuration = meetingDuration;
	}

	public Long getTimeZoneId() {
		return timeZoneId;
	}

	public void setTimeZoneId(Long timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

	public Date getMeetingDate() {
		return meetingDate;
	}

	public void setMeetingDate(Date meetingDate) {
		this.meetingDate = meetingDate;
	}

	public String getMeetingType() {
		return meetingType;
	}

	public void setMeetingType(String meetingType) {
		this.meetingType = meetingType;
	}

}

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
@Table(name = "tryit_room_sessions")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TryItRoom {

	public TryItRoom() {
	}

	public TryItRoom(String roomNo, Integer validity, Integer isValid) {
		super();
		this.roomNo = roomNo;
		this.validity = validity;
		this.isValid = isValid;
	}

	public TryItRoom(String roomNo, Integer validity, String clientId, String roomName, String userName, String isRecoding, Integer isValid) {
		super();
		this.roomNo = roomNo;
		this.validity = validity;
		this.clientId = clientId;
		this.userName = userName;
		this.roomName = roomName;
		this.isRecoding = isRecoding;
		this.isValid = isValid;
	}

	public TryItRoom(String roomNo, Integer validity, String clientId, String roomName, String userName, String isRecoding, Integer isValid,
			Byte scheduleMeetingRecurringFlag, String meetingDuration, Long timeZoneId, Date meetingDate, String location, String agenda,
			Date endDate, String attendees) {
		super();
		this.roomNo = roomNo;
		this.validity = validity;
		this.clientId = clientId;
		this.userName = userName;
		this.roomName = roomName;
		this.isRecoding = isRecoding;
		this.isValid = isValid;
		this.scheduleMeetingRecurringFlag = scheduleMeetingRecurringFlag;
		this.meetingDuration = meetingDuration;
		this.timeZoneId = timeZoneId;
		this.meetingDate = meetingDate;
		this.location = location;
		this.agenda = agenda;
		this.endDate = endDate;
		this.attendees = attendees;
	}

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "room_no")
	private String roomNo;

	@Column(name = "validity")
	private Integer validity;

	@Column(name = "created_timestamp", updatable = false)
	private Date createdTimestamp;

	@Column(name = "room_name")
	private String roomName;

	@Column(name = "client_Id")
	private String clientId;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "is_recoding")
	private String isRecoding;

	@Column(name = "is_valid")
	private Integer isValid;

	@Column(name = "sch_meeting_recurring_flag")
	private Byte scheduleMeetingRecurringFlag;

	@Column(name = "meeting_duration")
	private String meetingDuration;

	@Column(name = "time_zone_id")
	private Long timeZoneId;

	@Column(name = "meeting_date")
	private Date meetingDate;

	@Column(name = "location")
	private String location;

	@Column(name = "agenda")
	private String agenda;

	@Column(name = "end_date")
	private Date endDate;

	@Column(name = "attendees")
	private String attendees;

	@Column(name = "meeting_type")
	private String meetingType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getValidity() {
		return validity;
	}

	public void setValidity(Integer validity) {
		this.validity = validity;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

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

	public String getRoomNo() {
		return roomNo;
	}

	public void setRoomNo(String roomNo) {
		this.roomNo = roomNo;
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
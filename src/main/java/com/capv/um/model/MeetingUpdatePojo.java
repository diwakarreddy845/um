package com.capv.um.model;

import java.util.Date;
import java.util.List;

import javax.persistence.Column;

public class MeetingUpdatePojo {
	private String room_id;
	public String getRoom_id() {
		return room_id;
	}
	public void setRoom_id(String room_id) {
		this.room_id = room_id;
	}
	private String room_name;
	private String client_Id;
	private String user_name;
	private String is_recoding;
	
	private Integer is_valid;
	private Byte sch_meeting_recurring_flag;
	private String meeting_duration;
	private Long time_zone_id;
	private Date meeting_date;
	private String location;
	private String agenda;
	private Date endDate;
	private String attendees;

	
	
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
	public Integer getIs_valid() {
		return is_valid;
	}
	public void setIs_valid(Integer is_valid) {
		this.is_valid = is_valid;
	}
	public Byte getSch_meeting_recurring_flag() {
		return sch_meeting_recurring_flag;
	}
	public void setSch_meeting_recurring_flag(Byte sch_meeting_recurring_flag) {
		this.sch_meeting_recurring_flag = sch_meeting_recurring_flag;
	}
	public String getMeeting_duration() {
		return meeting_duration;
	}
	public void setMeeting_duration(String meeting_duration) {
		this.meeting_duration = meeting_duration;
	}
	public Long getTime_zone_id() {
		return time_zone_id;
	}
	public void setTime_zone_id(Long time_zone_id) {
		this.time_zone_id = time_zone_id;
	}
	public Date getMeeting_date() {
		return meeting_date;
	}
	public void setMeeting_date(Date meeting_date) {
		this.meeting_date = meeting_date;
	}
	public String getIs_recoding() {
		return is_recoding;
	}
	public void setIs_recoding(String is_recoding) {
		this.is_recoding = is_recoding;
	}
	public String getRoom_name() {
		return room_name;
	}
	public void setRoom_name(String room_name) {
		this.room_name = room_name;
	}
	public String getClient_Id() {
		return client_Id;
	}
	public void setClient_Id(String client_Id) {
		this.client_Id = client_Id;
	}
	public String getUser_name() {
		return user_name;
	}
	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}
    
}

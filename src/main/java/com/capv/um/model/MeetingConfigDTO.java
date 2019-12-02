package com.capv.um.model;
 
/**
  *  <h1> UserConfigDTO </h1>
  *   This class is used to represent client configuration as well as client configuration property details such as id, userId,
  *    userPropName, propValue, and configPropertyId of client.
  *   @author caprusit
  *   @version 1.0
  */
public class MeetingConfigDTO {


	private String meetingId;
	private String meetingPropName;
	private String propValue;
	private Integer meetingPropertyId;
	
	public String getMeetingId() {
		return meetingId;
	}
	public void setMeetingId(String meetingId) {
		this.meetingId = meetingId;
	}
	public String getMeetingPropName() {
		return meetingPropName;
	}
	public void setMeetingPropName(String meetingPropName) {
		this.meetingPropName = meetingPropName;
	}
	public String getPropValue() {
		return propValue;
	}
	public void setPropValue(String propValue) {
		this.propValue = propValue;
	}
	public Integer getMeetingPropertyId() {
		return meetingPropertyId;
	}
	public void setMeetingPropertyId(Integer meetingPropertyId) {
		this.meetingPropertyId = meetingPropertyId;
	}

	

	
}

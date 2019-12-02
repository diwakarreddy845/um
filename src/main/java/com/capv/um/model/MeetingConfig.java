package com.capv.um.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 *  <h1> MeetingConfig </h1>
 *   This class represents a model MeetingConfig contains the configuration details related to meeting such as id, meetingId,
 *    propValue, and ConfigProperty model details.
 *   @author caprus it
 *   @version 1.0
 */
@Entity
@Table(name = "meeting_config")
@JsonIgnoreProperties(value = { "handler", "hibernateLazyInitializer" })
public class MeetingConfig {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@Column(name = "meeting_id")
	private String meeting_id;

	@Column(name = "prop_value")
	private String prop_value;

	@ManyToOne
	@JoinColumn(name = "meeting_property_id")
	private MeetingConfigProperty meeting_property_id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMeeting_id() {
		return meeting_id;
	}

	public void setMeeting_id(String meeting_id) {
		this.meeting_id = meeting_id;
	}

	public String getProp_value() {
		return prop_value;
	}

	public void setProp_value(String prop_value) {
		this.prop_value = prop_value;
	}

	public MeetingConfigProperty getMeeting_property_id() {
		return meeting_property_id;
	}

	public void setMeeting_property_id(MeetingConfigProperty meeting_property_id) {
		this.meeting_property_id = meeting_property_id;
	}

	

}

package com.capv.um.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "schedule_meeting")
public class ScheduleMeeting {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "meeting_room_id")
	private String meetingRoomId;

	@Column(name = "meeting_subject")
	private String meetingSubject;

	@Column(name = "meeting_description")
	private String meetingDescription;

	@Column(name = "meeting_duration")
	private String meetingDuration;

	@Column(name = "video_participant")
	private Byte videoParticipant;

	@Column(name = "aleternative_host")
	private String aleternativeHost;

	@Column(name = "recurring_flag")
	private Byte recurringFlag;

	@Column(name = "communication_recurring_flag")
	private Byte communicationRecurringFlag;

	@Column(name = "video_host")
	private Byte videoHost;

	@Column(name = "audio_type_id")
	private Long audioTypeId;

	@Column(name = "time_zone_id")
	private Long timeZoneId;

	@Column(name = "meeting_date", updatable = false)
	private Date meetingDate;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getMeetingRoomId() {
		return meetingRoomId;
	}

	public void setMeetingRoomId(String meetingRoomId) {
		this.meetingRoomId = meetingRoomId;
	}

	public String getMeetingSubject() {
		return meetingSubject;
	}

	public void setMeetingSubject(String meetingSubject) {
		this.meetingSubject = meetingSubject;
	}

	public String getMeetingDescription() {
		return meetingDescription;
	}

	public void setMeetingDescription(String meetingDescription) {
		this.meetingDescription = meetingDescription;
	}

	public String getMeetingDuration() {
		return meetingDuration;
	}

	public void setMeetingDuration(String meetingDuration) {
		this.meetingDuration = meetingDuration;
	}

	public Byte getVideoParticipant() {
		return videoParticipant;
	}

	public void setVideoParticipant(Byte videoParticipant) {
		this.videoParticipant = videoParticipant;
	}

	public String getAleternativeHost() {
		return aleternativeHost;
	}

	public void setAleternativeHost(String aleternativeHost) {
		this.aleternativeHost = aleternativeHost;
	}

	public Byte getRecurringFlag() {
		return recurringFlag;
	}

	public void setRecurringFlag(Byte recurringFlag) {
		this.recurringFlag = recurringFlag;
	}

	public Byte getCommunicationRecurringFlag() {
		return communicationRecurringFlag;
	}

	public void setCommunicationRecurringFlag(Byte communicationRecurringFlag) {
		this.communicationRecurringFlag = communicationRecurringFlag;
	}

	public Byte getVideoHost() {
		return videoHost;
	}

	public void setVideoHost(Byte videoHost) {
		this.videoHost = videoHost;
	}

	public Long getAudioTypeId() {
		return audioTypeId;
	}

	public void setAudioTypeId(Long audioTypeId) {
		this.audioTypeId = audioTypeId;
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
}

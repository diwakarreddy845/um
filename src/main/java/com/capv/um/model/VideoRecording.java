package com.capv.um.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicUpdate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="video_recording")
@JsonIgnoreProperties(ignoreUnknown = true)
@DynamicUpdate(value=true)
public class VideoRecording extends AbstractModel {
	
	@Column(name = "user_name")
	private String userName;
	
	@Column(name = "room_id")
	private String roomId;
	
	@Column(name = "client_id")
	private int clientId;
	
	@Column(name = "start_time")
	private Date starttime;
	
	@Column(name = "end_time")
	private Date endtime;
	
	@Column(name= "scheduler")
	private Boolean scheduler;
	
	@Column(name= "isfullvideo")
	private Boolean isFullVideo;
	
	@Column (name="s3path")
	private String s3path;
	
	@Column (name="rec_video_id")
	private Long videoRecId;
	
	@Column (name="source_path")
	private String sourcePath;
	
	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	private String diff;
	public String getDiff() {
		return diff;
	}

	public void setDiff(String diff) {
		this.diff = diff;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public Date getStarttime() {
		return starttime;
	}

	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}

	public Date getEndtime() {
		return endtime;
	}

	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}

	
	public Boolean isScheduler() {
		return scheduler;
	}

	public void setScheduler(Boolean scheduler) {
		this.scheduler = scheduler;
	}

	public Boolean isFullVideo() {
		return isFullVideo;
	}

	public void setFullVideo(Boolean isFullVideo) {
		this.isFullVideo = isFullVideo;
	}

	public String getS3path() {
		return s3path;
	}

	public void setS3path(String s3path) {
		this.s3path = s3path;
	}

	public Long getVideoRecId() {
		return videoRecId;
	}

	public void setVideoRecId(Long videoRecId) {
		this.videoRecId = videoRecId;
	}

	public String getRoomId() {
		return roomId;
	}

	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	 
}

package com.capv.um.view;

import java.util.Date;

public class VideoRecordView {
	
	private Long id;
	
	private String userName;
	
	private String roomId;
	
	private int clientId;
	
	private Date starttime;
	
	private Date endtime;
	
	private Boolean scheduler;
	
	private Boolean isFullVideo;
	
	private String s3path;
	
	private Long videoRecId;
	
	private String callerId;
	
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


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
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

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	public String getCallerId() {
		return callerId;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	
	 
}



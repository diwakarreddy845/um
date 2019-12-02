package com.capv.um.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.capv.um.model.VideoRecording;

public interface VideoRecordingService {
	
	
	void save(VideoRecording videorecording);

	void update(VideoRecording videorecording);
	
	VideoRecording getById(Long id);

	Date getEndtime(Long videoRecordingId);

	List<VideoRecording> getRoomList();
	
	List<VideoRecording> getRoomListN();

	void updateS3path(VideoRecording videoRecording);

	String getplayBackUrlsList(String roomName);
	
	VideoRecording getVideoRecordingDetailsByMatchingProperties(Map<String, Object> properties);
	
	void updateSchedular(Long id);
	
}

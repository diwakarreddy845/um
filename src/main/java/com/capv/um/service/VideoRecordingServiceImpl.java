package com.capv.um.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.VideoRecording;
import com.capv.um.repository.VideoRecordingRepository;

@Service("VideoRecordingService")
@Transactional("transactionManager")
public class VideoRecordingServiceImpl implements VideoRecordingService {

	@Autowired
	VideoRecordingRepository videoRecordingRepository;

	public void save(VideoRecording videoRecordingModel) {
		videoRecordingRepository.save(videoRecordingModel);
	}

	public void update(VideoRecording videoRecordingModel) {
		videoRecordingRepository.save(videoRecordingModel);
	}

	public VideoRecording getById(Long id) {
		Optional<VideoRecording> vr = videoRecordingRepository.findById(id);
		if (vr.isPresent()) {
			return vr.get();
		}
		return null;
	}

	public Date getEndtime(Long vrId) {
		Optional<VideoRecording> vr = videoRecordingRepository.findById(vrId);
		if (vr.isPresent()) {
			return vr.get().getEndtime();
		}
		return null;
	}

	public void updateS3path(VideoRecording videoRecording) {
		Optional<VideoRecording> vr = videoRecordingRepository.findById(videoRecording.getId());
		if (vr.isPresent()) {
			vr.get().setS3path(videoRecording.getS3path());
			videoRecordingRepository.save(vr.get());
		}
	}

	@Override
	public void updateSchedular(Long id) {
		Optional<VideoRecording> vr = videoRecordingRepository.findById(id);
		if (vr.isPresent()) {
			vr.get().setScheduler(true);
			videoRecordingRepository.save(vr.get());
		}
	}
	
	public List<VideoRecording> getRoomList() {
		return videoRecordingRepository.getRoomList();
	}

	public List<VideoRecording> getRoomListN() {
		return videoRecordingRepository.getRoomListN();
	}

	public String getplayBackUrlsList(String roomName) {
		return videoRecordingRepository.getplayBackUrlsList(roomName);
	}

	public VideoRecording getVideoRecordingDetailsByMatchingProperties(Map<String, Object> properties) {
		return videoRecordingRepository.getUniqueEntityByMatchingProperties(VideoRecording.class, properties);
	}


}

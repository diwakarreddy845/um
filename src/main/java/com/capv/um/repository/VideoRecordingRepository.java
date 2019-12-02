package com.capv.um.repository;

import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.capv.um.model.VideoRecording;


public interface VideoRecordingRepository extends JpaRepository<VideoRecording, Long> {

	@Query("SELECT a FROM VideoRecording a WHERE scheduler = false and isFullVideo=true and endtime is not null order by clientId")
	List<VideoRecording> getRoomList();

	List<VideoRecording> getRoomListN();

	String getplayBackUrlsList(String roomName);

	void updateS3path(VideoRecording videoRecording);

	VideoRecording getUniqueEntityByMatchingProperties(Class<VideoRecording> class1, Map<String, Object> properties);

	void updateSchedular(Long id);

}

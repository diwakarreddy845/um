package com.capv.um.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.capv.um.model.UserRoomRequest;


public interface UserRoomRequestRepository extends JpaRepository<UserRoomRequest, Long> {

	UserRoomRequest getUniqeRecord(String username, String roomname);

	List<UserRoomRequest> getRecordByRoomName(String roomName);

	List<UserRoomRequest> getUserPendingRequest(String username);

	List<UserRoomRequest> getUserPendingRequestRoom(String roomname);
}

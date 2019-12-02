package com.capv.um.service;

import java.util.List;

import com.capv.um.model.UserRoomRequest;

public interface UserRoomRequestService {

	void save(UserRoomRequest userRoomReq);
	void update(UserRoomRequest userRoomReq);
	void delete(UserRoomRequest userRoomReq);
	
	List<UserRoomRequest> getUserPendingRequest(String username);
	
	UserRoomRequest getUniqeRecord(String username,String roomname);
	
	List<UserRoomRequest>  getRecordByRoomName(String roomName);
	
	List<UserRoomRequest> getUserPendingRequestRoom(String roomname);
}

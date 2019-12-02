package com.capv.um.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.UserRoomRequest;
import com.capv.um.repository.UserRoomRequestRepository;

@Service("UserRoomRequestService")
@Transactional("transactionManager")
public class UserRoomRequestServiceImpl implements UserRoomRequestService{

	@Autowired
	UserRoomRequestRepository userRoomRequestRepository;
	@Override
	public void save(UserRoomRequest userRoomReq) {
		userRoomRequestRepository.save(userRoomReq);
	}

	@Override
	public void update(UserRoomRequest userRoomReq) {
		userRoomRequestRepository.save(userRoomReq);
	}

	@Override
	public void delete(UserRoomRequest userRoomReq) {
		userRoomRequestRepository.delete(userRoomReq);
	}

	@Override
	public List<UserRoomRequest> getUserPendingRequest(String username) {
		return userRoomRequestRepository.getUserPendingRequest(username);
	}
	
	@Override
	public List<UserRoomRequest> getUserPendingRequestRoom(String roomname) {
		return userRoomRequestRepository.getUserPendingRequestRoom(roomname);
	}

	@Override
	public UserRoomRequest getUniqeRecord(String username, String roomname) {
		return userRoomRequestRepository.getUniqeRecord(username, roomname);
	}
	
	@Override
	public List<UserRoomRequest> getRecordByRoomName(String roomName) {
		return userRoomRequestRepository.getRecordByRoomName(roomName);
	}

}

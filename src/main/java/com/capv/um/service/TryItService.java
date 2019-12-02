package com.capv.um.service;

import java.util.List;
import com.capv.um.model.TryItRoom;

/**
 * <h1>UserService</h1> this interface is used to perform user crud operations
 * 
 * @author caprusit
 * @version 1.0
 */
public interface TryItService {

	/**
	 * this method is used to save entity
	 * 
	 * @param entity the object
	 */
	void save(TryItRoom tryitroom);

	void delete(TryItRoom tryitroom);

	void update(TryItRoom tryitroom);

	List<TryItRoom> listRoomsByMatchingUserName(String user_name, String client_Id);

	TryItRoom fetchUniqueRoomRecord(String paramValue);

	List<TryItRoom> fetchAllRooms();

	void sendMeetingICS(TryItRoom tryItRoom) throws Exception;
}

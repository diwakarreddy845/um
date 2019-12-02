package com.capv.um.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.capv.um.model.TryItRoom;


public interface TryItRoomRepository extends JpaRepository<TryItRoom, Long> {

	TryItRoom findbyRoomNo(String paramValue);

	@Query("SELECT a FROM TryItRoom a WHERE user_name = ?1 AND client_id = ?2 and is_valid = 0")
	List<TryItRoom> listRoomsByMatchingUserName(String userName, String clientId);
}

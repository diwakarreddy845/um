package com.capv.um.repository;

import java.sql.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.capv.um.model.ScheduleChat;

@Repository
public interface ScheduleChatRepository extends JpaRepository<ScheduleChat, Long> {

	List<ScheduleChat> findByuserName(String userName);
	
	@Query("SELECT a FROM ScheduleChat a WHERE schedule_start_date = ?1 AND status = ?2")
    List<ScheduleChat> findByDate(Date scheduleStartDate, int status);
}
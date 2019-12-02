package com.capv.um.repository;

import java.sql.Date;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.capv.um.model.ScheduleCall;

@Repository
public interface ScheduleCallRepository extends JpaRepository<ScheduleCall, Long> {

	@Query("SELECT a FROM ScheduleCall a WHERE schedule_start_date = ?1 AND status = ?2")
	List<ScheduleCall> findByDate(Date scheduleStartDate, int status);

	List<ScheduleCall> findByRoomName(String roomName);

	List<ScheduleCall> findByuserName(String userName);
}

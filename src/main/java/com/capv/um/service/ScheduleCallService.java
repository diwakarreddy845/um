package com.capv.um.service;


import java.sql.Date;
import java.util.List;

import com.capv.um.model.ScheduleCall;

public interface ScheduleCallService {

	void save(ScheduleCall call);

	ScheduleCall update(ScheduleCall call);

	List<ScheduleCall> getAllScheduledCall();

	List<ScheduleCall> getTodayScheduledCall();

	void delete(ScheduleCall scheduleCall);

	List<ScheduleCall> getAllByDate(Date date, int status);

	List<ScheduleCall> getAllByUserName(String userName, String clientId);

	List<ScheduleCall> getAllByGroupName(String groupName);

}

package com.capv.um.service;


import java.sql.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.ScheduleCall;
import com.capv.um.repository.ScheduleCallRepository;

@Service("scheduleCallService")
@Transactional("transactionManager")
public class ScheduleCallServiceImpl implements ScheduleCallService {

	@Autowired
	private ScheduleCallRepository scheduleCallRepository;
	
	
	@Override
	public List<ScheduleCall> getAllScheduledCall() {
		return scheduleCallRepository.findAll();
	}

	@Override
	public List<ScheduleCall> getTodayScheduledCall() {
		return null;
	}

	@Override
	public void save(ScheduleCall scheduledCall) {
		scheduleCallRepository.save(scheduledCall);
	}

	@Override
	public ScheduleCall update(ScheduleCall scheduledCall) {
		return  scheduleCallRepository.save(scheduledCall);
	}

	@Override
	public void delete(ScheduleCall scheduledCall) {
		scheduleCallRepository.delete(scheduledCall);
	}

	@Override
	public List<ScheduleCall> getAllByDate(Date date, int status) {
		return  scheduleCallRepository.findByDate(date, status);
	}

	@Override
	public List<ScheduleCall> getAllByUserName(String userName, String clientId) {
		return  scheduleCallRepository.findByuserName(userName);
	}

	@Override
	public List<ScheduleCall> getAllByGroupName(String groupName) {
		return scheduleCallRepository.findByRoomName(groupName);
	}

}

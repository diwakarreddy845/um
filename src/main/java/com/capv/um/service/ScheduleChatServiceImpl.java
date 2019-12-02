package com.capv.um.service;


import java.sql.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.ScheduleChat;
import com.capv.um.repository.ScheduleChatRepository;

@Service("scheduleChatService")
@Transactional("transactionManager")
public class ScheduleChatServiceImpl implements ScheduleChatService {

	@Autowired
	private ScheduleChatRepository scheduleChatRepository ;

	@Override
	public List<ScheduleChat> getAllScheduledChat() {
		return scheduleChatRepository.findAll();
	}

	@Override
	public List<ScheduleChat> getTodayScheduledChat() {
		return scheduleChatRepository.findByDate(new Date(0), 0);
	}

	@Override
	public void save(ScheduleChat scheduleChat) {
		scheduleChatRepository.save(scheduleChat);
	}

	@Override
	public ScheduleChat update(ScheduleChat scheduleChat) {
		return scheduleChatRepository.save(scheduleChat);
	}

	@Override
	public void delete(ScheduleChat scheduleChat) {
		scheduleChatRepository.delete(scheduleChat);
	}

	@Override
	public List<ScheduleChat> getAllByUserName(String userName, String clientId) {
		return scheduleChatRepository.findByuserName(userName);
	}

}

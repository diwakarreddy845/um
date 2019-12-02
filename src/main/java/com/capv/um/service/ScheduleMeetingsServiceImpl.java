package com.capv.um.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.ScheduleMeeting;
import com.capv.um.repository.ScheduleMeetingRepository;

@Service("scheduleMeetingsService")
@Transactional("transactionManager")
public class ScheduleMeetingsServiceImpl implements ScheduleMeetingService {

	@Autowired
	ScheduleMeetingRepository scheduleMeetingRepository;

	@Override
	public void save(ScheduleMeeting details) {
		scheduleMeetingRepository.save(details);
	}

	@Override
	public List<ScheduleMeeting> getScheduleMeetingList() {
		return scheduleMeetingRepository.findAll();
	}
}

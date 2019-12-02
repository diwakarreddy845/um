package com.capv.um.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.TimeZone;
import com.capv.um.repository.TimeZoneRepository;

@Service("timeZoneService")
@Transactional("transactionManager")
public class TimeZoneServiceImpl implements TimeZoneService {

	@Autowired
	TimeZoneRepository timeZoneRepository;

	public List<TimeZone> getAllTimeZones() {
		return timeZoneRepository.findAll();
	}
}

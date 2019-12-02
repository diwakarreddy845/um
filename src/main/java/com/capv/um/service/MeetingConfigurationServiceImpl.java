package com.capv.um.service;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.MeetingConfig;
import com.capv.um.model.MeetingConfigDTO;
import com.capv.um.model.MeetingConfigProperty;
import com.capv.um.repository.MeetingConfigPropertyRepository;
import com.capv.um.repository.MeetingConfigRepository;

@Service("meetingConfigurationService")
@Transactional("transactionManager")
public class MeetingConfigurationServiceImpl implements MeetingConfigurationService {
	private static final Logger log = LoggerFactory.getLogger(MeetingConfigurationServiceImpl.class);
	@Autowired
	MeetingConfigRepository meetingConfigRepository;

	@Autowired
	MeetingConfigPropertyRepository configPropertyRepository;

	@Override
	public boolean insertMeetingConfigInfo(List<MeetingConfigDTO> meetingConfigDTOList) {
		try {
			for (MeetingConfigDTO meetingConfigDTO : meetingConfigDTOList) {
				MeetingConfig meetConfig = new MeetingConfig();
				meetConfig.setMeeting_id(meetingConfigDTO.getMeetingId());
				meetConfig.setProp_value(meetingConfigDTO.getPropValue());
				Optional<MeetingConfigProperty> meetingConfigProperty = configPropertyRepository.findById(meetingConfigDTO.getMeetingPropertyId());
				if(meetingConfigProperty.isPresent()) {
					meetConfig.setMeeting_property_id(meetingConfigProperty.get());
				}
				meetingConfigRepository.save(meetConfig);
			}
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return false;
	}
}

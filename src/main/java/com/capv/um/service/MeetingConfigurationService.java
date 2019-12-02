package com.capv.um.service;

import java.util.List;
import com.capv.um.model.MeetingConfigDTO;

public interface MeetingConfigurationService {

	boolean insertMeetingConfigInfo(List<MeetingConfigDTO> meetingConfig);
}

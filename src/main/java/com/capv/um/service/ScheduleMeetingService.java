package com.capv.um.service;

import java.util.List;

import com.capv.um.model.ScheduleMeeting;

/**
 *  This interface is used to perform operations on ScheduleMeetingService such as save ScheduleMeetingService details.
 *  @author caprus it
 *  @version 1.0
 */
public interface ScheduleMeetingService {
	void save(ScheduleMeeting details);
	List<ScheduleMeeting> getScheduleMeetingList();
}

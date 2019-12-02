package com.capv.um.service;

import java.util.List;

import com.capv.um.model.TimeZone;

/**
 *  This interface is used to perform operations on TimeZone details  .
 *  @author caprusit
 *  @version 1.0
 */
public interface TimeZoneService {
	List<TimeZone> getAllTimeZones();
}

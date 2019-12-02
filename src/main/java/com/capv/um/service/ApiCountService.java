package com.capv.um.service;

import java.util.List;
import java.util.Map;

import com.capv.um.model.ApiCount;

public interface ApiCountService {

	void saveApiCount(Map<Long, List<ApiCount>> apiCounts);
}

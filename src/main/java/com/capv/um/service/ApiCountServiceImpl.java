package com.capv.um.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.ApiCount;
import com.capv.um.repository.ApiCountRepository;

@Service
@Transactional("transactionManager")
public class ApiCountServiceImpl implements ApiCountService {

	@Autowired
	private ApiCountRepository apiCountRepository;
	
	@Override
	public void saveApiCount(Map<Long, List<ApiCount>> apiCountMap) {
		
	if(apiCountMap!=null){
		for (List<ApiCount> apiCounts : apiCountMap.values()) {
			for (ApiCount apiCount : apiCounts) {
				apiCountRepository.save(apiCount);
			}
		}
		
	}
		
	}
	
}

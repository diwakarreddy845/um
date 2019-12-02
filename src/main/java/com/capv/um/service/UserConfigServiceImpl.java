package com.capv.um.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.UserConfig;
import com.capv.um.model.UserConfigDTO;
import com.capv.um.model.UserConfigProperty;
import com.capv.um.repository.UserConfigPropertyRepository;
import com.capv.um.repository.UserConfigRepository;

@Service("userConfigService")
@Transactional("transactionManager")
public class UserConfigServiceImpl implements UserConfigService {

	@Autowired
	UserConfigRepository userConfigRepository;

	@Autowired
	UserConfigPropertyRepository userConfigPropertyRepository;

	@Override
	public boolean updateUserConfigInfo(List<UserConfigDTO> updateConfigDetails) {
		return userConfigRepository.updateUserConfigInfo(updateConfigDetails);
	}

	@Override
	public boolean updateUserConfig(List<UserConfig> updateConfigList) {
		return userConfigRepository.updateUserConfig(updateConfigList);
	}

	@Override
	public boolean insertUserConfigInfo(List<UserConfigDTO> insertConfigDetails) {
		return userConfigRepository.insertUserConfigInfo(insertConfigDetails);
	}

	@Override
	public boolean insertUserConfig(List<UserConfig> userConfigList) {
		return userConfigRepository.insertUserConfig(userConfigList);
	}

	@Override
	public List<UserConfig> getAllUserConfigDetails() {
		return userConfigRepository.findAll();
	}

	@Override
	public List<UserConfig> getUserConfigDetailsByUserId(Long userId) {
		return userConfigRepository.getUserConfigByUserId(userId);
	}

	@Override
	public List<UserConfigProperty> getAllUserConfigPropertyDetails() {
		return userConfigPropertyRepository.findAll();
	}
}

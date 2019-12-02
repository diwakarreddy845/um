package com.capv.um.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.capv.um.model.UserConfig;
import com.capv.um.model.UserConfigDTO;

public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {

	List<UserConfig> getUserConfigByUserId(Long userId);

	boolean insertUserConfig(List<UserConfig> userConfigList);

	boolean insertUserConfigInfo(List<UserConfigDTO> insertConfigDetails);

	boolean updateUserConfig(List<UserConfig> updateConfigList);

	boolean updateUserConfigInfo(List<UserConfigDTO> updateConfigDetails);
}

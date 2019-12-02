package com.capv.um.service;

import java.util.List;
import com.capv.um.model.UserConfig;
import com.capv.um.model.UserConfigDTO;
import com.capv.um.model.UserConfigProperty;

 /**
  *  <h1> UserConfigService </h1>
  *  This interface is used to provide services related to User functionality.
  *  @author caprusit
  *  @version 1.0
  */
public interface UserConfigService {
	


	public boolean updateUserConfig(List<UserConfig> userConfigList);
	/**
	 *  This method is used for updating user configuration information.
	 *  @param updateConfigDetails - it contains all the user configuration information which user want to update.
	 *  @return boolean type status  as 'true' on successful updation of configuration information,
	 *   or returns status as 'false' on unsuccessful updation of configuration information.
	 */
	public boolean updateUserConfigInfo(List<UserConfigDTO> updateConfigDetails);
	
	/**
	 *  This method is used to insert user configuration information.
	 *  @param insertConfigDetails - it contains all the user configuration information which user want to insert.
	 *  @return boolean type status as 'true' on successful insertion of configuration information,
	 *  or returns status as 'false' on unsuccessful insertion of configuration information.
	 */
	public boolean insertUserConfigInfo(List<UserConfigDTO> insertConfigDetails);
	
	boolean insertUserConfig(List<UserConfig> userConfigList);
	

	/**
	 *  This method is used to get all user configuration information.
	 *  @return list contains the configuration information of all users.
	 */
	public List<UserConfig> getAllUserConfigDetails();
	
	/**
	 *  This method is used to get all user configuration property information.
	 *  @return list contains the configuration property information of all users.
	 */
	public List<UserConfigProperty> getAllUserConfigPropertyDetails();
	

	/**
	 *  This method is used to get user configuration details based on userId.
	 *  @param UserId -  it specifies an userId of UConfig model.
	 *  @return list contains the configuration details of user.
	 */
	List<UserConfig> getUserConfigDetailsByUserId(Long userId);
	
	
	
	
}
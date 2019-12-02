package com.capv.um.service;

import java.util.List;
import com.capv.um.model.User;
import com.capv.um.model.UserConfig;

/**
 * <h1>UserService</h1> this interface is used to perform user crud operations
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
public interface UserService {

	/**
	 * this method is used to save entity
	 * 
	 * @param entity the object
	 */
	void save(User user);

	void save(User user, List<UserConfig> userConfigList);

	/**
	 * this method is used to update entity
	 * 
	 * @param entity the object
	 */
	void update(User user, boolean updateVCard);

	/**
	 * this method is used to delete entity
	 * 
	 * @param entity the object
	 */
	void delete(User user);

	/**
	 * this method is used to get list of records
	 * 
	 * @param classInstance -- instance of a class
	 */
	List<User> getAllUsers();

	/**
	 * this method is used to get record by id
	 * 
	 * @param userId use id
	 */
	User getById(Long userId);

	User getById(Long userId, boolean loadUserConfig);

	/**
	 * this method is used to get record by userName
	 * 
	 * @param userName the userName
	 */
	User getByUserName(String userName, boolean loadUserConfig);

	/**
	 * this method is used to get record by clientId
	 * 
	 * @param clientId the clientId
	 */
	List<User> getByClientId(Long clientId);

	/**
	 * this method is used to get list of unmatched users
	 * 
	 * @param userName the userName
	 * @return list of unmatched users
	 */
	List<User> listUnMatchedUsers(String userName);

	/**
	 * this method is used to get the list of users by matching parameter value
	 * 
	 * @param paramValue the parameter
	 * @return list of users by matching parameter value
	 */
	List<User> listUsersByMatchingParameterVlue(String paramValue);

	/**
	 * this method is used to get list of online users
	 * 
	 * @param userName the userName
	 * @return list of online users
	 */
	List<User> getOnlineUsers(String userName);

	List<User> getUsersByPagination(int offset, int maxResults);

	List<User> getUsersByUserNames(List<String> userNames);

	Boolean checkOldPassword(String oldEncodedPassword, String username);

	Boolean changePassword(String newEncodedPassword, String username);

	void resetPassWordAndSendEmail(User user, String newPassword);

	List<User> searchUsers(List<String> searchWords, List<String> filteredUsers, int resultOffset, int maxResults);

	/**
	 * this method is used to get record by clientId
	 * 
	 * @param clientId the clientId
	 * @param start
	 * @param draw
	 */
	List<User> getUsersByPaginationUsingClientId(Long clientId, String draw, String start);

	Long getListOfUsers(Long clientId);

	List<User> getUsersByPaginationUsingClientId(Long clientId, String start, String length, String searchParam);

	Long getListOfUsersForSearch(Long clientId, String searchParam);

	Long getUserCount(Long clientId, Long activeParam);

	List<User> getListOfLoggedUsers();

	List<User> getListOfLoggedUsersForPresence();
}

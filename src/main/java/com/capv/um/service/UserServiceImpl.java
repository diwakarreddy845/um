package com.capv.um.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.util.ChatUserUtil;
import com.capv.um.model.User;
import com.capv.um.model.UserConfig;
import com.capv.um.repository.UserConfigRepository;
import com.capv.um.repository.UserRepository;
import com.capv.um.util.CapvUtil;

@Service("userService")
@Transactional("transactionManager")
public class UserServiceImpl implements UserService {

	@Autowired
	UserRepository userRepository;

	@Autowired
	EmailService email;

	@Autowired
	UserConfigService userConfigService;

	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public void save(User user) {
		userRepository.save(user);
		log.debug("Entered into userService save method");
		try {
			ChatUserUtil.saveOrUpdateUserVCard(user);
		} catch (Exception e) {
			log.error("Exception occured while saving user    :", e);
			throw new RuntimeException("user vcard save failed");
		}
		log.debug("Exit from userService save method");
	}

	@Override
	public void save(User user, List<UserConfig> userConfigList) {
		log.debug("Entered into userService save(User user, List<UserConfig> userConfigList) method");
		userRepository.save(user);
		for (UserConfig userConfig : userConfigList)
			userConfig.setUserId(user.getId());
		userConfigService.insertUserConfig(userConfigList);
		try {
			ChatUserUtil.saveOrUpdateUserVCard(user);
		} catch (Exception e) {
			log.error("Exception occured while updating user   :", e);
			throw new RuntimeException("user vcard save failed");
		}
		log.debug("Exit from userService save(User user, List<UserConfig> userConfigList) method");
	}

	@Override
	public void update(User user, boolean updateVCard) {
		log.debug("Entered into userService update method");
		userRepository.save(user);
		if (updateVCard) {
			try {
				ChatUserUtil.saveOrUpdateUserVCard(user);
			} catch (Exception e) {
				log.error("Exception occured while updating user  : ", e);
				throw new RuntimeException("user vcard update failed");
			}
		}
		log.debug("Exit from userService update method");
	}

	@Override
	public void delete(User user) {
		userRepository.delete(user);
	}

	@Override
	public User getById(Long userId) {
		Optional<User> user = userRepository.findById(userId);
		if (user.isPresent()) {
			return user.get();
		}
		return null;
	}

	@Override
	public User getById(Long userId, boolean loadUserConfig) {
		log.debug("Entered into userService getById method");
		Optional<User> user = userRepository.findById(userId);
		if (user.isPresent() && loadUserConfig) {
			Hibernate.initialize(user.get().getUserConfig());
			return user.get();
		}
		log.debug("Exit from userService getById method");
		return null;
	}

	@Override
	public User getByUserName(String userName, boolean loadUserConfig) {
		log.debug("Entered into userService getByUserName method");
		Map<String, Object> propsMap = new HashMap<>();
		propsMap.put("userName", userName);
		User user = (User) userRepository.getUniqueEntityByMatchingProperties(User.class, propsMap);
		if (loadUserConfig && user != null)
			Hibernate.initialize(user.getUserConfig());
		log.debug("Exit from userService getByUserName method");
		return user;
	}

	@Override
	public List<User> listUnMatchedUsers(String userName) {
		return userRepository.listUnMatchedUsers(userName);
	}

	@Override
	public List<User> listUsersByMatchingParameterVlue(String paramValue) {
		return userRepository.listUsersByMatchingParamValue(paramValue);
	}

	@Override
	public List<User> getOnlineUsers(String userName) {
		return userRepository.listOnlineUsers(userName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<User> getByClientId(Long clientId) {
		Map<String, Object> propsMap = new HashMap<>();
		propsMap.put("clientId", clientId);
		return (List<User>) userRepository.getEntitiesByMatchingProperties(User.class, propsMap);
	}

	public List<User> getUsersByPagination(int offset, int maxResults) {
		return userRepository.getUsersByPagination(offset, maxResults);
	}

	public List<User> getUsersByUserNames(List<String> userNames) {
		return userRepository.getUsersByUserNames(userNames);
	}

	@Override
	public Boolean checkOldPassword(String oldPassword, String username) {
		return userRepository.checkOldPassword(oldPassword, username);
	}

	@Override
	public Boolean changePassword(String newEncodedPassword, String username) {
		return userRepository.changePassword(newEncodedPassword, username);
	}

	@Override
	public void resetPassWordAndSendEmail(User user, String newPassword) {
		log.debug("Entered into userService resetPassWordAndSendEmail method");
		String encodedPassword = CapvUtil.encodePassword(newPassword);
		user.setPassword(encodedPassword);
		userRepository.save(user);
		String clientName = CapvClientUserUtil.getClientConfigProperty(user.getClientId(), CapvClientUserConstants.CAPV_UI_CLIENT_NAME);
		String userName = user.getUserName().substring(0, user.getUserName().lastIndexOf("_"));
		email.sendInviteEmail(new String[] { user.getEmail() }, clientName + ": Password Reset For user " + userName,
				"Hey &nbsp;" + user.getName() + ",<br><br>" + "Password for your acoount&nbsp" + clientName + "&nbsp is reset." + "<br>"
						+ "Please login using the new password.\r\n" + "<br><br>" + "URL:&nbsp;" + "<a href=\""
						+ CapvClientUserUtil.getClientConfigProperty(user.getClientId(), CapvClientUserConstants.CAPV_UI_URL)
						+ "\">Click to Login</a>" + "<br>Username:&nbsp;" + user.getUserName().substring(0, user.getUserName().lastIndexOf("_"))
						+ "<br>Password:&nbsp;" + newPassword + "<br><br>Thanks,\r\n" + "<br>" + clientName + "&nbsp;Support&nbsp;Team");
		CapvUtil.removeUserOAuthTokens(user.getClientId(), user.getUserName());
		log.debug("Exit from userService resetPassWordAndSendEmail method");
	}

	@Override
	public List<User> searchUsers(List<String> searchWords, List<String> filteredUsers, int resultOffset, int maxResults) {
		return userRepository.searchUsers(searchWords, filteredUsers, resultOffset, maxResults);
	}

	@Override
	public List<User> getUsersByPaginationUsingClientId(Long clientId, String start, String length) {
		Integer length1 = Integer.parseInt(length);
		Integer start1 = Integer.parseInt(start);
		return userRepository.getUsersByPaginationUsingClientId(clientId, start1, length1);
	}

	@Override
	public Long getListOfUsers(Long clientId) {
		return userRepository.getListOfUsers(clientId);
	}

	@Override
	public List<User> getUsersByPaginationUsingClientId(Long clientId, String start, String length, String searchParam) {
		Integer length1 = Integer.parseInt(length);
		Integer start1 = Integer.parseInt(start);
		return userRepository.getUsersByPaginationUsingClientId(clientId, start1, length1, searchParam);
	}

	@Override
	public Long getListOfUsersForSearch(Long clientId, String searchParam) {
		return userRepository.getListOfUsersForSearch(clientId, searchParam);
	}

	@Override
	public Long getUserCount(Long clientId, Long activeParam) {
		return userRepository.getUserCount(clientId, activeParam);
	}

	@Override
	public List<User> getListOfLoggedUsers() {
		return userRepository.getListOfLoggedUsers();
	}

	@Override
	public List<User> getListOfLoggedUsersForPresence() {
		return userRepository.getListOfLoggedUsersForPresence();
	}
}

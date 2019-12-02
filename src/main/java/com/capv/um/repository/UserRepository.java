package com.capv.um.repository;

import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.repository.JpaRepository;
import com.capv.um.model.User;


public interface UserRepository extends JpaRepository<User, Long> {

	User getUniqueEntityByMatchingProperties(Class<User> class1, Map<String, Object> propsMap);

	List<User> listUnMatchedUsers(String userName);

	List<User> listUsersByMatchingParamValue(String paramValue);

	List<User> listOnlineUsers(String userName);

	List<User> getEntitiesByMatchingProperties(Class<User> class1, Map<String, Object> propsMap);

	List<User> getUsersByPagination(int offset, int maxResults);

	List<User> getUsersByUserNames(List<String> userNames);

	Boolean checkOldPassword(String oldPassword, String username);

	Boolean changePassword(String newEncodedPassword, String username);

	List<User> searchUsers(List<String> searchWords, List<String> filteredUsers, int resultOffset, int maxResults);

	List<User> getUsersByPaginationUsingClientId(Long clientId, Integer start1, Integer length1, String searchParam);

	Long getListOfUsers(Long clientId);

	List<User> getUsersByPaginationUsingClientId(Long clientId, Integer start1, Integer length1);

	Long getListOfUsersForSearch(Long clientId, String searchParam);

	List<User> getListOfLoggedUsers();

	Long getUserCount(Long clientId, Long activeParam);

	List<User> getListOfLoggedUsersForPresence();
}

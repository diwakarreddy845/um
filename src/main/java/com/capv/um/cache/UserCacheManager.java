package com.capv.um.cache;

import java.util.List;

import com.capv.um.model.User;

public interface UserCacheManager {

	void addToUserCache(User user);
	
	CacheUserEntity getUserById(Long id);
	
	CacheUserEntity getUserByUserName(String userName);
	
	List<CacheUserEntity> searchAppUsers(String searchText, List<String> filteredUsers, 
											Long lastFetchId, int maxResults,String clientId);
	
}

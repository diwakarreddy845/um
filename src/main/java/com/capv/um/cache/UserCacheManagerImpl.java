package com.capv.um.cache;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.capv.client.user.util.ChatUserUtil;
import com.capv.um.model.User;
import com.capv.um.service.UserService;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.SearchAttribute;
import net.sf.ehcache.config.Searchable;
import net.sf.ehcache.search.Attribute;
import net.sf.ehcache.search.Direction;
import net.sf.ehcache.search.Query;
import net.sf.ehcache.search.Result;
import net.sf.ehcache.search.Results;
import net.sf.ehcache.search.expression.Criteria;
import net.sf.ehcache.search.expression.Or;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

@Component
public class UserCacheManagerImpl implements UserCacheManager {

	private CacheManager cacheManager = null;
	private Cache userCache = null;
	@Autowired
	private UserService userService;

	private void initCache() {
		// Create a singleton CacheManager using defaults
		cacheManager = CacheManager.create();
		// Create a Cache specifying its configuration.
		CacheConfiguration cacheConfiguration = new CacheConfiguration("userCache", 0).memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
				.eternal(true);
		Searchable searchable = new Searchable();
		cacheConfiguration.addSearchable(searchable);
		// Use an expression for accessing values.
		searchable.addSearchAttribute(new SearchAttribute().name("user_id").expression("key"));
		searchable.addSearchAttribute(new SearchAttribute().name("user_name").expression("value.getUserName()"));
		searchable.addSearchAttribute(new SearchAttribute().name("first_name").expression("value.getFirstName()"));
		searchable.addSearchAttribute(new SearchAttribute().name("last_name").expression("value.getLastName()"));
		searchable.addSearchAttribute(new SearchAttribute().name("clientId").expression("value.getClientId()"));
		userCache = new Cache(cacheConfiguration);
		cacheManager.addCache(userCache);
	}

	@PostConstruct
	public void loadUsersCache() {
		initCache();
		ChatUserUtil.setUserCacheManager(this);
		List<User> users = null;
		int offset = 0;
		int maxResults = 100;
		do {
			users = userService.getUsersByPagination(offset, maxResults);
			for (User user : users) {
				CacheUserEntity cacheUserEntity = getCacheUserEntityFromUserObject(user);
				userCache.put(new Element(user.getId(), cacheUserEntity));
			}
			offset = offset + maxResults;
		} while (users.size() == maxResults);
	}

	public void addToUserCache(User user) {
		if (user != null) {
			CacheUserEntity cacheUserEntity = getCacheUserEntityFromUserObject(user);
			userCache.put(new Element(user.getId(), cacheUserEntity));
		}
	}

	@Override
	public CacheUserEntity getUserById(Long id) {
		CacheUserEntity cacheUserEntity = null;
		if (userCache.get(id) != null)
			cacheUserEntity = (CacheUserEntity) userCache.get(id).getObjectValue();
		else {
			User user = userService.getById(id);
			if (user != null) {
				cacheUserEntity = getCacheUserEntityFromUserObject(user);
				userCache.put(new Element(user.getId(), cacheUserEntity));
			}
		}
		return cacheUserEntity;
	}

	@Override
	public CacheUserEntity getUserByUserName(String userName) {
		CacheUserEntity cacheUserEntity = null;
		Attribute<String> userNameAttribute = userCache.getSearchAttribute("user_name");
		Query query = userCache.createQuery();
		query.includeKeys();
		query.includeValues();
		query.addCriteria(userNameAttribute.eq(userName));
		Results usersResults = query.execute();
		for (Result userResult : usersResults.all()) {
			cacheUserEntity = (CacheUserEntity) userResult.getValue();
			break;
		}
		if (cacheUserEntity == null) {
			User user = userService.getByUserName(userName, false);
			if (user != null) {
				cacheUserEntity = getCacheUserEntityFromUserObject(user);
				userCache.put(new Element(user.getId(), cacheUserEntity));
			}
		}
		return cacheUserEntity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<CacheUserEntity> searchAppUsers(String searchText, List<String> filteredUsers, Long lastFetchId, int maxResults, String clientId) {
		if (searchText != null)
			searchText = searchText.trim();
		List<CacheUserEntity> searchUserResults = new ArrayList<>();
		List<String> searchWords = new ArrayList<>();
		if (searchText != null) {
			if (searchText.indexOf(' ') > -1) {
				searchWords.add(searchText.substring(0, searchText.lastIndexOf(' ')));
				searchWords.add(searchText.substring(searchText.lastIndexOf(' ') + 1, searchText.length()));
				searchWords.add(searchText.substring(0, searchText.indexOf(' ')));
				searchWords.add(searchText.substring(searchText.indexOf(' ') + 1, searchText.length()));
			} else {
				searchWords.add(searchText);
			}
		}
		Attribute<String> clientIdAttribute = userCache.getSearchAttribute("clientId");
		Attribute<Long> userIdAttribute = userCache.getSearchAttribute("user_id");
		Attribute<String> userNameAttribute = userCache.getSearchAttribute("user_name");
		Attribute<String> firstNameAttribute = userCache.getSearchAttribute("first_name");
		Attribute<String> lastNameAttribute = userCache.getSearchAttribute("last_name");
		Query query = userCache.createQuery();
		query.includeKeys();
		query.includeValues();
		if (searchText != null && searchText.length() > 0) {
			Criteria userNameCriteria = userNameAttribute.ilike(searchText + "*");
			Or orCriteria = null;
			for (String searchWord : searchWords) {
				if (orCriteria == null)
					orCriteria = new Or(firstNameAttribute.ilike(searchWord + "*"), lastNameAttribute.ilike(searchWord + "*"));
				else
					orCriteria = new Or(orCriteria, new Or(firstNameAttribute.ilike(searchWord + "*"), lastNameAttribute.ilike(searchWord + "*")));
			}
			query.addCriteria(new Or(userNameCriteria, orCriteria));
		}
		query.addCriteria(userNameAttribute.in(filteredUsers).not());
		query.addCriteria(clientIdAttribute.eq(clientId));
		if (lastFetchId != null && lastFetchId > 0)
			query.addCriteria(((Attribute<Long>) Query.KEY).lt(lastFetchId));
		query.addOrderBy(userIdAttribute, Direction.DESCENDING);
		query.maxResults(maxResults);
		Results usersResults = query.execute();
		for (Result userResult : usersResults.all()) {
			searchUserResults.add((CacheUserEntity) userResult.getValue());
		}
		return searchUserResults;
	}

	private CacheUserEntity getCacheUserEntityFromUserObject(User user) {
		CacheUserEntity cacheUserEntity = null;
		if (user != null)
			cacheUserEntity = new CacheUserEntity(user.getId(), user.getUserName(), user.getFirstName(), user.getLastName(), user.getEmail(),
					user.getMobile(), user.getRegistrationSource(), (user.getClientId()).toString());
		return cacheUserEntity;
	}
}
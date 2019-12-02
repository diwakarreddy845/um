package com.capv.um.rest.controller;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.capv.client.user.User;
import com.capv.client.user.UserRegistry;
import com.capv.client.user.chat.CapvChatClientManager;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserRequestProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.websocket.UserWebSocketMessage;
import com.capv.um.cache.CacheUserEntity;
import com.capv.um.cache.UserCacheManager;
import com.capv.um.model.SearchUserParams;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

@RestController
public class ProfileImagesFetchController {
	
	@Autowired
	private UserCacheManager userCacheManager;
	
	private static final Logger log = LoggerFactory.getLogger(ProfileImagesFetchController.class);
	
	@RequestMapping(value = "/profileFetch/getRosterUsersProfilePictures", method = RequestMethod.POST, produces = {
	"application/json" })
	public Map<String, Map<String,String>>  getRosterUsersProfilePictures(@RequestBody com.capv.um.model.User user,
			HttpServletRequest request, HttpServletResponse response) {
		
		JsonObject messageToSend = new JsonObject();
		Map<String, Map<String,String>> rosterUsersProfilePictures = new HashMap<>();
		
		try {
			CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
											getCapvChatUserRequestProcessor(user.getUserName().trim());
			
			List<User> rosterUsers = capvChatUserRequestProcessor.getRosterUsers();
			
			for(User rosterUser :rosterUsers) {
				
				Map<String, String> profilePictureData = 
						getCapvChatUserRequestProcessor(user.getUserName().trim()).getUserProfilePicture(rosterUser.getName());
				
				if(profilePictureData != null && profilePictureData.size() > 0) {
					rosterUsersProfilePictures.put(rosterUser.getName(), profilePictureData);
				}
			}
			
			Type profilePictureType = new TypeToken<Map<String, Map<String, String>>>(){}.getType();
			messageToSend.add("rosteUsersProfilePictures",CapvClientUserUtil.convertToJsonElement(rosterUsersProfilePictures, profilePictureType));
			
			
			return rosterUsersProfilePictures;
			
		} catch (Exception e) {
			e.printStackTrace();
			messageToSend.addProperty("Error","Unable to Retrieve");
		}
		
		return rosterUsersProfilePictures;
	}
	
	@RequestMapping(value = "/profileFetch/getTotalUsers", method = RequestMethod.POST, produces = {
	"application/json" })
	public Map<String,String> getTotalUsers(@RequestBody SearchUserParams searchParams,
			HttpServletRequest request, HttpServletResponse response) {
		
		JsonObject messageToSend = new JsonObject();
			
		CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
				getCapvChatUserRequestProcessor(searchParams.getUserName());
		Map<String,String> result = new HashMap<>();
		String searchText = null;
		Long lastFetchUserId	= null;
		int maxResults			= 25;
        String clientId="";
        
		if(searchParams.getSearchText() != null)
			searchText = searchParams.getSearchText();

		if(searchParams.getLastFetchUserId() != null) {
			String lastFetchUserString = searchParams.getLastFetchUserId();
			try {
				lastFetchUserId = new Long(lastFetchUserString);
			} catch (NumberFormatException nfe){}
		}
		if(searchParams.getMaxResults() != null) {
			String maxResultsString = searchParams.getMaxResults();
			try {
				maxResults = Integer.parseInt(maxResultsString);
			} catch (NumberFormatException nfe){}
		}
		if(maxResults < 1)
			maxResults = 25;
		if(maxResults > 100)
			maxResults = 100;
		if(searchParams.getClientId() != null) {
			 clientId = searchParams.getClientId();
		}
		List<User> userList = new ArrayList<>();
		List<User> users = null;
		try {
			users = capvChatUserRequestProcessor.getRosterUsers();

			List<String> filteredUsers = new ArrayList<>();


			for(User rosterUser : users)
				filteredUsers.add(rosterUser.getName());

			filteredUsers.add(searchParams.getUserName());

			List<CacheUserEntity> searchUserResults = userCacheManager.searchAppUsers(searchText, filteredUsers, 
					lastFetchUserId, maxResults,clientId);

			for(CacheUserEntity searchUserResult :searchUserResults) {

				User searchUser = new User();

				searchUser.setUser(searchUserResult.getUserName());
				searchUser.setFullName(searchUserResult.getFirstName() + " " 
						+ searchUserResult.getLastName());
				searchUser.setEmail(searchUserResult.getEmail());
				searchUser.setMobile(searchUserResult.getMobile());
				searchUser.setRegistrationSource(searchUserResult.getRegistrationSource());
				searchUser.setProfilePicture(capvChatUserRequestProcessor
						.getUserProfilePicture(searchUserResult.getUserName()));

				userList.add(searchUser);
			}
			Long searchResultLastFetchId = null;
			if(searchUserResults.size() == maxResults) {
				CacheUserEntity searchResultUserEntity = 
						searchUserResults.get(searchUserResults.size() - 1);
				searchResultLastFetchId = searchResultUserEntity.getId();

			}
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
					CapvClientUserConstants.WS_MESSAGE_TOTAL_USERS);
			Type objType = new TypeToken<List<User>>() {}.getType();
			jsonObject.add("userList", CapvClientUserUtil.convertToJsonElement(userList, objType));

			if(searchResultLastFetchId != null)
				jsonObject.addProperty("lastFetchUserId", searchResultLastFetchId);
			else
				jsonObject.addProperty("maxFetchReached", true);

			result.put("get_total_users", jsonObject.toString());
			
			
		} catch (Exception e) {
			e.printStackTrace();
			messageToSend.addProperty("Error","Unable to Retrieve");
			result.put("get_total_users", messageToSend.toString());
		}
		
		
		return result;
	}
	
	

	@RequestMapping(value = "/profileFetch/searchUser", method = RequestMethod.POST, produces = {
	"application/json" })
	public Map<String,String> getSearchUsers(@RequestBody SearchUserParams searchParams,
			HttpServletRequest request, HttpServletResponse response) {
		
		Map<String,String> result =new HashMap<String, String>();
		JsonObject messageToSend = new JsonObject();
		String clientId="";
		if(searchParams.getUser() != null) {
    		String searchText = searchParams.getUser();
    		
    		Long lastFetchUserId	= 0l;
    		int maxResults			= 25;
    		
    		if(searchParams.getLastFetchUserId() != null) {
    			String lastFetchUserString = searchParams.getLastFetchUserId();
    			try {
    				lastFetchUserId = new Long(lastFetchUserString);
    			} catch (NumberFormatException nfe){}
    		}
    		if(searchParams.getMaxResults() != null) {
    			String maxResultsString = searchParams.getMaxResults();
    			try {
    				maxResults = Integer.parseInt(maxResultsString);
    			} catch (NumberFormatException nfe){}
    		}
    		if(maxResults < 1)
    			maxResults = 25;
    		if(maxResults > 100)
    			maxResults = 100;
    		if(searchParams.getClientId() != null) {
    			clientId = searchParams.getClientId();
    		}
        	List<User> userList = new ArrayList<>();
        	List<User> users	= null;
        	CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
										getCapvChatUserRequestProcessor(searchParams.getUserName());
        	try {
				users = capvChatUserRequestProcessor.getRosterUsers();
				List<String> filteredUsers = new ArrayList<>();
				
				
				for(User rosterUser : users)
					filteredUsers.add(rosterUser.getName());
				
				filteredUsers.add(searchParams.getUserName());
				
				List<CacheUserEntity> searchUserResults = userCacheManager.searchAppUsers(searchText, filteredUsers, 
																						lastFetchUserId, maxResults,clientId);
				
				for(CacheUserEntity searchUserResult :searchUserResults) {
					
					User searchUser = new User();
					
					searchUser.setUser(searchUserResult.getUserName());
					searchUser.setFullName(searchUserResult.getFirstName() + " " 
											+ searchUserResult.getLastName());
					searchUser.setEmail(searchUserResult.getEmail());
					searchUser.setMobile(searchUserResult.getMobile());
					searchUser.setRegistrationSource(searchUserResult.getRegistrationSource());
					searchUser.setProfilePicture(capvChatUserRequestProcessor
													.getUserProfilePicture(searchUserResult.getUserName()));
					
					userList.add(searchUser);
				}
				
				Long searchResultLastFetchId = null;
				if(searchUserResults.size() == maxResults) {
					CacheUserEntity searchResultUserEntity = 
									searchUserResults.get(searchUserResults.size() - 1);
					searchResultLastFetchId = searchResultUserEntity.getId();
					
				}
				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
										CapvClientUserConstants.WS_MESSAGE_SEARCH_USER);
				Type objType = new TypeToken<List<User>>() {}.getType();
				jsonObject.add("userList", CapvClientUserUtil.convertToJsonElement(userList, objType));
				
				if(searchResultLastFetchId != null)
					jsonObject.addProperty("lastFetchUserId", searchResultLastFetchId);
				else
					jsonObject.addProperty("maxFetchReached", true);
				
				result.put("get_search_users", jsonObject.toString());
				
				return result;
				
			} catch (Exception e) {
				e.printStackTrace();
				messageToSend.addProperty("Error","Unable to Retrieve");
				result.put("get_search_users", messageToSend.toString());
			}
    	}
		
		
		return result;
	}

	private CapvChatUserRequestProcessor getCapvChatUserRequestProcessor(String userName) {
		
		CapvChatUserRequestProcessor capvChatUserRequestProcessor = null;
		
		if(userName != null  && 
				CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName) != null) {
			
			CapvChatClientManager capvChatClientManager = CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName);
			
			capvChatUserRequestProcessor = (capvChatClientManager.getCapvChatUserRequestProcessor() != null) ? 
														capvChatClientManager.getCapvChatUserRequestProcessor() : null;
				log.info("sharath_test2"+userName.trim());
		}
			
		return capvChatUserRequestProcessor;
	}

}

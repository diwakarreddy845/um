package com.capv.um.util;

import java.util.ArrayList;
import java.util.List;

import org.igniterealtime.restclient.entity.UserEntities;
import org.igniterealtime.restclient.entity.UserEntity;
import org.igniterealtime.restclient.entity.UserProperty;

import com.capv.um.constants.CapvConstants;
import com.capv.um.view.UserView;

public class EntityToViewConverter {

	public static UserView convertToUserView(UserEntity userEntity) {
		
		UserView userView = null;
		
		if(userEntity != null) {
			
			Long clientId = 0l;
			String mobile = null;
			List<UserProperty> userProperties = userEntity.getProperties();
			
			for(UserProperty userProperty :userProperties) {
				
				if(userProperty.getKey().equals(CapvConstants.CLIENT_ID_KEY))
					clientId = new Long(userProperty.getValue());
				if(userProperty.getKey().equals(CapvConstants.MOBILE_KEY))
					mobile = userProperty.getValue();
				
			}
			
			userView = new UserView(clientId, userEntity.getUsername(), 
									userEntity.getPassword(), userEntity.getName(),
									userEntity.getEmail(), mobile);
			
		}
		
		return userView;
	}
	
	public static List<UserView> convertToUserViewList(UserEntities userEntities) {
		
		List<UserView> userViewList = null;
		UserView userView			= null;
		List<UserEntity> users		= null;
		
		if(userEntities != null) {
			
			users = userEntities.getUsers();
			
			if(users != null && users.size() > 0) {
				
				userViewList = new ArrayList<>();
				
				for(UserEntity userEntity :users) {
					userView = convertToUserView(userEntity);
					userViewList.add(userView);
				}
			}
			
		}
		
		return userViewList;
	}
}

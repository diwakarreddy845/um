package com.capv.um.util;

import java.util.ArrayList;
import java.util.List;

import org.igniterealtime.restclient.entity.UserEntity;
import org.igniterealtime.restclient.entity.UserProperty;

import com.capv.um.constants.CapvConstants;
import com.capv.um.view.UserView;

public class ViewToEntityConverter {
	
	public static UserEntity convertUserViewToEntity(UserView userView) {
		
		UserEntity userEntity		= null;
		UserProperty userProperty	= null;
		
		if(userView != null) {
			userEntity = new UserEntity(userView.getUserName(), 
										userView.getName(),
										userView.getEmail(),
										userView.getPassword());
			
			List<UserProperty> userProperties = new ArrayList<>();
			userProperty = new UserProperty(CapvConstants.CLIENT_ID_KEY, userView.getClientId().toString());
			userProperties.add(userProperty);
			
			if(userView.getMobile() != null) {
				userProperty = new UserProperty(CapvConstants.MOBILE_KEY, userView.getMobile());
				userProperties.add(userProperty);
			}
			
			userEntity.setProperties(userProperties);
		}
		
		return userEntity;
	}

}

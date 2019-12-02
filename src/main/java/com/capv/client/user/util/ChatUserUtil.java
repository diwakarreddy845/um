package com.capv.client.user.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.listener.CapvChatClientConnectionListener;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.um.cache.CacheUserEntity;
import com.capv.um.cache.UserCacheManager;
import com.capv.um.model.User;
import com.capv.um.model.UserConfig;
import com.capv.um.util.CapvUtil;
import com.google.gson.JsonObject;

public class ChatUserUtil {
	
	private static UserCacheManager userCacheManager;
	
	public static CacheUserEntity getCacheUserEntity(String userName) {
		return userCacheManager.getUserByUserName(userName);
	}
	
	public static String getUserFullName(String userName) {
		
		String userFullName = null;
		
		CacheUserEntity cacheUserEntity = userCacheManager.getUserByUserName(userName);
		
		if(cacheUserEntity != null && cacheUserEntity.getFirstName() != null) {
			
			StringBuffer userFullNameBuffer = new StringBuffer(cacheUserEntity.getFirstName());
			
			if(cacheUserEntity.getLastName() != null) {
				userFullNameBuffer.append(" ").append(cacheUserEntity.getLastName());
			}
			
			userFullName = userFullNameBuffer.toString();
		}
		
		return userFullName;
	}
	
	public static String getUserFullNameFromCacheUser(CacheUserEntity cacheUser) {
		
		if(cacheUser.getFirstName() != null) {
			
			StringBuffer userFullNameBuffer = new StringBuffer(cacheUser.getFirstName());
			if(cacheUser.getLastName() != null)
				userFullNameBuffer.append(" ").append(cacheUser.getLastName());
			
			return userFullNameBuffer.toString();
		}
		return null;
	}
	
	public static void saveOrUpdateUserVCard(User user) throws Exception {
		XMPPTCPConnection chatUserConnection = 
								CapvChatClientManagerRegistry.getChatUserConnectionByUserName(user.getUserName());
		
		if(chatUserConnection != null)
			checkAndUpdateUserVCard(chatUserConnection, user);
		else {
			Thread updateVCardThread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						int chatServerPort = CapvClientUserConstants.XMPP_SERVER_DEFAULT_PORT;
						
						String chatServiceHost = CapvClientUserUtil.getClientConfigProperty(user.getClientId(), CapvClientUserConstants.CHAT_SERVER_HOST_NAME_KEY);
						try {
							chatServerPort = Integer.parseInt(CapvClientUserUtil.getClientConfigProperty(user.getClientId(), CapvClientUserConstants.CHAT_SERVER_PORT_KEY));
						} catch (Exception e) {
							e.printStackTrace();
						}
						String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(user.getClientId(), CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
						
						OAuth2Authentication oauth2Authentication = 
											CapvUtil.getOAuth2AuthenticationByUser(user.getClientId(), user.getUserName());
						
						if(oauth2Authentication != null && oauth2Authentication.getCredentials() != null && 
								oauth2Authentication.getUserAuthentication().getCredentials() instanceof String) {
							XMPPTCPConnectionConfiguration connectionConfiguration = XMPPTCPConnectionConfiguration.builder()
															.setSecurityMode(SecurityMode.disabled)
															.setHost(chatServiceHost)
															.setPort(chatServerPort)
															.setServiceName(chatServerServiceName)
															.setUsernameAndPassword(user.getUserName(), 
																					(String)oauth2Authentication.getUserAuthentication().getCredentials())
															.setConnectTimeout(5000)
															.build();


							XMPPTCPConnection chatUserConnection2 = new XMPPTCPConnection(connectionConfiguration);
							chatUserConnection2.setPacketReplyTimeout(5000);
							CapvChatClientConnectionListener capvChatClientConnectionListener = new CapvChatClientConnectionListener(null);
							chatUserConnection2.addConnectionListener(capvChatClientConnectionListener);
							
							chatUserConnection2.connect();
							int retryCount = 0;
							while(true){
								retryCount++;
								
								if(retryCount < 5) {
									if(chatUserConnection2.isAuthenticated()) {
										checkAndUpdateUserVCard(chatUserConnection2, user);
										chatUserConnection2.removeConnectionListener(capvChatClientConnectionListener);
										chatUserConnection2.disconnect();
										break;
									} else {
										try {
											Thread.sleep(1000);
										} catch (Exception e){}
									}
								} else
									break;
							}
						}
					} catch (Exception e){}
				}
			});
			updateVCardThread.start();
		}
	}
	
	public static void checkAndUpdateUserVCard(XMPPTCPConnection chatUserConnection, User dbUser) throws Exception {
		
		if(chatUserConnection != null) {
			VCardManager vCardManager = VCardManager.getInstanceFor(chatUserConnection);
			
			VCard userVCard = vCardManager.loadVCard();
			
			if(userVCard == null)
				userVCard = new VCard();
			
			if((userVCard.getFirstName() == null || !userVCard.getFirstName().equals(dbUser.getFirstName()))
					|| (userVCard.getLastName() == null || !userVCard.getLastName().equals(dbUser.getFirstName()))
					|| (dbUser.getEmail() != null && (userVCard.getEmailHome() == null || !userVCard.getEmailHome().equals(dbUser.getEmail())))
					|| (dbUser.getMobile() != null && (userVCard.getPhoneHome("VOICE") == null || !userVCard.getPhoneHome("VOICE").equals(dbUser.getMobile())))
					|| (dbUser.getUserConfig() != null && userVCard.getField("privacy") == null)) {
					
				userVCard.setFirstName(dbUser.getFirstName());
				userVCard.setLastName(dbUser.getLastName());
				userVCard.setEmailHome(dbUser.getEmail());
				userVCard.setPhoneHome("VOICE", dbUser.getMobile());
	    		
				addUserPrivacySettingsToVCard(dbUser.getUserConfig(), userVCard);
				
	    		vCardManager.saveVCard(userVCard);
			}
		}
	}
	
	public static void checkAndUpdateUserVCardPrivacySettings(XMPPTCPConnection chatUserConnection, List<UserConfig> userConfigList) throws Exception {
		
		if(chatUserConnection != null) {
			
			VCardManager vCardManager = VCardManager.getInstanceFor(chatUserConnection);
			
			VCard userVCard = vCardManager.loadVCard();
			
			if(userVCard == null)
				userVCard = new VCard();
			
			if(addUserPrivacySettingsToVCard(userConfigList, userVCard)) {
				vCardManager.saveVCard(userVCard);
			}
		}
	}
	
	private static boolean addUserPrivacySettingsToVCard(List<UserConfig> userConfigList, VCard userVCard) {
		
		boolean addedPrivacySettingsToVCard = false;
		Map<String,String> privacySettings = new HashMap<String,String>();
		
		for(UserConfig userConfig : userConfigList){
			if(userConfig.getConfigProperty().getConfigType().equalsIgnoreCase("privacy")){
				privacySettings.put(userConfig.getConfigProperty().getName(), userConfig.getPropValue());
			}
		}
		if(privacySettings.size()>0) {
				if(userVCard == null)
					userVCard = new VCard();
			   String privacyStr = userVCard.getField("privacy");
			   JsonObject vcardPrivacyObj = null;
			   
			   if(privacyStr != null)
			    vcardPrivacyObj = CapvClientUserUtil.convertToJsonObject(privacyStr);
			   else
			    vcardPrivacyObj = new JsonObject();
			   
			   for (String prop : privacySettings.keySet()) {
			          vcardPrivacyObj.addProperty(prop, privacySettings.get(prop));
			        }
			   userVCard.setField("privacy", vcardPrivacyObj.toString());
			   
			   addedPrivacySettingsToVCard = true;
		}
		
		return addedPrivacySettingsToVCard;
	}
	
	public static JsonObject getUserPrivacy(XMPPTCPConnection chatUserConnection) {
		JsonObject userPrivacy = null;
		
		if(chatUserConnection != null) {
			
			try {
				VCardManager vCardManager = VCardManager.getInstanceFor(chatUserConnection);
				VCard userVCard = vCardManager.loadVCard();
				
				if(userVCard != null && userVCard.getField("privacy") != null) {
					String privacyString = userVCard.getField("privacy");
					userPrivacy = CapvClientUserUtil.convertToJsonObject(privacyString);
				}
			} catch (Exception e){}
			
		}
		
		return userPrivacy;
	}
	
	public static void setUserCacheManager(UserCacheManager userCacheManager) {
		ChatUserUtil.userCacheManager = userCacheManager;
	}
	
}

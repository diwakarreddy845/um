package com.capv.client.user.chat.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.roster.packet.RosterPacket.ItemStatus;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.search.ReportedData;
import org.jivesoftware.smackx.search.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearchManager;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capv.client.user.User;
import com.capv.client.user.chat.CapvChatClientConfiguration;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.chat.listener.CapvChatConnectionClosingListener;
import com.capv.client.user.chat.listener.CapvChatUserPresenceListener;
import com.capv.client.user.chat.listener.CapvChatUserSubscribeListener;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.util.ChatUserUtil;
import com.capv.um.cache.CacheUserEntity;
import com.capv.um.service.UserService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
/**
 * <h1>Handler for user management requests</h1>
 * 
 * This handler class is used to handle the user management and presence change requests
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvChatUserManagementHandler {
	
	private String userName;
	
	private CapvChatConnectionClosingListener capvChatConnectionClosingListener;
	
	private static final Logger log = LoggerFactory.getLogger(CapvChatUserManagementHandler.class);
	/**
	 * This parameterized constructor is used to initialize the CapvChatUserManagementHandler
	 * @param capvChatUserRequestProcessor	The reference of CapvChatUserRequestProcessor
	 * 										@see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 * 
	 */
	public CapvChatUserManagementHandler(String userName) {
		this.userName = userName;
		initializeListeners();
	}
	
	/**
	 * This method is used to initialize listeners to process user subscription(friend) requests and presence changes
	 *
	 */
	@SuppressWarnings("unchecked")
	private void initializeListeners() {
		
		StanzaListener chatUserSubscribeListener = new CapvChatUserSubscribeListener(userName);
		
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
			
			chatUserConnection.addAsyncStanzaListener(chatUserSubscribeListener, 
														new StanzaTypeFilter(Presence.class));
			Roster roster = Roster.getInstanceFor(chatUserConnection);
			
			CapvChatUserPresenceListener capvChatUserPresenceListener = new CapvChatUserPresenceListener(userName);
			roster.addRosterListener(capvChatUserPresenceListener);
			
			capvChatConnectionClosingListener = (XMPPTCPConnection chatConnection) -> {
													chatConnection.removeAsyncStanzaListener(chatUserSubscribeListener);
													roster.removeRosterListener(capvChatUserPresenceListener);
													
													try {
														final Field roster_INSTANCES = Roster.class.getDeclaredField("INSTANCES");
														roster_INSTANCES.setAccessible(true);
														
														final Map<XMPPConnection, Roster> roster_INSTANCES_Map = 
																				(Map<XMPPConnection, Roster>) roster_INSTANCES.get(null);
														roster_INSTANCES_Map.remove(chatConnection);
													} catch(Exception e){
														log.error("Exception  occured while initializing Listeners :",e);
													}
												};
												
			CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName)
											.registerChatConnectionClosingListener(
														capvChatConnectionClosingListener);
			
		}
		
	}
	
	/**
	 * This method is used to remove friends from the user Roster
	 * 
	 * @param friendsList list friends needs to be delete from the Roster
	 * 
	 * @throws Exception if unable process the request
	 */
	 public void removeFriends(JsonArray friendsList) throws Exception{
		 
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
			Roster roster = Roster.getInstanceFor(chatUserConnection);
			for(JsonElement buddy : friendsList){
	    		 
				RosterEntry rosterEntry = roster.getEntry(buddy.getAsString()+"@"+chatUserConnection.getServiceName());
	    	 
				if(rosterEntry != null) {
		    		  
					Presence presence = new Presence(Presence.Type.unsubscribe);
					presence.setTo(rosterEntry.getUser());
					presence.setStatus("Offline");
					chatUserConnection.sendStanza(presence);
						
					roster.removeEntry(rosterEntry);
					log.debug("buddy is removed");
				}
	    	 }
		}
    }
    
	 /**
	 * This method is used to add friend to the user Roster
	 * 
	 * @param name name of the friend
	 *
	 * @throws Exception if unable process the request
	 */  
    public void addBuddy(String name, String fullName) throws Exception {
    	 
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
			 String buddyId = name + "@" + chatUserConnection.getServiceName();
			 
			 log.debug(String.format("Creating entry for buddy '%1$s' with name %2$s", buddyId, name));
		     Roster roster = Roster.getInstanceFor(chatUserConnection);
		     
		     if(roster.getEntry(buddyId) == null) {
		    	 /*Presence presence = new Presence(Type.subscribe);
		    	 
		    	 presence.setTo(buddyId);
		    	 presence.addExtension(new Nick(fullName));
		    	 chatUserConnection.sendStanza(presence);*/
		    	 
		    	roster.createEntry(buddyId, fullName, null);
		    	 
		     }
		}
    }
    
    /**
	 * This method is used to get all the user friends and friend requests
	 * 
	 * @return list of user friends and friend requests
	 */
    public List<User> getRosterUsers() {
    	
    	List<User> rosterUsers	= new ArrayList<>();
    	User user				= null;
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
	    	Roster roster = Roster.getInstanceFor(chatUserConnection);
	    	
	    	try {
				 roster.reloadAndWait();
			} catch (Exception e) {
				log.error("Exception occured while getting roster users :",e);
			}
	    	
	    	Set<RosterEntry> entries = roster.getEntries();
	    	
	    	for(RosterEntry entry :entries) {
	    		
	    		user = new User();
	    		
	    		user.setUser(entry.getUser());
	    		user.setName(entry.getUser().substring(0, entry.getUser().indexOf("@")));
	    		
	    		CacheUserEntity userDetails = ChatUserUtil.getCacheUserEntity(user.getName());
				
	    		if(userDetails != null) {
	    			user.setFullName(ChatUserUtil.getUserFullNameFromCacheUser(userDetails));
	    			user.setEmail(userDetails.getEmail());
	    			user.setMobile(userDetails.getMobile());
	    		}
	    		
	    		if(entry.getType().equals(RosterPacket.ItemType.none))
		    		if(entry.getStatus() == null  || entry.getStatus().equals(ItemStatus.SUBSCRIPTION_PENDING))
		    		       user.setStatusPending(true);
	    		Presence presence = roster.getPresence(entry.getUser());
	    		if(presence.isAvailable())
	    			user.setStatus(presence.getMode().toString());
	    		else
	    			user.setStatus(presence.getType().toString());
	    		
	    		rosterUsers.add(user);
	    	}
		}
    	
    	return rosterUsers;
    }
    
    /**
	 * This method is used to get the user friends
	 * 
	 * @return list of user friends
	 */
    public List<User> getUsers() {
    	
    	List<User> users = new ArrayList<>();
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
	    	Roster roster = Roster.getInstanceFor(chatUserConnection);
	    	
	    	if(!roster.isLoaded()) {
	    		while(!roster.isLoaded()) {
	    			try {
	    				 roster.reloadAndWait();
	        		} catch (Exception e) {
	        			log.error("Exception occured while getting users :",e);
	        		}
	    		}
	    	}
	    	
	    	Set<RosterEntry> entries = roster.getEntries();
	    	
	    	User user = null;
	    	for(RosterEntry entry :entries){
	    		
	    		if(entry.getType() != null) {
	    			 if(!entry.getType().equals(RosterPacket.ItemType.none)) {
	    				 
	    				user = new User();
	    				
			    		user.setUser(entry.getUser());
			    		user.setName(entry.getUser().substring(0, entry.getUser().indexOf("@")));
			    		
			    		CacheUserEntity userDetails = ChatUserUtil.getCacheUserEntity(user.getName());
			    				
			    		if(userDetails != null) {
			    			user.setFullName(ChatUserUtil.getUserFullNameFromCacheUser(userDetails));
			    			user.setEmail(userDetails.getEmail());
			    			user.setMobile(userDetails.getMobile());
			    		}
			    		user.setPrivacy(ChatUserUtil.getUserPrivacy(chatUserConnection));
			    		
			    		if(entry.getType().equals(RosterPacket.ItemType.to)) {
			    			try {
		    					 Presence presence = new Presence(Type.subscribed);
		        				 presence.setTo(entry.getUser());
		        				 chatUserConnection.sendStanza(presence);
		    				} catch (Exception e){
		    					log.error("Exception occured while getting users  :",e);
		    				}
			    		}
			    		
			    		Presence presence = roster.getPresence(entry.getUser());
			    		if(presence.isAvailable())
	  		    			user.setStatus(presence.getMode().toString());
	  		    		else
	  		    			user.setStatus(presence.getType().toString());
			    		
			    		users.add(user);
			    		
	    			 }
	    		}
	    	}
		}
    	return users;
    }
    
    public Map<String, String> getUserProfilePicture(String profilePicUserName) throws Exception {
    	
    	String userProfilePictureData 			= null;
    	XMPPTCPConnection chatUserConnection	= CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
    	Map<String, String>	profilePicData		= null;
    	
    	VCardManager vCardManager = VCardManager.getInstanceFor(chatUserConnection);
    	VCard vCard = vCardManager.loadVCard(profilePicUserName + "@" + chatUserConnection.getServiceName());
    	
    	if(vCard != null && vCard.getAvatar() != null) {
    		profilePicData = new HashMap<>();
    		byte[] profilePictureBytes	= vCard.getAvatar();
    		String profilePicFileType	= vCard.getAvatarMimeType();
    		
    		userProfilePictureData = Base64.getEncoder().encodeToString(profilePictureBytes);
    		profilePicData.put("profilePic", userProfilePictureData);
    		profilePicData.put("profilePicType", profilePicFileType);
    	}
    	
    	return profilePicData;
    }
    
    public void uploadUserProfilePicture(String profilePictureData) throws Exception {
    	
    	XMPPTCPConnection chatUserConnection					= CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
    	CapvChatClientConfiguration capvChatClientConfiguration	= CapvChatClientManagerRegistry.getChatClientConfigurationByUserName(userName);
    	UserService userService									= capvChatClientConfiguration.getUserService();
    	
    	VCardManager vCardManager = VCardManager.getInstanceFor(chatUserConnection);
    	VCard vCard = vCardManager.loadVCard();
    	
    	if(vCard == null)
    		vCard = new VCard();
    	
    	if(vCard.getFirstName() == null || vCard.getLastName() == null) {
    		com.capv.um.model.User user = userService.getByUserName(userName, false);
    		
    		vCard.setFirstName(user.getFirstName());
    		vCard.setLastName(user.getLastName());
    		vCard.setEmailHome(user.getEmail());
    		vCard.setPhoneHome("VOICE", user.getMobile());
    	}
    	vCard.setAvatar(profilePictureData, "image/jpeg");
    	vCardManager.saveVCard(vCard);
    }
    
    /**
	 * This method is used to get all the registered users in application
	 * 
	 * @return list of registered users in application
	 * 
	 * @throws Exception if unable process the request
	 */
    public List<User> getAllUsers() throws Exception {
    	
		List<User> userList = new ArrayList<>();
       
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
			UserSearchManager search = new UserSearchManager(chatUserConnection);
			Collection<String> services= search.getSearchServices();
			
			if (!services.isEmpty()){
				
				Form searchForm = search.getSearchForm("search."+chatUserConnection.getServiceName());
				Form answerForm = searchForm.createAnswerForm();  
				List<FormField> test= searchForm.getFields();
				test.isEmpty();
				
				answerForm.setAnswer("Username", true);
				answerForm.setAnswer("Name", true);
				answerForm.setAnswer("Email", true);
				answerForm.setAnswer("search", "*");  
				
				ReportedData data = search.getSearchResults(answerForm,"search."+chatUserConnection.getServiceName());  
	             
				if(data.getRows() != null)
				{
					List<Row> it =  data.getRows();
					for(int i=0;i<it.size();i++)
					{
						Row row = it.get(i);
	                    List<String> userNames =  row.getValues("username");
	                    List<String> names =  row.getValues("Name");
	                    List<String> emails =  row.getValues("Email");
	                    
	                    String userName = userNames.get(0);
	                    String fullName = names.get(0);
	                    String email = emails.get(0);
	                    
	                    User user = new User();
	                    user.setUser(userName);
	                    user.setFullName(fullName);
	                    user.setEmail(email);
	                    
	                    userList.add(user);
					}
				}
			}
		}
          
		return userList;
    }
  
    /**
	 * This method is used to search registered users in the application
	 * 
	 * @param user search string for user search
	 * 
	 * @throws Exception if unable process the request
	 */
    public List<User> searchUsers(String user) throws Exception {
    	
    	List<User> userList = new ArrayList<User>();
    	List<User> fullNameMatchedList = new ArrayList<>();
    	List<User> matchedList = new ArrayList<>();
    	
    	String searchText = user.trim();
    	
    	if(user != null){
    		
    		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
   		 
    		if(chatUserConnection != null) {
        	 
				UserSearchManager search = new UserSearchManager(chatUserConnection);
				Collection<String> services= search.getSearchServices();
				
				if (!services.isEmpty() && searchText.length() > 0){
					
					List<String> searchUserFindStrings = new ArrayList<>();
					
					String[] splittedSearchWords = searchText.split("\\s+");
					
					if(splittedSearchWords.length > 2) {
						
						//Take first word and last word as last name of user as user may give last name as first word or last word in the search text
							
						searchUserFindStrings.add(splittedSearchWords[0]);
						searchUserFindStrings.add(splittedSearchWords[(splittedSearchWords.length - 1)]);
						
						
						//Take middle words of search text to match the first name of the user
						StringBuffer searchStringMiddleWords = new StringBuffer();
						
						for(int i=1; i<(splittedSearchWords.length - 1); i++) {
							searchStringMiddleWords.append(splittedSearchWords[i]);
							
							if(i != (splittedSearchWords.length - 2))
								searchStringMiddleWords.append(" ");
						}
						
						searchUserFindStrings.add(searchStringMiddleWords.toString());
							
					} else
						searchUserFindStrings = Arrays.asList(splittedSearchWords);
	        		 
					Form searchForm = search.getSearchForm("search."+chatUserConnection.getServiceName());
					Form answerForm = searchForm.createAnswerForm();  
					
					answerForm.setAnswer("Name", true);
					
					FormField field = answerForm.getField("search");
					field.addValues(searchUserFindStrings);
					//answerForm.setAnswer("search", searchText);
	             
					ReportedData data = search.getSearchResults(answerForm,"search."+chatUserConnection.getServiceName());
	        		 
	        		if(data.getRows() != null)
	        		{
	        			List<Row> it =  data.getRows();
	        			 
	        			for(int i=0;i<it.size();i++)
	        			{
	        				Row row = it.get(i);
	        				
	        				List<String> names =  row.getValues("Name");
	        				List<String> userNames =  row.getValues("username");
	        				List<String> emails =  row.getValues("Email");
	        				
	        				if(names != null && names.size() > 0) {
	        					String userFullName = names.get(0);
	        					String userName  = userNames.get(0);
	        					String email = emails.get(0);
	        					
	        					String userFullNameLowerCase = userFullName.toLowerCase();
	        					
	        					String firstName = null;
	        					String lastName = null;
	        					
	        					if(userFullNameLowerCase.indexOf(' ') > 0) {
	        						firstName = userFullNameLowerCase.substring(0, userFullName.lastIndexOf(' '));
	        						lastName = userFullNameLowerCase.substring(userFullName.lastIndexOf(' ') + 1);
	        					} else
	        						firstName = userFullNameLowerCase;
	        					
	        					if(firstName != null && lastName != null) {
	        						if(isSearchUserMatchedWithName(searchText.toLowerCase(), firstName, lastName)) {
	        							User userObj =  new User();
	    								userObj.setUser(userName);
	    								userObj.setFullName(userFullName);
	    								userObj.setEmail(email);
	    								
	    								fullNameMatchedList.add(userObj);
	        						} else {
	        							for(String searchWord :searchUserFindStrings) {
	        								searchWord = searchWord.toLowerCase();
	        								
		        							if(searchWord.length() != 1 && 
		        									(firstName.startsWith(searchWord) || 
		        									lastName.startsWith(searchWord))) {
		        								
	        									User userObj =  new User();
			    								userObj.setUser(userName);
			    								userObj.setFullName(userFullName);
			    								userObj.setEmail(email);
			    								
			    								matchedList.add(userObj);
			    								break;
		        							}
		        						}
	        						}
	        						
	        					} else if((userFullNameLowerCase.startsWith(searchText.toLowerCase()) || 
	        									userFullNameLowerCase.endsWith(searchText.toLowerCase()))) {
	        						User userObj =  new User();
    								userObj.setUser(userName);
    								userObj.setFullName(userFullName);
    								userObj.setEmail(email);
    								
    								matchedList.add(userObj);
	        					}
	        					
	        				}
	        				
	        			}
	        			userList.addAll(fullNameMatchedList);
	        			userList.addAll(matchedList);
	        		}
	        	}
    		}
        }
        return userList;
    }
    
    private boolean isSearchUserMatchedWithName(String searchText, String firstName, String lastName) {
    	
    	boolean isSearchUserMatchedWithName = false;
    	
    	if(searchText.indexOf(' ') > 0) {
    		
			String searchFirstword = searchText.substring(0, searchText.lastIndexOf(' '));
			String searchLastWord = searchText.substring(searchText.lastIndexOf(' ') + 1);
			
			if((firstName.startsWith(searchFirstword) && lastName.startsWith(searchLastWord)) || 
					(lastName.startsWith(searchFirstword) && firstName.startsWith(searchLastWord)))
				isSearchUserMatchedWithName = true;
			
		} else if(firstName.startsWith(searchText) || lastName.startsWith(searchText))
			isSearchUserMatchedWithName = true;
    	
    	return isSearchUserMatchedWithName;
    }
    
    /**
	 * This method is used accept the friend request send by user
	 * 
	 * @param name of your friend
	 *  
	 * @throws Exception if unable process the request
	 */
    public void acceptFreindRequest(String name) throws NotConnectedException{
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
			
			String buddyId = name + '@' + chatUserConnection.getServiceName();
			
			String userFullName = ChatUserUtil.getUserFullName(name);
			
			try {
				/*Roster roster = Roster.getInstanceFor(chatUserConnection);
				roster.createEntry(buddyId, userFullName, null)*/;
				
				RosterPacket rosterPacket = new RosterPacket();
		        rosterPacket.setType(IQ.Type.set);
		        RosterPacket.Item item = new RosterPacket.Item(buddyId, userFullName);
		        rosterPacket.addRosterItem(item);
		        chatUserConnection.createPacketCollectorAndSend(rosterPacket).nextResultOrThrow();
			} catch (Exception e){
				log.error("Exception occured while accepting Freind Request  :",e);
			}
			
			Presence friendSubscription = new Presence(Presence.Type.subscribed);
			friendSubscription.setTo(buddyId); 
	        chatUserConnection.sendStanza(friendSubscription);
	        
	        //roster.getEntry(buddyId).setName(fullName);
		}
        
        /*Presence userSubscription = new Presence(Presence.Type.subscribed);
        userSubscription.setTo(chatUserConnection.getUser());
        userSubscription.setFrom(name + '@' + chatUserConnection.getServiceName());
        chatUserConnection.sendStanza(userSubscription);*/
    }
    
    /**
	 * This method used to decline the friend request send by user
	 * 
	 * @param name of your friend
	 * 
	 * @throws Exception if unable process the request
	 */
    public void declineFreindRequest(String name) throws NotConnectedException{
		  
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
			Presence unsubscribed = new Presence(Presence.Type.unsubscribed);
			unsubscribed.setTo(name + '@' + chatUserConnection.getServiceName()); 
			chatUserConnection.sendStanza(unsubscribed);
		}
    }
    
    /**
	 * This method used to change the user presence and status
	 * 
	 * @param presence	presence mode
	 * @param status	presence status message
	 * 
	 * @throws Exception if unable process the request
	 */
    public void chageUserPresence(String presence, String status) throws Exception {
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
	    	Presence userPresence = new Presence(Presence.Type.available, status, 128, Presence.Mode.valueOf(presence));
	    	
	    	chatUserConnection.sendStanza(userPresence);
	    	
	    	JsonObject messageToSend = new JsonObject();
			
			messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
										CapvClientUserConstants.WS_MESSAGE_BUDDY_STATUS);
			messageToSend.addProperty("status", status);
			messageToSend.addProperty("presence", presence);
			messageToSend.addProperty("from", userName);
			
			String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
			
			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
		}
    	
    }
    
    /**
	 * This method used to get the use presence mode
	 * 
	 * @param userName The user name
	 */
    public void getUserPresence(String userName) {
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		 
		if(chatUserConnection != null) {
	    	String xmppUserName = userName + "@" + chatUserConnection.getServiceName();
	    		
			Roster userRoster = Roster.getInstanceFor(chatUserConnection);
			
			try {
				userRoster.reloadAndWait();
			} catch(Exception e){
				log.error("Exception  occured while getting user presence  :",e);
			}
			
			if(userRoster != null && userRoster.getEntry(xmppUserName) != null) {
				Presence userPresence = userRoster.getPresence(xmppUserName);
				
				if(userPresence != null) {
					JsonObject messageToSend = new JsonObject();
					
					messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_BUDDY_STATUS);
					messageToSend.addProperty("status", userPresence.getStatus());
					if(userPresence.getType() == Presence.Type.available)
						messageToSend.addProperty("presence", userPresence.getMode().toString());
					else
						messageToSend.addProperty("presence", userPresence.getType().toString());
					messageToSend.addProperty("from", userName);
					
					String userMessage = CapvClientUserUtil.convertToJsonString(messageToSend);
					
					CapvChatUserMessageProcessor.sendChatClientMessageToUser(this.userName, userMessage);
				}
			}
		}
    }

}

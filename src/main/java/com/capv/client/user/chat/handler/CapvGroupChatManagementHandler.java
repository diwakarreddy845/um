package com.capv.client.user.chat.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.StanzaExtensionFilter;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.disco.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.packet.MUCInitialPresence;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.packet.DataForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.capv.client.user.UserChatRoom;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.listener.CapvChatConnectionClosingListener;
import com.capv.client.user.chat.listener.CapvGroupUserSubscribeListener;
import com.capv.client.user.chat.listener.CapvRoomInvitationListener;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.util.ChatUserUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
/**
 * <h1>Handler class for group management</h1>
 * 
 * This class used to handle user group management requests
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvGroupChatManagementHandler {
	
	private String userName;
	
	private CapvChatConnectionClosingListener capvChatConnectionClosingListener;
	
	private static final Logger log = LoggerFactory.getLogger(CapvGroupChatManagementHandler.class);
	/**
	 * this parameterized constructor is used to initialize the CapvGroupChatManagementHandler to handle user group management requests
	 * @param capvChatUserRequestProcessor	The reference of CapvChatUserRequestProcessor
	 * 										@see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 * 
	 */
	public CapvGroupChatManagementHandler(String userName) {
		
		this.userName = userName;
		
		initializeListeners();
	}
	
	/**  
	 * This method is used to initialize room invitation listener to listen users group requests and send them to respective user
	 */
	@SuppressWarnings("unchecked")
	private void initializeListeners() {
		
		StanzaListener groupUserSubscribeListener = new CapvGroupUserSubscribeListener(userName);
		
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
			CapvRoomInvitationListener roomInvitationListener = new CapvRoomInvitationListener(this.userName);
			
			MultiUserChatManager.getInstanceFor(chatUserConnection)
									.addInvitationListener(roomInvitationListener);
			
			chatUserConnection.addAsyncStanzaListener(groupUserSubscribeListener, 
										new StanzaExtensionFilter(MUCInitialPresence.NAMESPACE + "#user"));
			
			capvChatConnectionClosingListener = (XMPPTCPConnection chatConnection) -> {
														MultiUserChatManager.getInstanceFor(chatConnection)
																				.removeInvitationListener(roomInvitationListener);
														chatUserConnection.removeAsyncStanzaListener(groupUserSubscribeListener);
														
														try {
															final Field mucm_INSTANCES = MultiUserChatManager.class.getDeclaredField("INSTANCES");
															mucm_INSTANCES.setAccessible(true);
															
															final Map<XMPPConnection, MultiUserChatManager> mucm_INSTANCES_Map = 
																					(Map<XMPPConnection, MultiUserChatManager>) mucm_INSTANCES.get(null);
															mucm_INSTANCES_Map.remove(chatConnection);
															
														} catch(Exception e){
															log.error("Exception occurs while initializing Listeners :",e);
														}
													};
			
			CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName)
											.registerChatConnectionClosingListener(
														capvChatConnectionClosingListener);
		}
	}
	
	/**
	 *  @param room		The room name
	 *  @param friends	The friends list needs to be add into the room
	 *  
	 *  @throws NoResponseException if unable to receive response from the server
	 *  @throws XMPPErrorException	if server throws exception during the process of the request
	 *  @throws SmackException		if server throws exception during the process of the request
	 */
	public boolean createRoom(String roomname,JsonArray friendsList) throws NoResponseException, XMPPErrorException, SmackException{
		 
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	 MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(chatUserConnection);  
	    	 
	         MultiUserChat muc = multiUserChatManager.getMultiUserChat(roomname+"@conference."+chatUserConnection.getServiceName());  
	         
	         //multiUserChatManager.addInvitationListener(testingInvitationListener);
	         try {  
	        	 muc.create(roomname+"@conference."+chatUserConnection.getServiceName());  
	             muc.sendConfigurationForm(new Form(DataForm.Type.submit)); 
	             Form form = muc.getConfigurationForm().createAnswerForm();
	             form.setAnswer("muc#roomconfig_publicroom", true);
	             form.setAnswer("muc#roomconfig_roomname", roomname);
	             form.setAnswer("muc#roomconfig_persistentroom", true);
	             muc.sendConfigurationForm(form);
	             muc.join(chatUserConnection.getConfiguration().getUsername().toString());
	             log.debug("Creating room with friendsList   :  {}",friendsList);
	             for(JsonElement friend : friendsList){
	            	 muc.invite(friend.getAsString(), "hi "+friend.getAsString()+" welcome to my chatroom: "+roomname);
	            	 muc.grantMembership(friend.getAsString());
	             }
	             return true;  
	         }  
	         catch(NoResponseException ex) { 
	        	 log.error("NoResponseException occured while creating room  :",ex);
	             //ex.printStackTrace();  
	         }  
	         catch(XMPPErrorException ex) {  
	        	 log.error("XMPPErrorException occured while creating room  :",ex);
	             //ex.printStackTrace();  
	         }  
	         catch(NotConnectedException ex) {  
	        	 log.error("NotConnectedException occured while creating room  :",ex);
	            // ex.printStackTrace();  
	         }  
	         catch(SmackException ex) {  
	        	 log.error("SmackException occured while creating room  :",ex);
	            // ex.printStackTrace();  
	         } 
		}
        return false;  
    }
    
	/**
	 *  @param room		The room name
	 *  @param friends	The friends list needs to be add into the room
	 *  
	 *  @throws NoResponseException if unable to receive response from the server
	 *  @throws XMPPErrorException	if server throws exception during the process of the request
	 *  @throws SmackException		if server throws exception during the process of the request
	 */
	public boolean editRoom(String roomname,String friend) throws NoResponseException, XMPPErrorException, SmackException{
		 
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	 MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(chatUserConnection);  
	    	 
	         MultiUserChat muc = multiUserChatManager.getMultiUserChat(roomname+"@conference."+chatUserConnection.getServiceName());  
	         
	         //multiUserChatManager.addInvitationListener(testingInvitationListener);
	         try {  
	            // log.debug("Creating room with friendsList   :  {}",friendsList);
	             //for(JsonElement friend : friendsList){
	            	 muc.invite(friend, "hi "+friend+" welcome to my chatroom: "+roomname);
	            	 
	            	 List<Affiliate> bannedList = muc.getOutcasts();
	            	 for(Affiliate banUser :bannedList) {
		     				String banUsertemp = banUser.getJid().substring(0, banUser.getJid().indexOf("@"));
		     				if(banUsertemp.equals(friend)) {
		     					muc.revokeMembership(banUser.getJid());
		     				}else {
		     					muc.grantMembership(friend);
		     				}
		     			}
	            	 // }
	             return true;  
	         }  
	         catch(NoResponseException ex) { 
	        	 log.error("NoResponseException occured while creating room  :",ex);
	             //ex.printStackTrace();  
	         }  
	         catch(XMPPErrorException ex) {  
	        	 log.error("XMPPErrorException occured while creating room  :",ex);
	             //ex.printStackTrace();  
	         }  
	         catch(NotConnectedException ex) {  
	        	 log.error("NotConnectedException occured while creating room  :",ex);
	            // ex.printStackTrace();  
	         }  
	         catch(SmackException ex) {  
	        	 log.error("SmackException occured while creating room  :",ex);
	            // ex.printStackTrace();  
	         } 
		}
        return false;  
    }
	
	/**
	 *  @param room		The room name
	 *  @param friends	The friends list needs to be removed into the room
	 *  
	 *  @throws NoResponseException if unable to receive response from the server
	 *  @throws XMPPErrorException	if server throws exception during the process of the request
	 *  @throws SmackException		if server throws exception during the process of the request
	 */
	public boolean deleteMemberFromRoom(String roomname,String friend) throws NoResponseException, XMPPErrorException, SmackException{
		 
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	 MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(chatUserConnection);  
	    	 
	         MultiUserChat muc = multiUserChatManager.getMultiUserChat(roomname+"@conference."+chatUserConnection.getServiceName());  
	         
	         try {  
	            	 muc.banUser(friend, "removed From Room");
	             return true;  
	         }  
	         catch(NoResponseException ex) { 
	        	 log.error("NoResponseException occured while creating room  :",ex);
	             //ex.printStackTrace();  
	         }  
	         catch(XMPPErrorException ex) {  
	        	 log.error("XMPPErrorException occured while creating room  :",ex);
	             //ex.printStackTrace();  
	         }  
	         catch(NotConnectedException ex) {  
	        	 log.error("NotConnectedException occured while creating room  :",ex);
	            // ex.printStackTrace();  
	         }  
	         catch(SmackException ex) {  
	        	 log.error("SmackException occured while creating room  :",ex);
	            // ex.printStackTrace();  
	         } 
		}
        return false;  
    }
	
	/**
	 *  @param room		The room name
	 *  @param transferOwnerName	
	 *  @param userName	
	 *  @throws NoResponseException if unable to receive response from the server
	 *  @throws XMPPErrorException	if server throws exception during the process of the request
	 *  @throws SmackException		if server throws exception during the process of the request
	 */
	public boolean transferPrivilageMemberFromRoom(String roomname,String transferOwnerName,String username) throws NoResponseException, XMPPErrorException, SmackException{
		 
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	 MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(chatUserConnection);  
	    	 
	         MultiUserChat muc = multiUserChatManager.getMultiUserChat(roomname+"@conference."+chatUserConnection.getServiceName());  
	         
	         try {  
	            	 muc.grantOwnership(transferOwnerName);
	            	 muc.revokeOwnership(username);
	            	 muc.revokeAdmin(username);
	             return true;  
	         }  
	         catch(NoResponseException ex) { 
	        	 log.error("NoResponseException occured while creating room  :",ex);
	             //ex.printStackTrace();  
	         }  
	         catch(XMPPErrorException ex) {  
	        	 log.error("XMPPErrorException occured while creating room  :",ex);
	             //ex.printStackTrace();  
	         }  
	         catch(NotConnectedException ex) {  
	        	 log.error("NotConnectedException occured while creating room  :",ex);
	            // ex.printStackTrace();  
	         }  
	         catch(SmackException ex) {  
	        	 log.error("SmackException occured while creating room  :",ex);
	            // ex.printStackTrace();  
	         } 
		}
        return false;  
    }
	
	/**
	 * This method used to leave the user from room list
	 * 
	 * @param roomsList list of rooms user want to be leave
	 * 
	 * @return the user leave status from the room 
	 * 
	 * @throws NoResponseException		if unable to receive response from the server
	 * @throws XMPPErrorException		if server throws exception during the process of the request
	 * @throws SmackException			if server throws exception during the process of the request
	 * 
	 */
    public boolean leaveRoom(JsonArray roomsList) throws NoResponseException, XMPPErrorException, SmackException {
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(chatUserConnection); 
	    	
			for(JsonElement room: roomsList){
				 MultiUserChat muc = multiUserChatManager.getMultiUserChat(room.getAsString()+"@conference."+chatUserConnection.getServiceName());//+this.service);  
			     
			     try {  
			     		muc.leave();
			     		
			     		List<Affiliate> owners = null;
			     		boolean isUserOwnerOfTheRoom = false;
			     		try {
			     			owners = muc.getOwners();	
			     		} catch (Exception e){}
			     		
			     		if(owners != null && owners.size() > 0) {
			     			for(Affiliate owener :owners) {
			     				String ownerName = owener.getJid().substring(0, owener.getJid().indexOf("@"));
			     				if(ownerName.equals(userName)) {
			     					isUserOwnerOfTheRoom = true;
			     					break;
			     				}
			     			}
			     		}
			     		
			     		if(isUserOwnerOfTheRoom){
			     			muc.destroy("mywish",room.getAsString()+"@conference."+chatUserConnection.getServiceName());
			     			return true;
			     		}
			         }
			        
			     
			     catch(Exception ex) { 
			    	 log.error("Exception occured while leaving room   :",ex);
			         //ex.printStackTrace();  
			     }  
			}
		}
		
        return false;  
    }
    
    /**
	 * This method used to send group invitation 
	 * 
	 * @throws NoResponseException		if unable to receive response from the server
	 * @throws XMPPErrorException		if server throws exception during process of the request
	 * @throws NotConnectedException	if the connection is not established or broken with the server
     */
    public void sendInvitation() throws NoResponseException, XMPPErrorException, NotConnectedException{
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(chatUserConnection);
	    	MultiUserChat muc2 = manager.getMultiUserChat("cap@conference."+chatUserConnection.getServiceName());
	    	muc2.join("narendra@conference."+chatUserConnection.getServiceName());
	    	muc2.invite("rajesh"+chatUserConnection.getServiceName(), "hi surya");
		}
    }

    /**
     * This method is used to join into a room
     * 
     * @param room The room name
     * 
     * @throws XMPPErrorException		if server throws exception during process of the request
	 * @throws NotConnectedException	if the connection is not established or broken with the server
     */
    public void joinRoom(String room) throws XMPPErrorException, SmackException{
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	if(room != null && room.indexOf("@conference."+chatUserConnection.getServiceName()) < 0)
	    		room = room + "@conference." + chatUserConnection.getServiceName();
	    	MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(chatUserConnection);
	
	    	MultiUserChat muc2 = manager.getMultiUserChat(room);
	
	    	String userName = chatUserConnection.getConfiguration().getUsername().toString();
	    	muc2.join(userName);
	    	
	    	log.debug("joining in room with  userName :{}      joined in room  :{}",userName,room);
		}
    }
    
    /**
     * This method used to get all room list hosted in the application
	 * 
	 * @return All the hosted rooms
     *  
     * @throws NoResponseException		if unable to receive response from the server
	 * @throws XMPPErrorException		if server throws exception during process of the request
	 * @throws NotConnectedException	if the connection is not established or broken with the server
     */
    public List<HostedRoom> hostedRooms() throws NoResponseException, XMPPErrorException, NotConnectedException{
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(chatUserConnection);
	    	List<HostedRoom> hRooms = manager.getHostedRooms("conference."+chatUserConnection.getServiceName());
			return hRooms;
		}
		
		return null;
    	
    }
    
    /** 
     * This method used to get the room list of user joined
	 * 
	 * @return the roomsList of user Joined
     * 
     * @throws NoResponseException		if unable to receive response from the server
	 * @throws XMPPErrorException		if server throws exception during process of the request
	 * @throws NotConnectedException	if the connection is not established or broken with the server
     */
    public List<UserChatRoom> joinedRooms(Long clientId) throws NoResponseException, XMPPErrorException, NotConnectedException {
    	
    	List<UserChatRoom> userChatRooms = new ArrayList<>();
    	Integer propertyValue = Integer.parseInt(CapvClientUserUtil.getClientConfigProperty(clientId, "capv.video.groupsize"));
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(chatUserConnection);
	    	
	    	List<HostedRoom> hRooms = manager.getHostedRooms("conference."+chatUserConnection.getServiceName());
	    	
	    	if(hRooms != null && hRooms.size() > 0) {
	    		
	    		for(HostedRoom hostedRoom : hRooms){
		    		
		    		UserChatRoom userChatRoom = null;
		    		
			        List<DiscoverItems.Item> occupants = getOccupantsByRoom(hostedRoom.getJid());
			        
			        Map<String, String> userChatRoomOccupants = new HashMap<>();
			        
			        for(DiscoverItems.Item ocp : occupants) {
			        	
			        	String occupantName = ocp.getEntityID();
			        	
			        	if(!occupantName.toString().split("/")[1].equals(userName)) {
			        		
			        		int index = 0;
			        		if(occupantName.lastIndexOf("/") >= 0)
			        			index = occupantName.lastIndexOf("/") + 1;
			        		
			        		occupantName = occupantName.substring(index, occupantName.length());
			        		
		        			userChatRoomOccupants.put(occupantName, ChatUserUtil.getUserFullName(occupantName));
			        		
			        		
			        	} else {
			        		
			        		userChatRoom = new UserChatRoom();
			        		userChatRoom.setName(hostedRoom.getName().toLowerCase());
			        		userChatRoom.setJid(hostedRoom.getJid());
			        	}
			        }
			        if(userChatRoom != null) {
			        	
			        	MultiUserChat muc = manager.getMultiUserChat(hostedRoom.getJid());
			        	List<Affiliate> members = null;
			        	Map<String, String> pendingOccupants = new HashMap<>();
			        	try {
			                members = muc.getMembers();
			                userChatRoom.setAdmin(true);
			               } catch (Exception e){
			                userChatRoom.setAdmin(false);
			               }
				        if(members != null) {
				        	
				        	for(Affiliate member: members) {
				        		
				        		String memberJid = member.getJid();
				        		String memberName = memberJid.substring(0, memberJid.indexOf("@"));
				        		
				        		if(userChatRoomOccupants.get(memberName) == null) {
			        				pendingOccupants.put(memberName, ChatUserUtil.getUserFullName(memberName));
				        		}
				        	}
				        		
				        }
			        	userChatRoom.setOccupants(userChatRoomOccupants);
			        	userChatRoom.setPendingOccupants(pendingOccupants);
			        	userChatRoom.setOccupantsLength(userChatRoomOccupants.size());
			        	userChatRoom.setProgress(false);
			        	if(userChatRoomOccupants.size()>propertyValue) {
			        		userChatRoom.setVideoEnable(false);
			        	}else {
			        		userChatRoom.setVideoEnable(true);
			        	}
			        	userChatRooms.add(userChatRoom);
			        }
		    	}
	    	}
	    	log.debug("User Chat Rooms  :{}",userChatRooms);
		}
    	return userChatRooms;
    	
    }
    
    public UserChatRoom getUserChatRoomDetails(String roomJid) throws Exception {
    	
    	UserChatRoom userChatRoom = null;
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
    	MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(chatUserConnection);
    	
    	MultiUserChat muc = multiUserChatManager.getMultiUserChat(roomJid);
    	
    	if(muc != null) {
    		Map<String, String> userChatRoomOccupants = new HashMap<>();
    		
    		List<DiscoverItems.Item> occupants = getOccupantsByRoom(roomJid);
    		 
			userChatRoom = new UserChatRoom();
			userChatRoom.setName(roomJid.substring(0, roomJid.indexOf("@")).toLowerCase());
			userChatRoom.setJid(muc.getRoom());
		        
			for(DiscoverItems.Item ocp : occupants) {
				
				String occupantName = ocp.getEntityID();
				
				if(!occupantName.toString().split("/")[1].equals(userName)) {
					
					int index = 0;
					if(occupantName.lastIndexOf("/") >= 0)
						index = occupantName.lastIndexOf("/") + 1;
					
					occupantName = occupantName.substring(index, occupantName.length());
					
					userChatRoomOccupants.put(occupantName, ChatUserUtil.getUserFullName(occupantName));
					
				}
	        }
			
    		List<Affiliate> members = null;
        	Map<String, String> pendingOccupants = new HashMap<>();
        	
        	try {
                members = muc.getMembers();
                userChatRoom.setAdmin(true);
               } catch (Exception e){
                e.printStackTrace();
                userChatRoom.setAdmin(false);
               }
	        
	        if(members != null) {
	        	
	        	for(Affiliate member: members) {
	        		
	        		String memberJid = member.getJid();
	        		String memberName = memberJid.substring(0, memberJid.indexOf("@"));
	        		
	        		if(userChatRoomOccupants.get(memberName) == null) {
        				pendingOccupants.put(memberName, ChatUserUtil.getUserFullName(memberName));
	        		}
	        	}
	        		
	        }
        	userChatRoom.setOccupants(userChatRoomOccupants);
        	userChatRoom.setPendingOccupants(pendingOccupants);
        	userChatRoom.setOccupantsLength(userChatRoomOccupants.size());
    	}
    	
    	return userChatRoom;
    }
    
    /**
     * This method used to get the occupants of rooms
	 * 
	 * @return the occupants list of rooms
     *
     * @throws NoResponseException		if unable to receive response from the server
	 * @throws XMPPErrorException		if server throws exception during process of the request
	 * @throws NotConnectedException	if the connection is not established or broken with the server
     */
    public List<String> getOccupants() throws NoResponseException, XMPPErrorException, NotConnectedException {
    	
    	
    	List<String> jrooms = new ArrayList<String>();
    	
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
			MultiUserChatManager manager = MultiUserChatManager.getInstanceFor(chatUserConnection);
	    	List<HostedRoom> hRooms = manager.getHostedRooms("conference."+chatUserConnection.getServiceName());
	    	for(HostedRoom room : hRooms){
	    		
	    		ServiceDiscoveryManager discoManager =  ServiceDiscoveryManager.getInstanceFor(chatUserConnection);
		    	 DiscoverItems discoItems = discoManager.discoverItems(room.getJid());
		         List<DiscoverItems.Item> occupants = discoItems.getItems();  
		         for(DiscoverItems.Item ocp : occupants){
		        	 String userName = chatUserConnection.getConfiguration().getUsername().toString();
		        	 if(ocp.getEntityID().contains(userName)){
		        		 jrooms.add(room.getJid());
		        	 }
		         }
	    	}
	    	
	    	log.debug("jrooms  :{}",jrooms);
		}
    	
    	return jrooms;
    }
    public List<Item> getOccupantsByRoom(String room) throws NoResponseException, XMPPErrorException, NotConnectedException {
    	List<DiscoverItems.Item> occupants = null;
    	XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		if(chatUserConnection != null) {
	    	ServiceDiscoveryManager discoManager =  ServiceDiscoveryManager.getInstanceFor(chatUserConnection);
	   	 	DiscoverItems discoItems = discoManager.discoverItems(room);
	   	 	occupants = discoItems.getItems(); 
		}
   	 	return occupants;
    }
    /**
     *  This method is used to get ADMIN userName based on room name.
     *  @param room - it specifies the room name.
     *  @return userName of ADMIN.
     */
    public String getAdminDetails(String room) 
    {
      XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
  
      String adminUserName = ""; 
      
      if(chatUserConnection != null) {
        MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(chatUserConnection); 
       
        MultiUserChat muc = multiUserChatManager.getMultiUserChat(room+"@conference."+chatUserConnection.getServiceName());

        try {  
        
          List<Affiliate> owners = muc.getOwners(); 
        
            if(owners != null && owners.size() > 0) {
             for(Affiliate owener :owners) {
               String ownerName = owener.getJid().substring(0, owener.getJid().indexOf("@"));
               adminUserName = ownerName;
               break;
              }
             }
         }catch(Exception ex) { 
           log.error("Exception occured while leaving room   :",ex);
            ex.printStackTrace();  
         }  
       }
      
      return adminUserName;
    }

}

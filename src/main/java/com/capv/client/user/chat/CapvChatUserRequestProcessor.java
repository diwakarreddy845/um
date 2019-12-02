package com.capv.client.user.chat;

import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.disco.packet.DiscoverItems.Item;
import org.jivesoftware.smackx.muc.HostedRoom;

import com.capv.client.user.User;
import com.capv.client.user.UserChatRoom;
import com.capv.client.user.chat.handler.CapvChatUserManagementHandler;
import com.capv.client.user.chat.handler.CapvFileTransferHandler;
import com.capv.client.user.chat.handler.CapvGroupChatHandler;
import com.capv.client.user.chat.handler.CapvGroupChatManagementHandler;
import com.capv.client.user.chat.handler.CapvUserChatHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * <h1> Request processor for user requests with chat server </h1>
 * 
 * This class is used to process the user friend requests, presence changes, group management 
 * and chat messages with their respective handlers
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class CapvChatUserRequestProcessor {
	
	private String userName;
	
	private CapvUserChatHandler capvUserChatHandler;
	private CapvChatUserManagementHandler capvChatUserManagementHandler;
	private CapvGroupChatHandler capvGroupChatHandler;
	private CapvGroupChatManagementHandler capvGroupChatManagementHandler;
	private CapvFileTransferHandler capvFileTransferHandler;
	
	 /**
		 * This parameterized constructor is used to initialize the CapvChatUserRequestProcessor
		 * @param chatUserConnection			Reference of the chat service connection
		 * @param capvChatClientConfiguration	Reference of chat server configuration with user and server details
		 * 										@see com.capv.client.user.chat.CapvChatClientConfiguration
		 */
	public CapvChatUserRequestProcessor(String userName) {
		this.userName = userName;
		initializeCapvChatUserHandlers();
	}
	/**
	 * This method is used to initialize the handlers to process chat user friend requests, presence changes, group management and chat messages
	 * 
	 */
	private void initializeCapvChatUserHandlers() {
		
		capvUserChatHandler = new CapvUserChatHandler(userName);
		capvChatUserManagementHandler = new CapvChatUserManagementHandler(userName);
		capvGroupChatHandler = new CapvGroupChatHandler(userName);
		capvGroupChatManagementHandler = new CapvGroupChatManagementHandler(userName);
		capvFileTransferHandler = new CapvFileTransferHandler(userName);
		
	}
	
	public CapvUserChatHandler getCapvUserChatHandler() {
		return this.capvUserChatHandler;
	}
	
	/**
	 * This method is used to send chat message to receiver
	 * 
	 * @param message	chat message need to be send to receiver
	 * @param receiver	receiver of the message
	 * 
	 * @throws Exception if it is unable to deliver the message
	 * 
	 */
	public void sendMessage(String message, String receiver) throws Exception {
		capvUserChatHandler.sendMessage(message, receiver);
	}
	
	/**
	 * This method is used to remove friends from the user Roster
	 * 
	 * @param friendsList list friends needs to be delete from the Roster
	 * 
	 * @throws Exception if unable process the request
	 */
	public void removeFriends(JsonArray friendsList) throws Exception {
		capvChatUserManagementHandler.removeFriends(friendsList);
	}
	    
	/**
	 * This method is used to add friend to the user Roster
	 * @param name name of the friend
	 *
	 * @throws Exception if unable process the request
	 */    
	public void addBuddy(String name, String fullName) throws Exception {
		capvChatUserManagementHandler.addBuddy(name, fullName);
	}
	 
	/**
	 * This method is used to get all the user friends and friend requests
	 * 
	 * @return list of user friends and friend requests
	 */
	public List<User> getRosterUsers() {
		return capvChatUserManagementHandler.getRosterUsers();
	}
	
	/**
	 * This method is used to get the user friends
	 * 
	 * @return list of user friends
	 */
	public List<User> getUsers() {
		return capvChatUserManagementHandler.getUsers();
	}
	
	/**
	 * This method is used to get all the registered users in application
	 * 
	 * @return list of registered users in application
	 * 
	 * @throws Exception if unable process the request
	 */
	public List<User> getAllUsers() throws Exception {
		return capvChatUserManagementHandler.getAllUsers();
	}

	/**
	 * This method is used to search registered users in the application
	 * 
	 * @param user search string for user search
	 * 
	 * @return list of users that are matched with search string
	 * @throws Exception if unable process the request
	 */
	public List<User> searchUsers(String user) throws Exception {
		return capvChatUserManagementHandler.searchUsers(user);
		
	}
	    
	/**
	 * This method is used accept the friend request send by user
	 * 
	 * @param name of your friend
	 *  
	 * @throws Exception if unable process the request
	 */
	public void acceptFreindRequest(String name) throws Exception {
		capvChatUserManagementHandler.acceptFreindRequest(name);
	}
		
	/**
	 * This method used to decline the friend request send by user
	 * 
	 * @param name of your friend
	 * 
	 * @throws Exception if unable process the request
	 */
	public void declineFreindRequest(String name) throws Exception {
		capvChatUserManagementHandler.declineFreindRequest(name);
	}
	
	/**
	 * This method used to change the user presence and status
	 * 
	 * @param presence	presence mode
	 * @param status	presence status message
	 * 
	 * @throws Exception if unable process the request
	 */
	public void changeUserPresence(String presence, String status) throws Exception {
		capvChatUserManagementHandler.chageUserPresence(presence, status);
	}
	
	/**
	 * This method used to get the use presence mode
	 * 
	 * @param userName The user name
	 */
	public void getUserPresence(String userName) {
		capvChatUserManagementHandler.getUserPresence(userName);
	}
	
	public void uploadUserProfilePicture(String profilePictureData) throws Exception {
		capvChatUserManagementHandler.uploadUserProfilePicture(profilePictureData);
	}
	
	public Map<String, String> getUserProfilePicture(String userName) throws Exception {
		return capvChatUserManagementHandler.getUserProfilePicture(userName);
	}
	
	/**
	 *  This method used to send group message to group 
	 *  @param room		The room name
	 *  @param message	The message
	 *  
	 *  @throws Exception if unable process the request
	 */
	public void sendGroupMessage(String room, String message) throws Exception {
		capvGroupChatHandler.sendGroupMessage(room, message);
    }
	
	/**
	 *  @param roomname		The room name
	 *  @param friendsList	The friends list needs to be add into the room
	 *  
	 *  @return returns status of the room creation
	 *  @throws Exception if unable process the request
	 */
	public boolean createRoom(String roomname,JsonArray friendsList) throws Exception {
		 return capvGroupChatManagementHandler.createRoom(roomname, friendsList);
	}
	/**
	 *  @param roomname		The room name
	 *  @param friendsList	The friends list needs to be edit into the room
	 *  
	 *  @return returns status of the room creation
	 *  @throws Exception if unable process the request
	 */
	public boolean editRoom(String roomname,String friendsList) throws Exception {
		 return capvGroupChatManagementHandler.editRoom(roomname, friendsList);
	}
	/**
	 *  @param roomname		The room name
	 *  @param friendsList	The friends list needs to be deleted into the room
	 *  
	 *  @return returns status of the room creation
	 *  @throws Exception if unable process the request
	 */
	public boolean deleteMemberFromRoom(String roomname,String friend) throws NoResponseException, XMPPErrorException, SmackException{
		return capvGroupChatManagementHandler.deleteMemberFromRoom(roomname, friend);
	}
	/**
	 *  @param roomname		The room name
	 *  @param friendsList	The friends list needs to be deleted into the room
	 *  
	 *  @return returns status of the room creation
	 *  @throws Exception if unable process the request
	 */
	public boolean transferPrivilageMemberFromRoom(String roomname,String transferOwnerName,String userName) throws NoResponseException, XMPPErrorException, SmackException{
		return capvGroupChatManagementHandler.transferPrivilageMemberFromRoom( roomname, transferOwnerName, userName);
	}
	/**
	 * This method used to leave the user from room list
	 * 
	 * @param roomsList list of rooms user want to be leave
	 * 
	 * @return the user leave status from the room 
	 * 
	 * @throws Exception if unable to process the request
	 */
	public boolean leaveRoom(JsonArray roomsList) throws Exception {
		return capvGroupChatManagementHandler.leaveRoom(roomsList);
	}
   
	/**
	 * This method used to send group invitation 
	 * 
	 * @throws Exception if unable process the request
	 */
	public void sendInvitation() throws Exception {	
		capvGroupChatManagementHandler.sendInvitation();
	}
	
    /**
     * This method is used to join into a room
     * 
     * @param room The room name
     * 
     * @throws Exception if unable process the request
     */
	public void joinRoom(String room) throws Exception {
		capvGroupChatManagementHandler.joinRoom(room);
	}
   
	public List<HostedRoom> hostedRooms() throws Exception {
		return capvGroupChatManagementHandler.hostedRooms();
	}
   
	/**
	 * This method used to get the room list of user joined
	 * 
	 * @return the roomsList of user Joined
	 * 
	 * @throws Exception if unable process the request
	 */
	public List<UserChatRoom> joinedRooms(Long clientId) throws Exception {
		return capvGroupChatManagementHandler.joinedRooms(clientId); 
	}
	
	public UserChatRoom getUserChatRoomDetails(String roomJid) throws Exception {
		return capvGroupChatManagementHandler.getUserChatRoomDetails(roomJid);
	}
   
	public List<Item> getOccupantsByRoom(String room) throws Exception {
		return capvGroupChatManagementHandler.getOccupantsByRoom(room); 
	}
	
	/**
	 * This method used to get the occupants of rooms
	 * 
	 * @return the occupants list of rooms
	 * 
	 * @throws Exception if unable process the request
	 */
	public List<String> getOccupants() throws Exception {
		return capvGroupChatManagementHandler.getOccupants();
	}
	
	/**
	 *  This method used to share file to the user friends
	 *  
	 *  @param jsonMessageObj The json message object which contains the information about file and file receivers
	 *  
	 *  @throws Exception if unable process the request
	 */
	public void sendFile(JsonObject jsonMessageObj) throws Exception {
		capvFileTransferHandler.sendFile(jsonMessageObj);
	}
	
	/**
     *  This method is used to get ADMIN userName based on room name.
     *  @param room - it specifies the room name.
     *  @return userName of ADMIN.
     */
	public String getAdminDetails(String room){
		return capvGroupChatManagementHandler.getAdminDetails(room);
	 }
	
}

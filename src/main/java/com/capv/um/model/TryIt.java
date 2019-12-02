package com.capv.um.model;

public class TryIt {

	private String email;
	private String userName;
	private String inviteURL;
	private String messageToSend;
	private String id;
	private String room;
	private String senderName;

	public String getSenderName() {
		return senderName;
	}

	public void setSenderName(String senderName) {
		this.senderName = senderName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	public String getMessageToSend() {
		return messageToSend;
	}

	public void setMessageToSend(String messageToSend) {
		this.messageToSend = messageToSend;
	}

	public String getInviteURL() {
		return inviteURL;
	}

	public void setInviteURL(String inviteURL) {
		this.inviteURL = inviteURL;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Override
	public String toString() {
		return "TryIt [email=" + email + ", userName=" + userName + "]";
	}

}

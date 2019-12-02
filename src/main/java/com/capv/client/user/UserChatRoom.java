package com.capv.client.user;

import java.util.Map;

/**
 * <h1> Model class for user room info </h1>
 * This class is defined as a model class for user group details with the list of occupants in the group
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class UserChatRoom {

	private String name;
	private String jid;
	private int occupantsLength;
	private Map<String, String> occupants;
	private Map<String, String> pendingOccupants;
	private boolean isAdmin;
	private boolean isProgress;
	private boolean isVideoEnable;
	 
	 public boolean isVideoEnable() {
		return isVideoEnable;
	}

	public void setVideoEnable(boolean isVideoEnable) {
		this.isVideoEnable = isVideoEnable;
	}

	public boolean isProgress() {
		return isProgress;
	}

	public void setProgress(boolean isProgress) {
		this.isProgress = isProgress;
	}

	public boolean isAdmin() {
	  return isAdmin;
	 }

	 public void setAdmin(boolean isAdmin) {
	  this.isAdmin = isAdmin;
	 }
	
	public String getName() {
		return name;
	}
	
	public void setName(String room) {
		this.name = room;
	}
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public Map<String, String> getOccupants() {
		return occupants;
	}
	
	public void setOccupants(Map<String, String> occupants) {
		this.occupants = occupants;
	}
	
	public Map<String, String> getPendingOccupants() {
		return pendingOccupants;
	}

	public void setPendingOccupants(Map<String, String> pendingOccupants) {
		this.pendingOccupants = pendingOccupants;
	}

	public String toString() {
		return "Room:" + name + "::Room Occupants:" + occupants +"::Pending occupants:" +pendingOccupants;
	}

	public int getOccupantsLength() {
		return occupantsLength;
	}

	public void setOccupantsLength(int occupantsLength) {
		this.occupantsLength = occupantsLength;
	}
}

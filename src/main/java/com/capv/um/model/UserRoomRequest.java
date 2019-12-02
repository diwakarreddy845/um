package com.capv.um.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="user_room_request")
public class UserRoomRequest extends AbstractModel {

	@Column(name="room_name")
	private String roomName;
	
	@Column(name="inviter")
	private String inviter;
	
	@Column(name="invitee")
	private String invitee;
	
	@Column(name="is_pending")
	private int isPending;
	
	@Column(name="created_date",updatable=false)
	private Date createdDate;
	
	@Column(name="update_date")
	private Date updateDate;

	public String getRoomName() {
		return roomName;
	}

	public void setRoomName(String roomName) {
		this.roomName = roomName;
	}

	public String getInviter() {
		return inviter;
	}

	public void setInviter(String inviter) {
		this.inviter = inviter;
	}

	public String getInvitee() {
		return invitee;
	}

	public void setInvitee(String invitee) {
		this.invitee = invitee;
	}

	public int getIsPending() {
		return isPending;
	}

	public void setIsPending(int isPending) {
		this.isPending = isPending;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	
	
	
	
	

}

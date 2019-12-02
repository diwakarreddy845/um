package com.capv.um.chat.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name="ofMucRoom")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupsDetails  {
	
	@Id
	@Column(name = "serviceID")
    private Long serviceID;
	
	@Column(name = "roomID")
    private Long roomID;
	
	@Column(name = "creationDate")
    private String creationDate;
	
	@Column(name = "modificationDate")
    private String modificationDate;
	
	@Column(name = "name")
    private String name;
	
	@Column(name = "naturalName")
    private String naturalName;
	
	@Column(name = "description")
    private String description;
	
	@Column(name = "lockedDate")
    private String lockedDate;
	
	@Column(name = "emptyDate")
    private String emptyDate;
	
	@Column(name = "canChangeSubject")
    private Integer canChangeSubject;
	
	@Column(name = "maxUsers")
    private Integer maxUsers;
	
	@Column(name = "publicRoom")
    private Integer publicRoom;
	
	@Column(name = "moderated")
    private Integer moderated;
	
	@Column(name = "membersOnly")
    private Integer membersOnly;
	
	@Column(name = "canInvite")
    private Integer canInvite;
	
	@Column(name = "roomPassword")
    private String roomPassword;
	
	@Column(name = "canDiscoverJID")
    private Integer canDiscoverJID;

	@Column(name = "logEnabled")
    private Integer logEnabled;
	
	@Column(name = "subject")
    private String subject;
	
	@Column(name = "rolesToBroadcast")
    private Integer rolesToBroadcast;
	
	@Column(name = "useReservedNick")
    private Integer useReservedNick;
	
	@Column(name = "canChangeNick")
    private Integer canChangeNick;
	
	@Column(name = "canRegister")
    private Integer canRegister;

	public Long getServiceID() {
		return serviceID;
	}

	public void setServiceID(Long serviceID) {
		this.serviceID = serviceID;
	}

	public Long getRoomID() {
		return roomID;
	}

	public void setRoomID(Long roomID) {
		this.roomID = roomID;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public String getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(String modificationDate) {
		this.modificationDate = modificationDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNaturalName() {
		return naturalName;
	}

	public void setNaturalName(String naturalName) {
		this.naturalName = naturalName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLockedDate() {
		return lockedDate;
	}

	public void setLockedDate(String lockedDate) {
		this.lockedDate = lockedDate;
	}

	public String getEmptyDate() {
		return emptyDate;
	}

	public void setEmptyDate(String emptyDate) {
		this.emptyDate = emptyDate;
	}

	public Integer getCanChangeSubject() {
		return canChangeSubject;
	}

	public void setCanChangeSubject(Integer canChangeSubject) {
		this.canChangeSubject = canChangeSubject;
	}

	public Integer getMaxUsers() {
		return maxUsers;
	}

	public void setMaxUsers(Integer maxUsers) {
		this.maxUsers = maxUsers;
	}

	public Integer getPublicRoom() {
		return publicRoom;
	}

	public void setPublicRoom(Integer publicRoom) {
		this.publicRoom = publicRoom;
	}

	public Integer getModerated() {
		return moderated;
	}

	public void setModerated(Integer moderated) {
		this.moderated = moderated;
	}

	public Integer getMembersOnly() {
		return membersOnly;
	}

	public void setMembersOnly(Integer membersOnly) {
		this.membersOnly = membersOnly;
	}

	public Integer getCanInvite() {
		return canInvite;
	}

	public void setCanInvite(Integer canInvite) {
		this.canInvite = canInvite;
	}

	public String getRoomPassword() {
		return roomPassword;
	}

	public void setRoomPassword(String roomPassword) {
		this.roomPassword = roomPassword;
	}

	public Integer getCanDiscoverJID() {
		return canDiscoverJID;
	}

	public void setCanDiscoverJID(Integer canDiscoverJID) {
		this.canDiscoverJID = canDiscoverJID;
	}

	public Integer getLogEnabled() {
		return logEnabled;
	}

	public void setLogEnabled(Integer logEnabled) {
		this.logEnabled = logEnabled;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public Integer getRolesToBroadcast() {
		return rolesToBroadcast;
	}

	public void setRolesToBroadcast(Integer rolesToBroadcast) {
		this.rolesToBroadcast = rolesToBroadcast;
	}

	public Integer getUseReservedNick() {
		return useReservedNick;
	}

	public void setUseReservedNick(Integer useReservedNick) {
		this.useReservedNick = useReservedNick;
	}

	public Integer getCanChangeNick() {
		return canChangeNick;
	}

	public void setCanChangeNick(Integer canChangeNick) {
		this.canChangeNick = canChangeNick;
	}

	public Integer getCanRegister() {
		return canRegister;
	}

	public void setCanRegister(Integer canRegister) {
		this.canRegister = canRegister;
	}
}

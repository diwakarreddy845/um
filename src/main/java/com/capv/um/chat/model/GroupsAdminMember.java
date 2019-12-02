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
@Table(name="ofMucAffiliation")
@JsonIgnoreProperties(ignoreUnknown = true)
public class GroupsAdminMember  {
	


	@Id
	@Column(name = "roomID")
    private Long roomID;
	
	@Column(name = "jid")
    private String jid;
	
	@Column(name = "affiliation")
    private Integer nickname;

	public Long getRoomID() {
		return roomID;
	}

	public void setRoomID(Long roomID) {
		this.roomID = roomID;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public Integer getNickname() {
		return nickname;
	}

	public void setNickname(Integer nickname) {
		this.nickname = nickname;
	}
	
	
}

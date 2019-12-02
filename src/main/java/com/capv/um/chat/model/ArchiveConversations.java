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
@Table(name="archiveConversations")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArchiveConversations {
	
	@Id
	@Column(name = "conversationId")
    private Long conversationId;
	
	@Column(name = "startTime")
    private Long startTime;
	
	@Column(name = "endTime")
    private Long endTime;
	
	@Column(name = "ownerJid")
    private String ownerJid;
	
	@Column(name = "ownerResource")
    private String ownerResource;
	
	@Column(name = "withJid")
    private String withJid;
	
	@Column(name = "withResource")
    private String withResource;
	
	@Column(name = "subject")
    private String subject;
	
	@Column(name = "thread")
    private String thread;

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}

	public String getOwnerJid() {
		return ownerJid;
	}

	public void setOwnerJid(String ownerJid) {
		this.ownerJid = ownerJid;
	}

	public String getOwnerResource() {
		return ownerResource;
	}

	public void setOwnerResource(String ownerResource) {
		this.ownerResource = ownerResource;
	}

	public String getWithJid() {
		return withJid;
	}

	public void setWithJid(String withJid) {
		this.withJid = withJid;
	}

	public String getWithResource() {
		return withResource;
	}

	public void setWithResource(String withResource) {
		this.withResource = withResource;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getThread() {
		return thread;
	}

	public void setThread(String thread) {
		this.thread = thread;
	}

	
}

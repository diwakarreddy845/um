package com.capv.um.chat.model;

public class ArchiveMessagesConversation {
	
	
    private Long messageId;
	
    private Long time;
	
    private String direction;
	
    private String type;
	
    private String subject;
	
    private String body;
	
	private Long conversationId;
	
    private String ownerJid;
	
    private String withJid;

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Long getConversationId() {
		return conversationId;
	}

	public void setConversationId(Long conversationId) {
		this.conversationId = conversationId;
	}

	public String getOwnerJid() {
		return ownerJid;
	}

	public void setOwnerJid(String ownerJid) {
		this.ownerJid = ownerJid;
	}

	public String getWithJid() {
		return withJid;
	}

	public void setWithJid(String withJid) {
		this.withJid = withJid;
	}

	
}

package com.capv.um.model;

import java.sql.Timestamp;

public class ChatHistory {
	
	String from;
	String body;
	String to;
	String type;
	String messageId;
	String isEdited;
	String message_type;
	String reply_message_body;
	
	public String getReply_message_body() {
		return reply_message_body;
	}
	public void setReply_message_body(String reply_message_body) {
		this.reply_message_body = reply_message_body;
	}
	public String getMessage_type() {
		return message_type;
	}
	public void setMessage_type(String message_type) {
		this.message_type = message_type;
	}
	public String getIsEdited() {
		return isEdited;
	}
	public void setIsEdited(String isEdited) {
		this.isEdited = isEdited;
	}
	public void setSentTime(Long sentTime) {
		this.sentTime = sentTime;
	}
	public String getMessageId() {
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public Long getSentTime() {
		return sentTime;
	}
	public void setSentTime(long l) {
		this.sentTime = l;
	}
	Long sentTime;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

}

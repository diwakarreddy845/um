package com.capv.um.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "ofGroupArchive")
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfGroupArchive {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "conversationID")
	private Long conversationID;

	@Column(name = "fromJID")
	private String fromJID;

	@Column(name = "fromJIDResource")
	private String fromJIDResource;

	@Column(name = "toJID")
	private String toJID;

	@Column(name = "toJIDResource")
	private String toJIDResource;

	@Column(name = "sentDate")
	private Long sentDate;

	@Column(name = "stanza")
	private String stanza;

	@Column(name = "body")
	private String body;

	@Column(name = "isEdited")
	private Integer isEdited;

	@Column(name = "isDeleted")
	private Integer isDeleted;

	@Column(name = "message_type")
	private Byte message_type;

	@Column(name = "reply_message_body")
	private String reply_message_body;

	public Byte getMessage_type() {
		return message_type;
	}

	public void setMessage_type(Byte message_type) {
		this.message_type = message_type;
	}

	public String getReply_message_body() {
		return reply_message_body;
	}

	public void setReply_message_body(String reply_message_body) {
		this.reply_message_body = reply_message_body;
	}

	public Integer getIsEdited() {
		return isEdited;
	}

	public void setIsEdited(Integer isEdited) {
		this.isEdited = isEdited;
	}

	public Integer getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}

	public Long getConversationID() {
		return conversationID;
	}

	public void setConversationID(Long conversationID) {
		this.conversationID = conversationID;
	}

	public String getFromJID() {
		return fromJID;
	}

	public void setFromJID(String fromJID) {
		this.fromJID = fromJID;
	}

	public String getFromJIDResource() {
		return fromJIDResource;
	}

	public void setFromJIDResource(String fromJIDResource) {
		this.fromJIDResource = fromJIDResource;
	}

	public String getToJID() {
		return toJID;
	}

	public void setToJID(String toJID) {
		this.toJID = toJID;
	}

	public String getToJIDResource() {
		return toJIDResource;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setToJIDResource(String toJIDResource) {
		this.toJIDResource = toJIDResource;
	}

	public Long getSentDate() {
		return sentDate;
	}

	public void setSentDate(Long sentDate) {
		this.sentDate = sentDate;
	}

	public String getStanza() {
		return stanza;
	}

	public void setStanza(String stanza) {
		this.stanza = stanza;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Override
	public String toString() {
		return "OfMessageArchive [messageID=" + id + ", conversationID=" + conversationID + ", fromJID=" + fromJID + ", fromJIDResource="
				+ fromJIDResource + ", toJID=" + toJID + ", toJIDResource=" + toJIDResource + ", sentDate=" + sentDate + ", stanza=" + stanza
				+ ", body=" + body + "]";
	}
}

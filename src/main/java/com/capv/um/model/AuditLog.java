package com.capv.um.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * AuditLog Persistence object will store data in AuditLog table
 * 
 * @author caprusit
 * @version 1.0
 */
@Entity
@Table(name = "auditlog")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditLog {

	public AuditLog() {
	}

	public AuditLog(AuditLog log) {
		super();
		this.id = log.getId();
		this.controllername = log.getControllername();
		this.input = log.getInput();
		this.username = log.getUsername();
	}

	public AuditLog(String controllername, String input, String username, String room, Date lastupdate, String ip, String msg_type) {
		super();
		this.controllername = controllername;
		this.input = input;
		this.username = username;
		this.room = room;
		this.lastupdate = lastupdate;
		this.ip = ip;
		this.msg_type = msg_type;
	}
	
	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "controllername")
	private String controllername;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getControllername() {
		return controllername;
	}

	public void setControllername(String controllername) {
		this.controllername = controllername;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRoom() {
		return room;
	}

	public void setRoom(String room) {
		this.room = room;
	}

	@Column(name = "input")
	private String input;

	@Column(name = "username")
	private String username;

	@Column(name = "room")
	private String room;

	@Column(name = "lastupdate")
	private Date lastupdate;

	@Column(name = "created_timestamp", updatable = false)
	private Date createdTimestamp;

	@Column(name = "msg_type")
	private String msg_type;

	public String getMsg_type() {
		return msg_type;
	}

	public void setMsg_type(String msg_type) {
		this.msg_type = msg_type;
	}

	public Date getCreatedTimestamp() {
		return createdTimestamp;
	}

	public void setCreatedTimestamp(Date createdTimestamp) {
		this.createdTimestamp = createdTimestamp;
	}

	@Column(name = "ip")
	private String ip;

	public Date getLastupdate() {
		return lastupdate;
	}

	public void setLastupdate(Date lastupdate) {
		this.lastupdate = lastupdate;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	
}
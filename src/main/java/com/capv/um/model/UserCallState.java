package com.capv.um.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "user_call_state")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCallState {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "caller_name")
	private String callerName;

	@Column(name = "client_id")
	private Long clientId;

	@Column(name = "room_no")
	private String roomNo;

	@Column(name = "call_type")
	private String callType;

	@Column(name = "call_status")
	private Byte callStatus;

	@Column(name = "start_time")
	private Date startTime;

	@Column(name = "end_time")
	private Date endTime;

	@Column(name = "update_time")
	private Date updateTime;

	@Column(name = "callee_list")
	private String calleeList;

	@Column(name = "jid")
	private String jid;

	@Column(name = "call_mode")
	private Integer callMode;

	@Column(name = "delete_vid_flag")
	private Integer delete_vid_flag;

	@Column(name = "admin")
	private String admin;

	public Integer getDelete_vid_flag() {
		return delete_vid_flag;
	}

	public void setDelete_vid_flag(Integer delete_vid_flag) {
		this.delete_vid_flag = delete_vid_flag;
	}

	public String getCallerName() {
		return callerName;
	}

	public void setCallerName(String callerName) {
		this.callerName = callerName;
	}

	public Long getClientId() {
		return clientId;
	}

	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}

	public String getRoomNo() {
		return roomNo;
	}

	public void setRoomNo(String roomNo) {
		this.roomNo = roomNo;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public Byte getCallStatus() {
		return callStatus;
	}

	public void setCallStatus(Byte callStatus) {
		this.callStatus = callStatus;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getCalleeList() {
		return calleeList;
	}

	public void setCalleeList(String calleeList) {
		this.calleeList = calleeList;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public Integer getCallMode() {
		return callMode;
	}

	public void setCallMode(Integer callMode) {
		this.callMode = callMode;
	}

	public String getAdmin() {
		return admin;
	}

	public void setAdmin(String admin) {
		this.admin = admin;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}

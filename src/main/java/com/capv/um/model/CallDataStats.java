package com.capv.um.model;

import java.util.List;

/**
 *   <h1>CallDataStats</h1> .
 */
public class CallDataStats {
	private String clientId;
	private String caller;
	private List<String> calleeList;
	private String callType;
	private int totalCalls;
	private String totalCallDuration;
	private String startDate;
	private String endDate;
	private String inOROutCall;

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getCaller() {
		return caller;
	}

	public void setCaller(String caller) {
		this.caller = caller;
	}

	public List<String> getCalleeList() {
		return calleeList;
	}

	public void setCalleeList(List<String> calleeList) {
		this.calleeList = calleeList;
	}

	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

	public int getTotalCalls() {
		return totalCalls;
	}

	public void setTotalCalls(int totalCalls) {
		this.totalCalls = totalCalls;
	}

	public String getTotalCallDuration() {
		return totalCallDuration;
	}

	public void setTotalCallDuration(String totalCallDuration) {
		this.totalCallDuration = totalCallDuration;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getInOROutCall() {
		return inOROutCall;
	}

	public void setInOROutCall(String inOROutCall) {
		this.inOROutCall = inOROutCall;
	}

}

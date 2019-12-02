package com.capv.um.model;


public class SearchUserParams {

	
	
	private String userName;
	
	
	private String maxResults;
	
	
	private String lastFetchUserId;

	private String user;
	
	private String searchText;
	
	private String clientId;

	public String getClientId() {
		return clientId;
	}


	public void setClientId(String clientId) {
		this.clientId = clientId;
	}


	public String getSearchText() {
		return searchText;
	}


	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	public String getMaxResults() {
		return maxResults;
	}


	public void setMaxResults(String maxResults) {
		this.maxResults = maxResults;
	}


	public String getLastFetchUserId() {
		return lastFetchUserId;
	}


	public void setLastFetchUserId(String lastFetchUserId) {
		this.lastFetchUserId = lastFetchUserId;
	}
	


	
}

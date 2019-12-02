package com.capv.um.model;

import java.util.List;

/**
 *  <h1>UserDataDTO</h1> 
 *  This class is used to represent Jquery DataTables parameters which is returned to the capv_auth service 
 *  such as draw, recordsTotal, recordsFiltered and data of client.
 *  @author rahul.murlidhar
 *  @version 1.0
 */
public class UserDataDTO {

	private Integer draw;
	private Long recordsTotal;
	private Long recordsFiltered;
	List<User> data;

	public Integer getDraw() {
		return draw;
	}

	public void setDraw(Integer draw) {
		this.draw = draw;
	}

	public Long getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(Long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public Long getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(Long recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public List<User> getData() {
		return data;
	}

	public void setData(List<User> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "UserDataDTO [draw=" + draw + ", recordsTotal=" + recordsTotal + ", recordsFiltered=" + recordsFiltered
				+ ", data=" + data + "]";
	}

}
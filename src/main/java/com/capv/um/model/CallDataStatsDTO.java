package com.capv.um.model;

import java.util.List;

public class CallDataStatsDTO {
	private Integer draw;
	private Integer recordsTotal;
	private Integer recordsFiltered;
	List<CallDataStats> data;

	public Integer getDraw() {
		return draw;
	}

	public void setDraw(Integer draw) {
		this.draw = draw;
	}

	public Integer getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(Integer recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public Integer getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(Integer recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public List<CallDataStats> getData() {
		return data;
	}

	public void setData(List<CallDataStats> data) {
		this.data = data;
	}

}

package com.capv.um.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "time_zones")
@JsonIgnoreProperties(value = { "handler", "hibernateLazyInitializer" })
public class TimeZone  {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(name = "offset")
	private Long offset;

	@Column(name = "time_zone_name")
	private String time_zone_name;

	@Column(name = "offset_in_min")
	private String offset_in_min;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOffset_in_min() {
		return offset_in_min;
	}

	public void setOffset_in_min(String offset_in_min) {
		this.offset_in_min = offset_in_min;
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public String getTime_zone_name() {
		return time_zone_name;
	}

	public void setTime_zone_name(String time_zone_name) {
		this.time_zone_name = time_zone_name;
	}
}

package com.capv.um.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 
/**
 *  <h1> MeetingConfigProperty </h1>
 *   This class represents a model ConfigProperty contains the configuration property details related to Meeting such as id, name, and configType
 *   of meeting configuration properties.
 *   @author caprus it
 *   @version 1.0
 */
@Entity
@Table(name = "meeting_config_property")
@JsonIgnoreProperties(value = { "handler", "hibernateLazyInitializer" })
public class MeetingConfigProperty {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private int id;

	@Column(name = "name")
	private String name;

	@Column(name = "config_type")
	private String configType;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getConfigType() {
		return configType;
	}

	public void setConfigType(String configType) {
		this.configType = configType;
	}

	@Override
	public String toString() {
		return "ConfigProperty [id=" + id + ", name=" + name + ", configType=" + configType + "]";
	}

}

package com.capv.um.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <h1>ConfigProperty</h1> This class represents a model ConfigProperty contains the configuration property details related to client such
 * as id, name, and configType of User configuration properties.
 * 
 * @author caprusit
 * @version 1.0
 */
@Entity
@Table(name = "user_config_property")
@JsonIgnoreProperties(value = { "handler", "hibernateLazyInitializer" })
public class UserConfigProperty {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "name")
	private String name;

	@Column(name = "config_type")
	private String configType;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

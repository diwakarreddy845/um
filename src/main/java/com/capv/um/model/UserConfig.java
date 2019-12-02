package com.capv.um.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <h1>userConfig</h1> This class represents a model userConfig contains the configuration details related to client such as id, userId,
 * propValue, and ConfigProperty model details.
 * 
 * @author caprusit
 * @version 1.0
 */
@Entity
@Table(name = "user_config")
@JsonIgnoreProperties(value = { "handler", "hibernateLazyInitializer" })
public class UserConfig {

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id")
	private Long userId;

	@Column(name = "prop_value")
	private String propValue;

	@ManyToOne
	@JoinColumn(name = "config_property_id")
	private UserConfigProperty configProperty;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getPropValue() {
		return propValue;
	}

	public void setPropValue(String propValue) {
		this.propValue = propValue;
	}

	public UserConfigProperty getConfigProperty() {
		return configProperty;
	}

	public void setConfigProperty(UserConfigProperty configProperty) {
		this.configProperty = configProperty;
	}

	@Override
	public String toString() {
		return "ClientConfig [id=" + id + ", clientId=" + userId + ", propValue=" + propValue + ", configProperty=" + configProperty + "]";
	}
}

package com.capv.um.exception;

public class ClientConfigurationException extends RuntimeException {

	private static final long serialVersionUID = -7826257702047915407L;

	public ClientConfigurationException() {
		super();
	}
	
	public ClientConfigurationException(String message) {
		super(message);
	}
	
	public ClientConfigurationException(Throwable cause) {
		super(cause);
	}
	
	public ClientConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}

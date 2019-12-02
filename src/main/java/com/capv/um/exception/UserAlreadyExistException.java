package com.capv.um.exception;

public class UserAlreadyExistException extends RuntimeException {

	private static final long serialVersionUID = -7826257702047915407L;

	public UserAlreadyExistException() {
		super();
	}
	
	public UserAlreadyExistException(String message) {
		super(message);
	}
	
	public UserAlreadyExistException(Throwable cause) {
		super(cause);
	}
	
	public UserAlreadyExistException(String message, Throwable cause) {
		super(message, cause);
	}
}

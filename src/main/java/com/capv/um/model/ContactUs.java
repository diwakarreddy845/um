package com.capv.um.model;

public class ContactUs {

	private String email;

	private String message;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "ContactUs [email=" + email + ", message=" + message + "]";
	}

}

package com.capv.um.view;

public class ChangePasswordView {

	private String oldPassword;
	private String newPassword;
	private String confirmNewPassword;
	private String currentPassword;
	
	public String getOldPassword() {
		return oldPassword;
	}
	public void setOldPassword(String oldPassword) {
		this.oldPassword = oldPassword;
	}
	public String getNewPassword() {
		return newPassword;
	}
	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}
	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}
	public String getCurrentPassword() {
		return currentPassword;
	}
	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
	@Override
	public String toString() {
		return "ChangePasswordView [oldPassword=" + oldPassword + ", newPassword=" + newPassword
				+ ", confirmNewPassword=" + confirmNewPassword + ", currentPassword=" + currentPassword + "]";
	}
	
}

package com.capv.um.rest.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.capv.um.util.CapvUtil;
import com.capv.um.view.ChangePasswordView;

public class ChangePasswordValidator implements Validator {
	
    @Override
    public boolean supports(final Class<?> clazz) {
        return ChangePasswordView.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
    	
    	ChangePasswordView changePasswordView = (ChangePasswordView) target;
    	
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "oldPassword", "message.oldPassword",CapvUtil.environment.getProperty("message.oldPassword"));
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", "message.newPassword",CapvUtil.environment.getProperty("message.newPassword"));
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "confirmNewPassword", "message.confirmNewPassword",CapvUtil.environment.getProperty("message.confirmNewPassword"));
        
        if(!errors.hasFieldErrors() && !errors.hasErrors()) {
        	
        	String encryptedOldPassword = CapvUtil.encodePassword(changePasswordView.getOldPassword());
        	String encryptedNewPassword = CapvUtil.encodePassword(changePasswordView.getNewPassword());
        	
        	if(!encryptedOldPassword.equals(changePasswordView.getCurrentPassword())) {
        		errors.rejectValue("oldPassword", "message.oldPassword",CapvUtil.environment.getProperty("message.oldPasswordNotValid"));
            	return;	
            }
        	
        	if(!PasswordValidator.getInstance().isValid(changePasswordView.getNewPassword()))
        		errors.rejectValue("newPassword", "message.newPassword",CapvUtil.environment.getProperty("message.passwordNotValid"));
        	else if(encryptedNewPassword.equals(changePasswordView.getCurrentPassword()))
        		errors.rejectValue("newPassword", "message.newPassword",CapvUtil.environment.getProperty("message.newPasswordMatch"));
        	
        	if(!changePasswordView.getNewPassword().equals(changePasswordView.getConfirmNewPassword()))
        		errors.rejectValue("confirmNewPassword", "message.confirmNewPassword",CapvUtil.environment.getProperty("message.confirmNewPasswordNotMatch"));
        }
        	
    }

}

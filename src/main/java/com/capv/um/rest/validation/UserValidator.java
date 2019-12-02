package com.capv.um.rest.validation;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import com.capv.um.constants.CapvConstants.RegistrationSource;
import com.capv.um.model.User;
import com.capv.um.util.CapvUtil;

public class UserValidator implements Validator {
	
	private ValidationType validationType;
	
	public enum ValidationType {
		
		SAVEUSER("SaveUser"),
		UPDATEUSER("UpdateUser");
		
		private String validationType;
		
		public String getValidationType() {
			return validationType;
		}
		
		private ValidationType(String validationType) {
			this.validationType = validationType;
		}
		
	}
	
	public UserValidator(ValidationType validationType) {
		this.validationType = validationType;
	}
	
    @Override
    public boolean supports(final Class<?> clazz) {
        return User.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(final Object target, final Errors errors) {
    	
    	User user = (User) target;
    	
    	if(this.validationType.equals(ValidationType.SAVEUSER)) {
    		validateSaveUser(user, errors);
    	} else {
    		validateUpdateUser(user, errors);
    	}
        	
    }
    
    private void validateSaveUser(User user, Errors errors) {
    	
    	 ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "message.firstName",CapvUtil.environment.getProperty("message.firstName"));
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "message.lastName",CapvUtil.environment.getProperty("message.lastName"));
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userName", "message.userName",CapvUtil.environment.getProperty("message.userName"));
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "registrationSource", "message.registrationSource",CapvUtil.environment.getProperty("message.registrationSource"));
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "osType", "message.osType",CapvUtil.environment.getProperty("message.osType"));
         
         if(user.getRegistrationSource() == null ||
         		(!user.getRegistrationSource().equals(RegistrationSource.FACEBOOK.getRegistrationSource()) && 
         				!user.getRegistrationSource().equals(RegistrationSource.GPLUS.getRegistrationSource()))) {
         	ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "message.password",CapvUtil.environment.getProperty("message.password"));
         	ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "message.email",CapvUtil.environment.getProperty("message.email"));
             ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mobile", "message.mobile",CapvUtil.environment.getProperty("message.mobile"));
         }
         
         if(!errors.hasFieldErrors() && !errors.hasErrors()) {
         	
             if(user.getUserName() != null && 
             		(user.getRegistrationSource() == null ||
             		(!user.getRegistrationSource().equals(RegistrationSource.FACEBOOK.getRegistrationSource()) && 
             				!user.getRegistrationSource().equals(RegistrationSource.GPLUS.getRegistrationSource())))) {
             	if(!UserNameValidator.getInstance().isValid(user.getUserName()))
             		errors.rejectValue("userName", "message.userName",CapvUtil.environment.getProperty("message.userNameNotValid"));
             }
             
             if(user.getPassword() != null) {
             	if(!PasswordValidator.getInstance().isValid(user.getPassword()))
             		errors.rejectValue("password", "message.password",CapvUtil.environment.getProperty("message.passwordNotValid"));
             }
             
             if(user.getEmail() != null) {
             	if(!EmailValidator.getInstance().isValid(user.getEmail()))
             		errors.rejectValue("email", "message.email",CapvUtil.environment.getProperty("message.emailNotValid"));
             }
             
             if(user.getMobile() != null) {
             	if(!MobileValidator.getInstance().isValid(user.getMobile()))
             		errors.rejectValue("mobile", "message.mobile",CapvUtil.environment.getProperty("message.mobileNotValid"));
             }
             
             if(user.getFirstName() != null) {
             	if(!NameValidator.getInstance().isValid(user.getFirstName()))
             		errors.rejectValue("firstName", "message.firstName",CapvUtil.environment.getProperty("message.firstNameNotValid"));
             }
             
             if(user.getLastName() != null) {
             	if(!NameValidator.getInstance().isValid(user.getLastName()))
             		errors.rejectValue("lastName", "message.lastName",CapvUtil.environment.getProperty("message.lastNameNotValid"));
             }
             
             if(user.getRegistrationSource() != null) {
             	if(!RegistrationSource.MANUAL.contains(user.getRegistrationSource()))
             		errors.rejectValue("registrationSource", "message.registrationSource",CapvUtil.environment.getProperty("message.registrationSourceNotValid"));
             }
             
             if(user.getOsType() != null) {
             	if(user.getOsType().length() > 50)
             		errors.rejectValue("osType", "message.osType",CapvUtil.environment.getProperty("message.osTypeNotValid"));
             }
             
             if(user.getDeviceId() != null) {
             	if(user.getDeviceId().length() > 75)
             		errors.rejectValue("deviceId", "message.deviceId",CapvUtil.environment.getProperty("message.deviceId"));
             }
         }
    }
    
    private void validateUpdateUser(User user, Errors errors) {
    	
    	 ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstName", "message.firstName",CapvUtil.environment.getProperty("message.firstName"));
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastName", "message.lastName",CapvUtil.environment.getProperty("message.lastName"));
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "message.email",CapvUtil.environment.getProperty("message.email"));
         ValidationUtils.rejectIfEmptyOrWhitespace(errors, "mobile", "message.mobile",CapvUtil.environment.getProperty("message.mobile"));
         
         if(!errors.hasFieldErrors() && !errors.hasErrors()) {
         	
             if(user.getEmail() != null) {
             	if(!EmailValidator.getInstance().isValid(user.getEmail()))
             		errors.rejectValue("email", "message.email",CapvUtil.environment.getProperty("message.emailNotValid"));
             }
             
             if(user.getMobile() != null) {
             	if(!MobileValidator.getInstance().isValid(user.getMobile()))
             		errors.rejectValue("mobile", "message.mobile",CapvUtil.environment.getProperty("message.mobileNotValid"));
             }
             
             if(user.getFirstName() != null) {
             	if(!NameValidator.getInstance().isValid(user.getFirstName()))
             		errors.rejectValue("firstName", "message.firstName",CapvUtil.environment.getProperty("message.firstNameNotValid"));
             }
             
             if(user.getLastName() != null) {
             	if(!NameValidator.getInstance().isValid(user.getLastName()))
             		errors.rejectValue("lastName", "message.lastName",CapvUtil.environment.getProperty("message.lastNameNotValid"));
             }
         }
    }

}

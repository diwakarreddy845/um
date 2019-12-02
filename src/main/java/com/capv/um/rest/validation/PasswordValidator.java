package com.capv.um.rest.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordValidator {
	
	private static PasswordValidator passwordValidator;
	
	private Pattern pattern;
    private Matcher matcher;
    private static final String PASSWORD_PATTERN = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[!@#$%^&])[A-Za-z\\d][A-Za-z\\d!@#$%^&]{5,14}$";

    private PasswordValidator(){}
    
    public static PasswordValidator getInstance() {
    	
    	if(passwordValidator == null)
    		passwordValidator = new PasswordValidator();
    	
    	return passwordValidator;
    }
    
    public boolean isValid(final String password) {
		pattern = Pattern.compile(PASSWORD_PATTERN);
		matcher = pattern.matcher(password);
		return matcher.matches();
    }

}

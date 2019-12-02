package com.capv.um.rest.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UserNameValidator {
	
	private static UserNameValidator userNameValidator;
	
	private Pattern pattern;
    private Matcher matcher;
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9_.]{1,13}[a-zA-Z0-9]$";
    
    private UserNameValidator(){}
    
    public static UserNameValidator getInstance() {
    	
    	if(userNameValidator == null)
    		userNameValidator = new UserNameValidator();
    	
    	return userNameValidator;
    }

    public boolean isValid(final String userName) {
		pattern = Pattern.compile(USERNAME_PATTERN);
		matcher = pattern.matcher(userName);
		return matcher.matches();
    }

}

package com.capv.um.rest.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {
	
	private static EmailValidator emailValidator = null;
	
    private Pattern pattern;
    private Matcher matcher;
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private EmailValidator(){}
    
    public static EmailValidator getInstance() {
    	if(emailValidator == null)
    		emailValidator = new EmailValidator();
    	
    	return emailValidator;
    }
    
    public boolean isValid(final String email) {
    	pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

}

package com.capv.um.rest.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameValidator {
	
	private static NameValidator nameValidator;
	
    private Pattern pattern;
    private Matcher matcher;
    private static final String NAME_PATTERN = "^[A-Za-z\\s]{1,20}$";
    
    private NameValidator(){}
    
    public static NameValidator getInstance() {
    	if(nameValidator == null)
    		nameValidator = new NameValidator();
    	
    	return nameValidator;
    }

    public boolean isValid(final String name) {
    	pattern = Pattern.compile(NAME_PATTERN);
        matcher = pattern.matcher(name);
        return matcher.matches();
    }

}

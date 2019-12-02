package com.capv.um.rest.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MobileValidator {
	
	private static MobileValidator mobileValidator = null;
	
	private Pattern pattern;
    private Matcher matcher;
    private static final String MOBILE_PATTERN = "^(\\+\\d{1,3}[-]?)?\\d{10}$";
    
    private MobileValidator(){}
    
    public static MobileValidator getInstance() {
    	if(mobileValidator == null)
    		mobileValidator = new MobileValidator();
    	
    	return mobileValidator;
    }

    public boolean isValid(final String mobile) {
		pattern = Pattern.compile(MOBILE_PATTERN);
		matcher = pattern.matcher(mobile);
		return matcher.matches();
    }

}

package com.capv.um.constants;

public class CapvConstants {
	
	public static final String CLIENT_ID_KEY = "client_id";
	public static final String MOBILE_KEY = "mobile";
	
	public static final String USER_ROLE = "USER";
	public static final int WEEKLY = 2;
	public static final int DAILY = 1;
	public static final String WEEK_DAYS[] = new String[]{"SU","MO","TU","WE","TH","FR","SA"};
	
	
	
	public enum RegistrationSource {
		
		FACEBOOK("fb"),
		GPLUS("gplus"),
		MANUAL("manual"),
		LDAP("ldap");
		
		private String registrationSource;
		
		public String getRegistrationSource() {
			return registrationSource;
		}
		
		private RegistrationSource(String registrationSource) {
			this.registrationSource = registrationSource;
		}
		
		public boolean contains(String registrationSource) {
			
			for(RegistrationSource regSource: RegistrationSource.values())
				if(regSource.getRegistrationSource().equals(registrationSource))
					return true;
			
			return false;
		}
	}

}

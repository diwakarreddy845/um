package com.capv.um.rest.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.constants.CapvClientUserConstants.UserState;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.cache.UserCacheManager;
import com.capv.um.constants.CapvConstants.RegistrationSource;
import com.capv.um.exception.UserAlreadyExistException;
import com.capv.um.model.User;
import com.capv.um.model.UserConfig;
import com.capv.um.model.UserConfigProperty;
import com.capv.um.model.UserDataDTO;
import com.capv.um.rest.validation.ChangePasswordValidator;
import com.capv.um.rest.validation.UserValidator;
import com.capv.um.rest.validation.UserValidator.ValidationType;
import com.capv.um.security.UserRepositoryUserDetails;
import com.capv.um.service.EmailService;
import com.capv.um.service.UserConfigService;
import com.capv.um.service.UserService;
import com.capv.um.util.CapvUtil;
import com.capv.um.util.ServiceStatus;
import com.capv.um.view.ChangePasswordView;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * <h1>UserController</h1>
 * this class is used to perform custom user operations 
 * @author narendra.muttevi
 * @version 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserConfigService userConfigService;
	
	@Autowired
	private UserCacheManager userCacheManager;
	
	private TokenStore tokenStore;
	
	@Autowired
	private EmailService email;
	
	private static final Logger log = LoggerFactory.getLogger(UserController.class);
	
	/**
	 * this method is used to check oauth token for security purpose
	 * @param authHeaderToken
	 * @return status of oauth token 
	 */
	@RequestMapping( value="/checkSession", method = RequestMethod.POST, 
					produces={"application/json"},
				    consumes={"application/json"})
	@ApiOperation(value = "Check OAuth session against the OAuth token sent through authorization header "
			+ "and return user details which is associated with OAuth token as part of service status result.")
	public ServiceStatus<Object> checkToken(@ApiParam(name="Authorization", value="The authorization header with the value of OAuth token prepended with \"Bearer \"", required=true) 
									@RequestHeader(name="Authorization", required=true)
										String authHeaderToken) {
		log.debug("Entered into checkSession method");
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.validUserSession"));
		
		if(tokenStore == null) {
			ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			WebApplicationContext applicationContext = WebApplicationContextUtils
														.getRequiredWebApplicationContext(
																requestAttributes.getRequest().getServletContext());
			
			tokenStore = (TokenStore)applicationContext.getBean("tokenStore");
		}
		
		String authToken = authHeaderToken.substring("Bearer ".length());
		OAuth2AccessToken oauth2TokenDetails = tokenStore.readAccessToken(authToken);
		
		if(oauth2TokenDetails == null || oauth2TokenDetails.isExpired()){
			
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.userSessionNotExist"));
			
			return serviceStatus;
		} else {
			OAuth2Authentication oauth2Authentication = tokenStore.readAuthentication(oauth2TokenDetails);
			
			if(oauth2Authentication != null && oauth2Authentication.getUserAuthentication() != null && 
					oauth2Authentication.getUserAuthentication().getPrincipal() != null && 
					oauth2Authentication.getUserAuthentication().getPrincipal() instanceof UserRepositoryUserDetails) {
				
				Map<String, Object> result = new HashMap<>();
				
				UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails)oauth2Authentication.getUserAuthentication().getPrincipal();
				
				result.put("userName", userDetails.getUsername());
				result.put("userId", userDetails.getId());
				result.put("clientId", userDetails.getClientId());
				
				serviceStatus.setResult(result);
			}
			
		}
		
		log.debug("Exit from checkSession method");
		return serviceStatus;
	}
	
	@RequestMapping( value="/getOAuthClientDetails", method = RequestMethod.GET, 
					produces={"application/json"})
	public Map<String, String> getOAuthClientDetails(@RequestParam("clientId") String clientId) {
		
		log.debug("Entered into getOAuthClientDetails method");
		Map<String, String> oauthClientDetails = null;
		
		//String clientId = environment.getProperty("client.id");
		ClientDetails clientDetails = null;
		
		if(clientId != null && clientId.trim().length() > 0) {
			clientId = clientId + "@web";
			clientDetails = CapvClientUserUtil.getOAuthClientDetails(
													CapvClientUserConstants.GET_OAUTH_CLIENT_DETAILS_BY_CLIENT_ID, 
													clientId);
		}
		
		if(clientDetails != null && 
				clientDetails.getClientId() != null && 
				clientDetails.getClientSecret() != null) {
			oauthClientDetails = new HashMap<>();
			oauthClientDetails.put("oauthClientId", clientDetails.getClientId());
			oauthClientDetails.put("oauthClientSecret", clientDetails.getClientSecret());
		}
		
		log.debug("Exit from getOAuthClientDetails method");
		return oauthClientDetails;
	}
	
	/**
	 * this method is used to perform register operation for user
	 * @param user the user name
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @return status of registration
	 */
	
	@RequestMapping(value="/register", method = RequestMethod.POST, 
					produces={"application/json"},
			        consumes={"application/json"})
	@ApiOperation(value = "Creates the new user into application if the request data is valid "
							+ "and returns success response otherwise throws an error as part of response")
	
	/*@ApiResponses(value = { @ApiResponse(value="{\"status\":\"success\", \"message\":\"Registration successful\"}"),
			@ApiResponse(value="{\"status\":\"failure\", \"message\":\"User already registered\"}"),
			@ApiResponse(value="{\"status\":\"Error\", \"message\":\"Server error. Please contact Administrator\"}"),
			@ApiResponse(value="{\"status\":\"failure\", \"message\":\"Does not meet user requirements\"}"), 
			@ApiResponse(value="{\"status\":\"failure\", \"message\":\"Invalid user details\"}")})*/
	public ServiceStatus<Object> registerUser(@RequestBody User user, BindingResult bindingResult,
					HttpServletRequest request, HttpServletResponse response) throws BindException {
	
		log.debug("Entered into create new user method");
		log.info("Creating new user with user object     :{}", user);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.registrationSuccess"));
		
		String userName = user.getUserName().trim();
		String password = user.getPassword();
		Long clientId = 0l;
		clientId = user.getClientId();
		UserValidator userValidator = new UserValidator(ValidationType.SAVEUSER);
        userValidator.validate(user, bindingResult);
        Long maxCount= userService.getUserCount(clientId, 1L);
        if(bindingResult.hasErrors())
        {
            log.error("Binding exception while creating new user");
        	throw new BindException(bindingResult);
        }	
		
       
		String lowerCaseUsername = userName.toLowerCase()+"_"+clientId;
	       
		User dbUser = userService.getByUserName(lowerCaseUsername, false);
		
		if(dbUser != null) {
			log.error("User already exist exception from create new user method.");
			throw new UserAlreadyExistException(CapvUtil.environment.getProperty("message.userExist"));
			
		} else {
			
			try {
				if(maxCount <= Long.parseLong(CapvClientUserUtil.getClientConfigProperty(user.getClientId(), CapvClientUserConstants.CAPV_CONFIG_MAX_USER_COUNT))){
			
					user.setUserName(userName.toLowerCase()+"_"+user.getClientId());
					user.setCreatedDate(new Date());
					user.setLastUpdated(new Date());
					user.setActive(true);
					user.setPassword(CapvUtil.encodePassword(password));
					user.setCallStatus(UserState.NOTLOGGEDIN.getStateId());
					user.setName(user.getFirstName() + " " +user.getLastName());
					
					if(user.getRegistrationSource().equals(RegistrationSource.FACEBOOK.getRegistrationSource()) || 
						user.getRegistrationSource().equals(RegistrationSource.GPLUS.getRegistrationSource())) {
					
						user.setPassword(CapvUtil.encodePassword(user.getUserName() + "@" +
																user.getRegistrationSource()));
					}
					List<UserConfig> userConfigList = getDefaultUserConfig();
					userService.save(user, userConfigList);
					
					//userCacheManager.addToUserCache(user);
					String[] to= user.getEmail().split(",");
					String clientName=CapvClientUserUtil.getClientConfigProperty(user.getClientId(),CapvClientUserConstants.CAPV_UI_CLIENT_NAME);
					email.sendInviteEmail(to,clientName+": Registered successfully",  "Hey &nbsp;"+user.getName()+",<br><br>"
							+ "Thanks for registering with capV."+"<br>" + 
							"Please use the following URL and credentials to login.\r\n" + 
							"<br><br>"
							+ "URL: &nbsp;"+"<a href=\""+ CapvClientUserUtil.getClientConfigProperty(user.getClientId(),CapvClientUserConstants.CAPV_UI_URL) + "\">Click to Login</a>" +  
							"<br>Username:&nbsp;" +user.getUserName().substring(0, user.getUserName().lastIndexOf("_"))+
							"<br>Password:&nbsp;" +password+
							"<br><br>Thanks,\r\n"+"<br>"
							+ clientName+"&nbsp;Support&nbsp;Team");
				}else{
					log.error("Exception occured while creating new user");
					serviceStatus.setStatus("Error");
			    	serviceStatus.setMessage("User registration limit exceeded. Please contact Administrator");
				}
			} catch (Exception e) {
				log.error("Exception occured while creating new user :", e);
				serviceStatus.setStatus("Error");
		    	serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverError"));
			}
		}
		
		log.debug("Exit from create new user post method");
		return serviceStatus;
	
	}
	
	private List<UserConfig> getDefaultUserConfig() {
		
		List<UserConfig> userConfigList = new ArrayList<>();
		List<UserConfigProperty> userConfigProperties = userConfigService.getAllUserConfigPropertyDetails();
		
		for(UserConfigProperty userConfigProperty :userConfigProperties) {
			
			UserConfig userConfig = new UserConfig();
			userConfig.setConfigProperty(userConfigProperty);
			
			switch(userConfigProperty.getName()) {
				case "chathistory":
					userConfig.setPropValue("30");
					break;
					
				case "chatlayout":
					userConfig.setPropValue("basic");
					break;
					
				default:
					userConfig.setPropValue("true");
					break;
			}
			
			userConfigList.add(userConfig);
		}
		
		return userConfigList;
	}
	
	/**
	 * this method is used to retrieve the user by clientId
	 * @param clientId the clientId
	 * @return user record
	 */
	@RequestMapping( value = "/all/{clientId}", method = RequestMethod.GET, 
					produces={"application/json"})
	public ServiceStatus<List<User>> getUsers(@PathVariable("clientId") Long clientId) {
		
		log.debug("Entered into all/{clientId} method");
		log.info("Getting users with clientId  :{}",clientId);
		ServiceStatus<List<User>> serviceStatus = new ServiceStatus<List<User>>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.usersFetchedSuccess"));
		 
		if(clientId != null) {
			
			List<User> users = userService.getByClientId(clientId);
			
			if (users != null && !users.isEmpty())
				serviceStatus.setResult(users);
			else{

				serviceStatus.setStatus("failure");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.invalidClientId"));
			}
			
		} else {
			
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.invalidClientId"));
		}
		
			
		log.debug("Exit from all/{clientId} method");
		return serviceStatus;
	}
	
	/**
	 * this method is used to get the record by userName
	 * @param userName the userName
	 * @return user record
	 */
	@RequestMapping( value = "/getUserDetailsByUsername", method = RequestMethod.POST, 
											produces={"application/json"}, consumes={"application/json"})
	public ServiceStatus<User> getUserDetailsByUsername(@RequestBody User user) {
	
		log.debug("Entered into /getUserDetailsByUsername method");
		log.info("Getting user with getUserDetailsByUsername    :{}",user.getUserName());
		
		ServiceStatus<User> serviceStatus = new ServiceStatus<User>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.usersFetchedSuccess"));
		
		try {
			User userDetails = userService.getByUserName(user.getUserName(), true);
			serviceStatus.setResult(userDetails);
		} catch (Exception e){
			serviceStatus.setStatus("fail");
			serviceStatus.setStatus(CapvUtil.environment.getProperty("message.serverFailedError"));
		}
		log.debug("Exit from /getUserDetailsByUsername method");
		return serviceStatus;
	}
	
	/**
	 * this method is used to perform update operation of user
	 * @param user the object of user
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @return status of update operation
	 */
	@RequestMapping( method = RequestMethod.PUT, 
					produces={"application/json"},
			        consumes={"application/json"})
	public ServiceStatus<Object> updateUser(@RequestBody User user, OAuth2Authentication oAuth2Authentication,
											BindingResult bindingResult,
											HttpServletRequest request, HttpServletResponse response) throws BindException {
	
		log.debug("Entered into update user put method");
		log.info("updating user with user object    :{}",user);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.userUpdatedSuccess"));
		
		try {
			
			UserValidator userValidator = new UserValidator(ValidationType.UPDATEUSER);
			userValidator.validate(user, bindingResult);
			
			if(bindingResult.hasErrors())
				throw new BindException(bindingResult);
			
			UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails)oAuth2Authentication.getPrincipal();
			Long userId = userDetails.getId();
			
			User dbUser = userService.getById(userId);
			
			dbUser.setFirstName(user.getFirstName());
			dbUser.setLastName(user.getLastName());
			dbUser.setName(user.getFirstName() + " " + user.getLastName());
			dbUser.setEmail(user.getEmail());
			dbUser.setMobile(user.getMobile());
			
			userService.update(dbUser, false);
			
			userCacheManager.addToUserCache(dbUser);
		} catch (BindException be) {
			log.error("Binding Exception occured while upadating user   :", be);
			throw be;
		} catch (Exception e) {
			log.error("Exception  occured while upadating user   :",e);
			serviceStatus.setStatus("fail");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
		}
		log.debug("Exit from update user put method");
		return serviceStatus;
	
	}
	
	@RequestMapping(value = "/generatePassword", method = RequestMethod.POST, 
			produces={"application/json"},
	        consumes={"application/json"})
    public ServiceStatus<Map<String,String>> generatePassword(@RequestBody User user,
			HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into /generatePassword method");
		log.info("Generating password with user object   :{}",user);
		ServiceStatus<Map<String,String>> serviceStatus = new ServiceStatus<>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.passwordGenerationSuccess"));
		Map<String,String>  genaratePasswords = new HashMap<String,String>();  
		String userName = user.getUserName();
		Long clientId	= user.getClientId();
		
		if(userName !=null && clientId != null){
			User oldUser = userService.getByUserName(userName, false);
			if(oldUser != null && oldUser.getClientId().equals(user.getClientId())) {
				genaratePasswords.put("password", oldUser.getPassword());
				genaratePasswords.put("generatePassword", CapvUtil.encodePassword(oldUser.getPassword()));
				oldUser.setPassword(CapvUtil.encodePassword(oldUser.getPassword()));
				userService.update(oldUser, false);
				serviceStatus.setResult(genaratePasswords);
			}else {
				
				serviceStatus.setStatus(" failure");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.invalidDetails"));
				
				return serviceStatus;
				
			}
		}else {
			serviceStatus.setStatus(" failure");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.invalidDetails"));
			return serviceStatus;
		}
		   log.debug("Exit from /generatePassword method");
				return serviceStatus;
	}
	/**
	 * this method is used to perform delete operation of user 
	 * @param userName the userName
	 * @return status of delete operation
	 */
	@RequestMapping( value = "/{userName}", method = RequestMethod.DELETE, 
			produces={"application/json"})
		public ServiceStatus<Object> deleteUser(@PathVariable("userName") String userName) {
		
		log.debug("Entered into /{userName} DELETE mehtod");
		log.info("Trying to delete user with username   :{}",userName);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.userDeletedSuccess"));
		
		User user = userService.getByUserName(userName, false);
		if(user != null) {
			
			try {
				userService.delete(user);
			} catch (Exception e) {
				log.error("Exception  occured while deleting user :", e);
				serviceStatus.setStatus("Error");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverError"));
				return serviceStatus;
			}
			
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.userNotFound"));
		}
		log.debug("Exit from /{userName} DELETE method");
		return serviceStatus;
	}
	
	/**
	 * this method is used to get the online users of friends list
	 * @param loginName the userName
	 * @return list of online users of friends list
	 */
	@RequestMapping( value = "/onlineusers/{loginname}", method = RequestMethod.GET, 
					produces={"application/json"})
	public List<User> getOnlineUsers(@PathVariable("loginname") String loginName) {
		log.debug("Entered into  /onlineusers/{loginname} method");
		log.info("Getting all Online Users with loginName  :{}" , loginName);
		return userService.getOnlineUsers(loginName);
	}
	
	/**
	 * this method is used to perform login operation
	 * @param request HttpServletRequest
	 * @param response HttpServletResponse
	 * @return status of login operation
	 */
	@RequestMapping(value ="/login",  method = { RequestMethod.POST })
	public ServiceStatus<Object> loginUser(HttpServletRequest request, HttpServletResponse response) {
		
		log.debug("Entered into /login method");
		//String userName		=	request.getParameter("username").trim();
		//String password		=	request.getParameter("password").trim();
		
		ServiceStatus<Object> loginStatus = new ServiceStatus<Object>();
		
		
		loginStatus.setStatus("failure");
		loginStatus.setMessage(CapvUtil.environment.getProperty("message.invalidUsernameOrPassword"));
			
		log.debug("Exit from /login method.");
		return loginStatus;
	}
	
	/**
	 * this method is used to get the user by userId
	 * @param userId the userId
	 * @return user
	 */
	@RequestMapping( value = "/getById", method = RequestMethod.POST, 
											produces={"application/json"}, consumes={"application/json"})
	public ServiceStatus<User> getUser(@RequestBody Long userId) {
		log.debug("Entered into /getById method");
		log.info("Getting User with userId  :{}", userId);
		ServiceStatus<User> serviceStatus = new ServiceStatus<>();
		serviceStatus.setStatus("success");
		
		try {
			User user = userService.getById(userId, true);
			serviceStatus.setResult(user);
		} catch (Exception e){
			log.error("Exception occured while getting user with userId  :", e);
			serviceStatus.setStatus("fail");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
		}
		log.debug("Exit from /getById method");
		return serviceStatus;
	}
	
	/**
	 *  This method is used to update User configuration information.
	 *  @param updateConfigDetails - it contains all the User configuration information which User want to update.
	  * @return serviceStatus as 'true' on successful updation of configuration information, 
	 *   or returns serviceStatus as 'false' on unsuccessful updation of configuration information.
	 */
	@RequestMapping(value = "/config", method = RequestMethod.PUT, 
					produces = {"application/json" }, consumes = {"application/json"})
	public ServiceStatus<Object> updateUserConfig(@RequestBody List<UserConfig> updateConfigList) {
		
		log.debug("Entered into /config put method.");
		ServiceStatus<Object> serviceStatus = new ServiceStatus<>();

		if (userConfigService.updateUserConfig(updateConfigList)){
			serviceStatus.setStatus("success");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.updatedConfigDataSuccess"));
		}
		else{
			serviceStatus.setStatus("fail");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.insufficientConfigData"));
		}
		log.debug("Exit from /config put method.");
		return serviceStatus;
	}
	/**
	 *   This method is used for inserting User configuration information.
	 *   @param insertConfigDetails - it contains all the User configuration information which User want to insert.
	  *  @return serviceStatus as 'true' on successful insertion of configuration information, 
	 *    or returns serviceStatus as 'false' on unsuccessful insertion of configuration information.
	 */
	@RequestMapping(value = "/config", method = RequestMethod.POST, 
					produces = { "application/json" }, consumes = {"application/json"})
	public ServiceStatus<Object> saveUserConfig(@RequestBody List<UserConfig> userConfigList) {
	
		log.debug("Entered into /config post method.");
		ServiceStatus<Object> serviceStatus = new ServiceStatus<>();

		try {
			if (userConfigService.insertUserConfig(userConfigList)){
				serviceStatus.setStatus("success");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.addedConfigDataSuccess"));
			}
			else{
				serviceStatus.setStatus("fail");
			    serviceStatus.setMessage(CapvUtil.environment.getProperty("message.insufficientConfigData"));
			}
		} catch (Exception e) {
			log.error("Exception occured while saving user configurations  :",e);
			serviceStatus.setStatus("fail");
		    serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
		}
		log.debug("Exit from /config post method.");
		return serviceStatus;
	}
	
	
	/**
	 *  This method is used to get particular User configuration details
	 *   based on UserID .
	 *  @param userId - it specifies the UserId of User.
	 *  @return serviceStatus contains configuration details of given UserConfig if UserId of client matches with existing UserId.
	 */
	@RequestMapping(value = "/config/{userId}", method = RequestMethod.GET, produces = {"application/json" })
	public ServiceStatus<List<UserConfig>> getUserConfig(@PathVariable("userId")Long userId) {
		log.debug("Entered into /config/{userId} method");
		log.info("Getting user configurations with userId :  {}",userId);
		ServiceStatus<List<UserConfig>> serviceStatus = new ServiceStatus<>();
		
			try {
				List<UserConfig> userConfig = userConfigService.getUserConfigDetailsByUserId(userId);
				serviceStatus.setStatus("success");
				serviceStatus.setResult(userConfig);
			} catch(Exception e) {
				log.error("Exception occured while getting user configuration with userId  :",e);
				serviceStatus.setStatus("fail");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
			}
            log.debug("Exit from  /config/{userId} method");
			return serviceStatus;
	}
	
	/*@RequestMapping(value = "/config/{userName:.+}", method = RequestMethod.GET, produces = {"application/json" })
	public ServiceStatus<List<UserConfig>> getUserConfig(@PathVariable("userName")String userName) {
		ServiceStatus<List<UserConfig>> serviceStatus = new ServiceStatus<>();
		
			try {
				User user = userService.getByUserName(userName, true);
				if(user != null) {
					serviceStatus.setStatus("success");
					serviceStatus.setResult(user.getUserConfig());
				} else {
					serviceStatus.setStatus("fail");
					serviceStatus.setMessage(CapvUtil.environment.getProperty("message.userConfigurationNotFound"));
				}
			} catch(Exception e) {
				serviceStatus.setStatus("fail");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
			}

			return serviceStatus;
	}*/
	
	@RequestMapping(value = "/changePassword", method = RequestMethod.PUT, 
					produces={"application/json"}, consumes={"application/json"})
	public ServiceStatus<Object> changePassword(@RequestBody ChangePasswordView changePasswordView, 
												OAuth2Authentication oAuth2Authentication, BindingResult bindingResult) throws BindException{
		log.debug("Entered into /changePassword method");
		log.info("changing password with ChangePasswordView object : {}",changePasswordView);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<>();
		boolean passwordResetSuccess = false;
		
		try {
			ChangePasswordValidator changePasswordValidator = new ChangePasswordValidator();
			String userName = oAuth2Authentication.getName();
			
			User user = userService.getByUserName(userName, false);
			changePasswordView.setCurrentPassword(user.getPassword());
			
			changePasswordValidator.validate(changePasswordView, bindingResult);
			
			if(bindingResult.hasErrors())
	        	throw new BindException(bindingResult);
			
			String newEncodedPassword = CapvUtil.encodePassword(changePasswordView.getNewPassword());
			userService.changePassword(newEncodedPassword, userName);
			passwordResetSuccess = true;
			serviceStatus.setStatus("success");
		} catch (BindException be) {
			log.error("Binding Exception occured while changing user password :",be);
			throw be;
		} catch (Exception e) {
			log.error("Exception occured while changing user password :",e);
			serviceStatus.setStatus("fail");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
		} finally {
			if(passwordResetSuccess) {
				UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails)oAuth2Authentication.getPrincipal();
				CapvUtil.removeUserOAuthTokens(userDetails.getClientId(), userDetails.getUserName());
			}
		}
		log.debug("Exit from /changePassword method");
		return serviceStatus;
	}
	
	@RequestMapping(value = "/resetPassword/{userName:.+}", method = RequestMethod.GET, 
					produces={"application/json"})
	public ServiceStatus<Object> resetPassword(@PathVariable("userName") String userName){
		log.debug("Entered into /resetPassword/{userName:.+} mehtod");
		log.info("Reset password with userName  :{}",userName);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<>();
		
		try {
			User user = userService.getByUserName(userName, false);
			if(user != null && 
					user.getRegistrationSource().equals(RegistrationSource.MANUAL.getRegistrationSource())) {
				userService.resetPassWordAndSendEmail(user, CapvUtil.getRandomString(8));
				
				serviceStatus.setStatus("success");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.resetPasswodRequestSuccess"));
			} else {
				serviceStatus.setStatus("fail");
				serviceStatus.setMessage(CapvUtil.environment.getProperty("message.invalidUserName"));
			}
		} catch (Exception e){
			log.error("Exception occured while resetting password  :",e);
			serviceStatus.setStatus("fail");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.resetPasswordRequestFailed"));
		}
		log.debug("Exit from /resetPassword/{userName:.+} method");
		return serviceStatus;
	}
	
	/**
	 *  This method is used to get all users details using server side pagination.
	 *  @param draw                -  it is used by DataTables to ensure that data returns from server-side processing requests are drawn in sequence by DataTables.
	 *  @param length              -  it indicate that number of records data table can display in the current draw. 
	 *  @param start                 -  it indicate the starting point in the current data set.
	 *  @return userDataDTO contains an array of users details to be displayed in data table.
	 */
	@RequestMapping( value = "/all/pagination/{clientId}/{draw}/{length}/{start}", method = RequestMethod.GET, produces={"application/json"})
	public UserDataDTO getUsersUsingPagination(@PathVariable("clientId") String clientId, @PathVariable("draw") String draw,
																																@PathVariable("length") String length, @PathVariable("start") String start) 
	{
		log.debug("Entered into getUsersUsingPagination  all/{clientId} method");
		log.info("Getting users getUsersUsingPagination with clientId  :{}",clientId);

		UserDataDTO userDataDTO = new UserDataDTO();
		 
		if(clientId != null) {
			Long clientId1 = Long.parseLong(clientId);
			Integer draw1 = Integer.parseInt(draw);
			
			List<User> usersList = userService.getUsersByPaginationUsingClientId(clientId1, start, length);

			Long totalRecordCount = userService.getListOfUsers(clientId1);

			userDataDTO.setData(usersList);
			userDataDTO.setRecordsTotal(totalRecordCount);
			userDataDTO.setDraw(draw1);
			userDataDTO.setRecordsFiltered(totalRecordCount);

			log.debug("Exit from all/{clientId} getUsersUsingPagination method");
		}
		
		return userDataDTO;
	}
	
	/**
	 *  This method is used to get all users details using server side pagination with search.
	 *  @param draw                -  it is used by DataTables to ensure that data returns from server-side processing requests are drawn in sequence by DataTables.
	 *  @param length              -  it indicate that number of records data table can display in the current draw. 
	 *  @param start                 -  it indicate the starting point in the current data set.
	 *  @param searchParam  -  it indicate the records to be search based on specific column.
	 *  @return userDataDTO contains an array of users details to be displayed in data table.
	 */
	@RequestMapping( value = "/all/pagination/{clientId}/{draw}/{length}/{start}/{searchParam}", method = RequestMethod.GET, produces={"application/json"})
	public UserDataDTO getUsersUsingPaginationWithSearch(@PathVariable("clientId") String clientId, @PathVariable("draw") String draw,@PathVariable("length") String length, 
																								@PathVariable("start") String start, @PathVariable("searchParam") String searchParam) 
	{
		log.debug("Entered into getUsersUsingPaginationWithSearch  all/{clientId} method");
		log.info("Getting users getUsersUsingPaginationWithSearch with clientId  :{}",clientId);

		UserDataDTO userDataDTO = new UserDataDTO();
		 
		if(clientId != null) {
				
			Long clientId1 = Long.parseLong(clientId);
			Integer draw1 = Integer.parseInt(draw);
			
			List<User> usersList = userService.getUsersByPaginationUsingClientId(clientId1, start, length, searchParam);

			Long totalRecordCount = userService.getListOfUsersForSearch(clientId1, searchParam);

			userDataDTO.setData(usersList);
			userDataDTO.setRecordsTotal(totalRecordCount);
			userDataDTO.setDraw(draw1);
			userDataDTO.setRecordsFiltered(totalRecordCount);

			log.debug("Exit from all/{clientId} getUsersUsingPaginationWithSearch method");
		}
		
		return userDataDTO;
	}
	
	/**
	 *  This method is used to perform update operation of user
	 *  @param user the object of user
	 *  @param request HttpServletRequest
	 *  @param response HttpServletResponse
	 *  @return status of update operation
	 */
	@RequestMapping(value="/update", method = RequestMethod.PUT, produces={"application/json"},
											consumes={"application/json"})
	public ServiceStatus<Object> updateUserDetails(@RequestBody User user, BindingResult bindingResult,
													HttpServletRequest request, HttpServletResponse response) throws BindException {
	
		log.debug("Entered into updateUserDetails user put method");
		log.info("updating user with user object    :{}",user);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.userUpdatedSuccess"));
		
		try {
			
			UserValidator userValidator = new UserValidator(ValidationType.UPDATEUSER);
			userValidator.validate(user, bindingResult);
			
			if(bindingResult.hasErrors())
				throw new BindException(bindingResult);
			
			Long userId = user.getId();
			
			User dbUser = userService.getById(userId);
			
			dbUser.setFirstName(user.getFirstName());
			dbUser.setLastName(user.getLastName());
			dbUser.setName(user.getFirstName() + " " + user.getLastName());
			dbUser.setEmail(user.getEmail());
			dbUser.setMobile(user.getMobile());
			
			if(user.getActive() != null)
				dbUser.setActive(user.getActive());
			
			userService.update(dbUser, false);
			
			userCacheManager.addToUserCache(dbUser);
		} catch (BindException be) {
			log.error("Binding Exception occured while upadating user   :", be);
			throw be;
		} catch (Exception e) {
			log.error("Exception  occured while upadating user   :",e);
			serviceStatus.setStatus("fail");
			serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
		}
		log.debug("Exit from updateUserDetails put method");
		return serviceStatus;
	
	}
	
	
	/**
	 *  This method is used for registering client users.
	 * @param user                       -  This specifies User model.
	 * @param bindingResult	  -  This specifies validations of user data.
	 * @param request				  -  It is instance of HttpServletRequest.
	 * @param response			  -  It is instance of HttpServletResponse.
	 * @return								  - It returns the status of user registration. 									
	 * @throws BindException   - It throws an exception when user data is invalid.
	 */
	@RequestMapping(value="/registerUser", method = RequestMethod.POST, produces={"application/json"}, consumes={"application/json"})
	@ApiOperation(value = "Creates the new user into application if the request data is valid " + "and returns success response otherwise throws an error as part of response")
	public ServiceStatus<Object> registerClientUser(@RequestBody User user, BindingResult bindingResult,
					HttpServletRequest request, HttpServletResponse response) throws BindException {
	
		log.debug("Entered into registerClientUser method");
		log.info("Creating new user with user object     :{}", user);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage(CapvUtil.environment.getProperty("message.registrationSuccess"));
		
		String userName = user.getUserName().trim();
		String password = user.getPassword();
		Long clientId = 0l;
		clientId = user.getClientId();
		UserValidator userValidator = new UserValidator(ValidationType.SAVEUSER);
        userValidator.validate(user, bindingResult);
        Long maxCount= userService.getUserCount(clientId, 1L);
        if(bindingResult.hasErrors())
        {
            log.error("Binding exception while creating new user");
        	throw new BindException(bindingResult);
        }	
		
       
		String lowerCaseUsername = userName.toLowerCase()+"_"+clientId;
	       
		User dbUser = userService.getByUserName(lowerCaseUsername, false);
		
		if(dbUser != null) {
			log.error("User already exist exception from create new user method.");
			throw new UserAlreadyExistException(CapvUtil.environment.getProperty("message.userExist"));
			
		} else {
			
			try {
				if(maxCount <= Long.parseLong(CapvClientUserUtil.getClientConfigProperty(user.getClientId(), CapvClientUserConstants.CAPV_CONFIG_MAX_USER_COUNT))){
			
					user.setUserName(userName.toLowerCase()+"_"+user.getClientId());
					user.setCreatedDate(new Date());
					user.setLastUpdated(new Date());
					user.setActive(true);
					user.setPassword(CapvUtil.encodePassword(password));
					user.setCallStatus(UserState.NOTLOGGEDIN.getStateId());
					user.setName(user.getFirstName() + " " +user.getLastName());
					
					if(user.getRegistrationSource().equals(RegistrationSource.FACEBOOK.getRegistrationSource()) || 
						user.getRegistrationSource().equals(RegistrationSource.GPLUS.getRegistrationSource())) {
					
						user.setPassword(CapvUtil.encodePassword(user.getUserName() + "@" +
																user.getRegistrationSource()));
					}
					List<UserConfig> userConfigList = getDefaultUserConfig();
					userService.save(user, userConfigList);
					
				//	userCacheManager.addToUserCache(user);
					String[] to= user.getEmail().split(",");
					String clientName=CapvClientUserUtil.getClientConfigProperty(user.getClientId(),CapvClientUserConstants.CAPV_UI_CLIENT_NAME);
					email.sendInviteEmail(to,clientName+": Registered successfully",  "Hey &nbsp;"+user.getName()+",<br><br>"
							+ "Thanks for registering with "+clientName+".<br>" + 
							"Please use the following URL and credentials to login.\r\n" + 
							"<br><br>"
							+ "URL:&nbsp;"+"<a href=\""+ CapvClientUserUtil.getClientConfigProperty(user.getClientId(),CapvClientUserConstants.CAPV_UI_URL) + "\">Click to Login</a>" +  
							"<br>Username:&nbsp;" +user.getUserName().substring(0, user.getUserName().lastIndexOf("_"))+
							"<br>Password:&nbsp;" +password+
							"<br><br>Thanks,\r\n"+"<br>"
							+ clientName+"&nbsp;Support&nbsp;Team");
					/*email.sendEmail(to, " Registered successfully", "Thank you for registering. Here are your credentials \n"+"URL:\t"+ CapvClientUserUtil.getClientConfigProperty(clientId, 
							CapvClientUserConstants.CAPV_UI_URL)+"\n"+"Username:\t "+userName+"\nPassword:\t "+password);*/
				}else{
					log.error("Exception occured while creating new user");
					serviceStatus.setStatus("Error");
			    	serviceStatus.setMessage("User registration limit exceeded. Please contact Administrator");
				}
			} catch (Exception e) {
				log.error("Exception occured while creating new user :", e);
				serviceStatus.setStatus("Error");
		    	serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverError"));
			}
		}
		
		log.debug("Exit from registerClientUser post method");
		return serviceStatus;
	}
	
	/**
	  *  This method is used to get user's Billing Cycle Date
	  *  @param userName of the User
	  *  @return the Billing Cycle Date
	  */
	 @RequestMapping(value="/fetchBillingCycleDate/{userName:.+}", method = RequestMethod.GET, produces={"application/json"})
	 public ServiceStatus<Object> fetchBillingCycleDate(@PathVariable("userName") String userName) {
	  log.debug("Entered into fetchBillingCycleDate get method");
	  ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
	  serviceStatus.setStatus("success");
	  
	  try {
			  		User dbUser = userService.getByUserName(userName,false);
			  		
					if(dbUser != null && dbUser.getBillingCycleDate() != null)
			  			serviceStatus.setResult(dbUser.getBillingCycleDate());
					else
						serviceStatus.setStatus("failure");
					
			  }catch (Exception e) {
				  	log.error("Exception  occured while fetchBillingCycleDate   :",e);
				   serviceStatus.setStatus("failure");
				   serviceStatus.setMessage(CapvUtil.environment.getProperty("message.serverFailedError"));
			  }
	  
		  log.debug("Exit from fetchBillingCycleDate get method");
		  return serviceStatus;
	 }

}

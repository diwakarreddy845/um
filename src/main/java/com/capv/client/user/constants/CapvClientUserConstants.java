package com.capv.client.user.constants;

public class CapvClientUserConstants {
	
	public static final String WS_MESSAGE_ID_KEY = "capv_msg";
	public static final String WS_GET_ENCRYTION_KEY = "op";
	public static final String WS_MESSAGE_LOGIN = "login";
	public static final String WS_MESSAGE_LOGOUT = "logout";
	public static final String WS_MESSAGE_USER_CHECK = "user_check";
	public static final String WS_MESSAGE_ERROR = "capv_error";
	public static final String WS_MESSAGE_CHANGE_PASSWORD = "change_password";
	public static final String WS_MESSAGE_CHANGE_PASSWORD_RESPONSE = "change_password_response";
	
	/* Login Related Messages */
	public static final String WS_MESSAGE_USER_LOGIN_SUCCESS = "user_login_success";
	public static final String WS_MESSAGE_USER_LOGIN_FAILURE = "user_login_failure";
	
	public static final String WS_MESSAGE_USER_SERVICES_CONNECT_STATUS = "user_services_connect_status";
	/*Video Record Related messages */
	public static final String WS_MESSAGE_RECORD_INITIAL_START = "video_record_initial_start";
	public static final String WS_MESSAGE_RECORD_START = "video_record_start";
	public static final String WS_MESSAGE_RECORD_ID = "video_record_id";
	public static final String WS_MESSAGE_VIDEO_RECORD_STATUS = "video_record_status";
	public static final String WS_MESSAGE_RECORD_ISFULLID = "video_record_isfullid";
	public static final String WS_MESSAGE_RECORD_STOP = "video_record_stop";
	public static final String WS_MESSAGE_FULL_RECORD_STOP = "video_full_record_stop";
	public static final String WS_MESSAGE_GET_ROOM = "get_room";
	public static final String WS_MESSAGE_SET_ROOM = "set_room";
	public static final String WS_MESSAGE_DELETE_GROUP = "delete_group";
	public static final String WS_MESSAGE_TRANSFER_PRIVILEGE = "transfer_privilege";
	public static final String WS_MESSAGE_GET_ROOM_DETAILS = "get_room_details";
	public static final String WS_MESSAGE_GET_GROUP_GROUP_DETAILS = "get_active_group_details";
	public static final String WS_MESSAGE_GET_GROUP_GROUP_DETAILS_RESPONSE = "get_active_group_details_response";
	public static final String WS_MESSAGE_GET_ROOM_DETAILS_RESPONSE = "get_room_details_response";
	
	public static final String WS_MESSAGE_ROOM_DELETE_RESPONSE = "room_delete_response";
	
	public static final String WS_MESSAGE_VC_SERVICE_CONNECT_ERROR = "vc_service_connect_error";
	
	/* Room Creation Related Messages  */
	public static final String WS_MESSAGE_USER_SEARCH ="user_search";
	public static final String WS_MESSAGE_SEARCH_USER = "search_user";
	public static final String WS_MESSAGE_ADD_FRIEND = "add_friend";
	public static final String WS_MESSAGE_DELETE_FRIEND = "delete_friend";
	public static final String WS_MESSAGE_SELECTED_FRIENDS = "selected_friends";
	public static final String WS_MESSAGE_ADD_GROUP_FRIENDS = "add_group_friends";
	public static final String WS_MESSAGE_EDIT_GROUP_FRIENDS = "edit_group_friends";
	public static final String WS_MESSAGE_DELETE_GROUP_FRIENDS = "delete_group_friends";
	public static final String WS_MESSAGE_ACCEPT_FREIND_REQUEST = "accept_friend_request";
	public static final String WS_MESSAGE_DECLINE_FRIEND_REQUEST = "decline_friend_request";
	public static final String WS_MESSAGE_ACCEPT_ROOM_REQUEST = "accept_room_request";
	public static final String WS_MESSAGE_DECLINE_ROOM_REQUEST = "decline_room_request";
	public static final String WS_MESSAGE_ACCEPT_ROOM_REQUEST_RESPONSE = "accept_room_request_response"; 
	public static final String WS_MESSAGE_SUBSCRIBE_BOTH = "subscribe_both";
	public static final String WS_MESSAGE_GET_HOSTEDROOMS = "get_hostedrooms";
	public static final String WS_MESSAGE_SET_HOSTEDROOMS = "set_hostedrooms";
	public static final String WS_MESSAGE_GROUP_MESSAGE_SEND = "group_message_send";
	public static final String WS_MESSAGE_MESSAGE_EDIT = "message_edit";
	public static final String WS_MESSAGE_MESSAGE_DELETE = "message_delete";
	public static final String WS_MESSAGE_MESSAGE_SEARCH = "message_search";
	public static final String WS_MESSAGE_GROUP_JOIN_ROOM = "group_join_room";
	public static final String WS_MESSAGE_GROUP_MESSAGE_RECEIVE = "group_message_receive";
	public static final String WS_MESSAGE_CHANGE_USER_PRESENCE = "change_user_presence";
	public static final String WS_MESSAGE_CREATE_ROOM_RESPONSE = "create_room_response";
	
	public static final String WS_MESSAGE_FILE_SHARE =  "file_share";
	public static final String WS_MESSAGE_FILE_REQUEST = "file_request";
	public static final String WS_MESSAGE_GROUP_FILE_REQUEST = "group_file_request";
	public static final String WS_MESSAGE_RECIEVE_FILE= "recieve_file";
	/*add friend*/
	public static final String WS_MESSAGE_FRIEND_REQUEST = "friend_request";
	public static final String WS_MESSAGE_FRIEND_SUBSCRIBED = "friend_subscribed";
	public static final String WS_MESSAGE_FRIEND_UNSUBSCRIBED = "friend_unsubscribed";
	public static final String WS_MESSAGE_ROOM_REQUEST = "room_request";
	public static final String WS_MESSAGE_GROUP_REQUEST = "group_request";
	public static final String WS_MESSAGE_BUDDY_STATUS = "buddy_status";
	
	/* Chat Related Messages */
	public static final String WS_MESSAGE_MESSAGE_RECEIVE = "message_receive";
	public static final String WS_MESSAGE_MESSAGE_SEND = "message_send";
	public static final String WS_MESSAGE_CHAT_MESSAGE_KEY = "message";
	public static final String WS_MESSAGE_CHAT_MESSAGE_ID_KEY = "messageId";
	public static final String WS_MESSAGE_CHAT_REPLY_MESSAGE_BODY = "reply_message_body";
	public static final String WS_MESSAGE_CHAT_EMPTY_MESSAGE = "empty_message_not_allowed";
	public static final String WS_MESSAGE_CHAT_RECEIVER_KEY = "receiver";
	public static final String WS_MESSAGE_CHAT_SENDER_KEY = "sender";
	public static final String WS_MESSAGE_CHAT_MESSAGE_TYPE = "message_type";
	public static final String WS_CHAT_ONE_HISTORY = "history_one";
	public static final String WS_CHAT_ONE_TO_ONE_HISTORY = "one_to_one";
	public static final String WS_GROUP_CHAT_HISTORY_RESPONSE = "group_chat_history";
	public static final String WS_MESSAGE_GET_CHAT_LOG = "chat_log";
	public static final String WS_GROUP_CHAT_LOG_RESPONSE = "chat_log_response";
	
	public static final String WS_MESSAGE_GET_USERS = "get_users";
	public static final String WS_MESSAGE_GET_ROSTERUSERS = "get_roster_users";
	public static final String WS_MESSAGE_GET_TOTAL_USERS = "get_total_users";
	public static final String WS_MESSAGE_TOTAL_USERS = "total_users";
	public static final String WS_MESSAGE_USER_LIST = "user_list";
	public static final String WS_MESSAGE_ROSTER_LIST = "roster_list";
	public static final String WS_MESSAGE_GET_USER_PRESENCE = "get_user_presence";
	
	public static final String WS_MESSAGE_GET_UNKNOWN_USER_FULLNAME = "get_unknown_user_fullname";
	public static final String WS_MESSAGE_UNKNOWN_USER_FULLNAME = "unknown_user_fullname";
	
	public static final String WS_MESSAGE_GET_USERS_FULL_DETAILS = "get_users_full_details";
	public static final String WS_MESSAGE_USERS_FULL_DETAILS = "users_full_details";
	
	public static final String WS_MESSAGE_UPDATE_USER_SETTINGS = "update_user_settings";
	public static final String WS_MESSAGE_UPDATE_USER_SETTINGS_RESPONSE = "update_user_settings_response";
	/*AES enc and dec prop*/
	public static final String WS_MESSAGE_GET_KYES = "op1";
	public static final String ALGORITHM = "AES";
	public static final int AES_128 = 128;
	
	/* PROPERTIES KEYS */
	
	public static final String CAPV_CLIENT_MAIL_HOST = "email.host";
	public static final String CAPV_CLIENT_MAIL_PORT = "email.port";
	public static final String CAPV_CLIENT_MAIL_SENDERNAME = "email.from";
	public static final String CAPV_CLIENT_MAIL_USERNAME = "email.user";
	public static final String CAPV_CLIENT_MAIL_PASSWORD = "email.pass";
	public static final String CAPV_CLIENT_MAIL_SMTP_AUTH = "email.auth";
	public static final String CAPV_CLIENT_MAIL_SSL_ENABLE = "email.ssl.enable";
	
	public static final String CAPV_PLAYBACK_URL = "capv.playback";

	public static final String CAPV_UI_URL = "capv.ui_url";
	public static final String CAPV_MEETING_URL = "capv.meetings.url";
	public static final String CAPV_CLIENT_SUPPORT_EMAIL = "config.support.email";
	public static final String CAPV_UI_CLIENT_NAME = "capv.clientname";
	public static final String CAPV_UI_CLIENT_ANDROID = "config.app.android";
	public static final String CAPV_UI_CLIENT_IOS = "config.app.ios";
	public static final String CAPV_C2_UI_URL = "capv.c2.ui_url";
	public static final String CAPV_C2_Group = "C2_group";
	public static final String VC_SERVER_URL_KEY = "capv.videocalling_endpoint_url";
	public static final String VC_TRYIT_URL_KEY = "capv.try_endpoint_url";
	public static final String CHAT_SERVER_HOST_NAME_KEY = "capv.chatserver.host";
	public static final String CHAT_SERVER_PORT_KEY = "capv.chatserver.port";
	public static final String CHAT_SERVER_SERVICE_KEY = "capv.chatserver.service";
	public static final String CHAT_HISTORY_MAX_RECORDS = "capv.chathistory.maxrecords";
	public static final String CHAT_MAX_HISTORY = "capv.chathistory.max";
	public static final String TRYIT_ROOM_VALIDITY_KEY = "capv.tryitroom.validity";
	public static final String CAPV_TRYIT_URL = "config.tryit.url";
	public static final String REC_TYPE = "rec_type";
	
	public static final String SCHEDULE_CALL = "schedulecall";
	public static final String VC_MSG_SCHEDULE_MESSAGE = "schedule_group_message";
	public static final String VC_MSG_SCHEDULE_MESSAGE_RESPONSE = "schedule_group_message_response";
	public static final String MAX_RETRY = "max_retry";
	public static final String WS_MESSAGE_SCHEDULE_CHAT_MESSAGE_KEY = "schedule_message";
	
	public static final String CLIENT_CONFIG_SERVICE_PATH = "/global/configuration/service";
	
	public static final String VIDEO_RECORDING_SERVICE_PATH = "/video/recording/logtime-stamp";
	
	public static final String CAPV_CHAT_FILE_SEND_TEMP_URL_KEY = "capv.chat.file.send.temp.url";
	public static final String CAPV_CHAT_FILE_RECEIVE_TEMP_URL_KEY = "capv.chat.file.receive.temp.url";
	
	public static final String WS_MESSAGE_CALL_PROGRESS = "call_progress";
	public static final String WS_MESSAGE_CALL_LOG_EVENTS = "call_log_events";
	public static final String WS_MESSAGE_CALL_EVENTS = "call_events";
	public static final String WS_MESSAGE_IS_CALL_IN_PROGRESS = "is_call_in_progress";
	public static final String WS_MESSAGE_CALL_IN_PROGRESS = "call_in_progress";
	public static final String WS_MESSAGE_CALL_REJECT = "call_reject";
	
	public static final String WS_MESSAGE_UPLOAD_PROFILE_PICTURE = "upload_profile_picture";
	public static final String WS_MESSAGE_UPLOAD_PROFILE_PICTURE_RESPONSE = "upload_profile_picture_response";
	
	public static final String WS_MESSAGE_GET_PROFILE_PICTURE = "get_profile_picture";
	public static final String WS_MESSAGE_GET_PROFILE_PICTURE_RESPONSE = "get_profile_picture_response";
	
	public static final String WS_MESSAGE_GET_ROSTER_USERS_PROFILE_PICTURES = "get_roster_users_profile_pictures";
	public static final String WS_MESSAGE_GET_ROSTER_USERS_PROFILE_PICTURES_RESPONSE = "get_roster_users_profile_pictures_response";
	
	public static final String WS_MESSAGE_UPDATE_PROFILE = "update_profile";
	public static final String WS_MESSAGE_UPDATE_PROFILE_RESPONSE = "update_profile_response";
	
	public static final String WS_MESSAGE_GET_USER_DETAILS = "get_user_details";
	public static final String WS_MESSAGE_GET_USER_DETAILS_RESPONSE = "get_user_details_response";
	
	public static final String WS_MESSAGE_ROOM_JOIN_CONFLICT = "room_join_conflict";
	
	public static final String WS_MESSAGE_UPDATE_CALL_STATS = "update_call_stats";
	
	public static final String WS_MESSAGE_MISSED_CALL = "missed_call";
	public static final String WS_MESSAGE_MISSED_CALL_SEEN = "missed_call_seen";
	
	public static final String WS_MESSAGE_MISSED_CALL_SEEN_RESPONSE = "missed_call_seen_response";
	public static final String WS_MESSAGE_MISSED_CALL_RESPONSE = "missed_call_response";
	public static final String WS_MESSAGE_GET_MISSED_CALLS = "get_missed_calls";
	public static final String WS_MESSAGE_GET_MISSED_CALLS_RESPONSE = "get_missed_calls_response";
	
	public static final String WS_MESSAGE_PING = "ping";
	public static final String WS_MESSAGE_PONG = "pong";
	public static final String WS_MESSAGE_GET_CONFIG_PROPS= "get_config_props";
	
	/* Video Calling Messages STATES KEYS */
	public static final String VC_MSG_ONE = "one-one";
	public static final String VC_MSG_ADD_PARTICIPANT = "one-one-addparticipant";
	public static final String VC_MSG_ADD_GROUP_PARTICIPANT = "group-addparticipant";
	public static final String VC_TRY_IT = "tryit";
	public static final String GET_TRY_IT_ROOM = "get_tryit_room";
	public static final String CHECK_TRY_IT_ROOM_USER = "check_tryit_room_user";
	public static final String CHECK_TRY_IT_ROOM_USER_RESPONSE = "check_tryit_room_user_response";
	public static final String TRY_IT_ROOM = "tryit_room";
	public static final String GET_TRY_IT_ROOM_VALIDITY = "get_tryit_room_validity";
	public static final String TRY_IT_ROOM_VALIDITY = "tryit_room_validity";
	public static final String VC_MSG_GROUP = "group";
	public static final String VC_MSG_EXIT = "exit";
	public static final String VC_MSG_RECONNECT = "reconnect";
	
	//Recoded Video fetch
	public static final String WS_MESSAGE_FETCH_VIDEO_RECORDED_EVENTS = "get_Rec_Videos";
	
	
	/* USER STATES KEYS */
	public static final String USER_STATE_ID = "userstatus";
	
	/* CALL TYPE KEYS */
	public static final String CALL_TYPE_GROUP = "group";
	public static final String CALL_TYPE_ONE = "one-one";
	
	public static final String CLIENT_REQUEST_SOURCE_ANDROID_APP = "ANDROID_APP";
	
	public static final String GET_OAUTH_CLIENT_DETAILS_BY_DOMAIN = "domain";
	public static final String GET_OAUTH_CLIENT_DETAILS_BY_CLIENT_ID = "client_id";
	
	public static final Integer XMPP_SERVER_DEFAULT_PORT = 5222;
	
	public static final String SENDMAIL = "sendmail";
	
	public static final String VC_PARTICIPANT_TOGGLE_MEDIA = "vc_participant_toggle_media";
	public static final String GET_PARTICIPANT_MEDIA_STATUS = "get_participant_media_status";
	public static final String PARTICIPANT_MEDIA_STATUS = "participant_media_status";
	public static final String WS_MESSAGE_SCREEN_SHARE_INTIATED="SCREEN_SHARE_INTIATED";
	
	public static final String WS_VIDEO_DELETE_USERS = "delete_rec_video";
	public static final String CAPV_CONFIG_MAX_USER_COUNT="capv.maxusers";
	
	/*Mobile Cloud Messaging Constants*/
	public static final String CAPV_CONFIG_APNS_CERT="apns.cert.path";
	public static final String CAPV_CONFIG_APNS_KEY="apns.cert.key";
	public static final String CAPV_CONFIG_FCM_CERT="fcm.cert.path";
	public static final String CAPV_CONFIG_FCM_KEY="fcm.cert.key";
	
	public static final String CAPV_CONFIG_FETCH_RECORDED_VIDEOS="fetch.recorded.videos";
	public enum UserState {
		
		NOTLOGGEDIN((byte)1, "notloggedin"),
		IDLE((byte)2, "idle"),
		CALLING((byte)3, "calling"),
		INCALL((byte)4, "incall"),
		TERMINATING((byte)5, "terminating"),
		RECEIVING((byte)6, "receiving");
		
		private byte stateId;
		private String state;
		
		private UserState(byte stateId, String state) {
			this.stateId = stateId;
			this.state = state;
		}
		
		public byte getStateId() {
			return this.stateId;
		}
		
		public String getState() {
			return this.state;
		}
		
		public String getStateByStateId(byte stateId) {
			
			String state = null;
			
			switch(stateId) {
				case 1: state = NOTLOGGEDIN.getState();
						break;
						
				case 2: state = IDLE.getState();
						break;
						
				case 3: state = CALLING.getState();
						break;
						
				case 4: state = INCALL.getState();
						break;
						
				case 5: state = TERMINATING.getState();
						break;
						
				case 6: state = RECEIVING.getState();
						break;
			}
			
			return state;
		}
	}
	
	public enum CallState {
		
		STARTED((byte)1, "started"),
		RECONNECT((byte)2, "reconnect"),
		INPROGRESS((byte)3, "inprogress"),
		ENDED((byte)4, "ended"),
		MISSED((byte)5, "missed"),
		MISSED_CALL_SEEN((byte)8, "missed");
		
		private byte stateId;
		private String state;
		
		private CallState(byte stateId, String state) {
			this.stateId = stateId;
			this.state = state;
		}
		
		public byte getStateId() {
			return this.stateId;
		}
		
		public String getState() {
			return this.state;
		}
		
		public String getStateByStateId(byte stateId) {
			
			String state = null;
			
			switch(stateId) {
			
				case 1: state = STARTED.getState();
						break;
						
				case 2: state = RECONNECT.getState();
						break;
						
				case 3: state = INPROGRESS.getState();
						break;
						
				case 4: state = ENDED.getState();
						break;
						
				case 5: state = MISSED.getState();
						break;
				case 8: state = MISSED_CALL_SEEN.getState();
				        break;
			}
			
			return state;
		}
	}
	
	public enum UserServicesConnectStatus {
		
		SUCCESS("success"), FAIL("fail");
		
		private String status;
		
		private UserServicesConnectStatus(String status) {
			this.status = status;
		}
		
		public String getStatus() {
			return this.status;
		}
	}
	
	public enum VideoRecordingStatus {
		
		SUCCESS("success"), FAIL("fail");
		
		private String status;
		
		private VideoRecordingStatus(String status) {
			this.status = status;
		}
		
		public String getStatus() {
			return this.status;
		}
	}
	
	public enum ChatClientAuthStatus {
		
		SUCCESS("success"), FAIL("fail");
		
		private String status;
		
		private ChatClientAuthStatus(String status) {
			this.status = status;
		}
		
		public String getStatus() {
			return this.status;
		}
	}
	
	public enum VideoCallingClientConnectStatus {
		
		SUCCESS("success"), FAIL("fail");
		
		private String status;
		
		private VideoCallingClientConnectStatus(String status) {
			this.status = status;
		}
		
		public String getStatus() {
			return this.status;
		}
	}
	
public enum UserRoomRequestState {
		
		PENDING((byte)1, "pending"),
		ACCEPTED((byte)2, "accepted"),
		REJECTED((byte)3, "rejected"),
		DELETED((byte)4, "deleted");
		
		private byte id;
		private String description;
		private UserRoomRequestState(byte id, String description){
			this.id=id;
			this.description=description;
		}
		
		public byte getStateId() {
			return this.id;
		}
		
		public String getState() {
			return this.description;
		}
}

public enum MessageType {
	
	MESSAGE((byte)1, "message"),
	REPLY((byte)2, "reply");
	
	private byte id;
	private String type;
	private MessageType(byte id, String type){
		this.id=id;
		this.type=type;
	}
	
	public byte getTypeId() {
		return this.id;
	}
	
	public String getType() {
		return this.type;
	}
}

}

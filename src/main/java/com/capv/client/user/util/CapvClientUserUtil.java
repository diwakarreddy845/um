package com.capv.client.user.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.jivesoftware.smackx.disco.packet.DiscoverItems.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.binary.Base64;
import com.capv.client.user.UserChatRoom;
import com.capv.client.user.chat.CapvChatClientConfiguration;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.chat.CapvChatUserRequestProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.um.chat.model.OfMessageArchive;
import com.capv.um.model.ChatHistory;
import com.capv.um.model.OfGroupArchive;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 * <h1> CapV Client User Util </h1>
 * This class is used to define utility methods require to be process user requests
 * 
 * @author ganesh.maganti
 * @version 1.0
 */
public class CapvClientUserUtil {
	
	private static Environment environment;
	
	
	
	//private static CapvChatUserRequestProcessor capvChatUserRequestProcessor;
	
	private final static Gson gson = new GsonBuilder().create();
	
	private static Map<Long, Properties> capvConfigProperties = new HashMap<>();
	
	/**
	 * This method is used to refer org.springframework.core.env.Environment reference 
	 * to access properties defined in properties file 
	 * 
	 * @param environment The environment reference to read properties defined in the current environment
	 */
	/*
	public CapvClientUserUtil(CapvChatUserRequestProcessor capvChatUserRequestProcessor){
		CapvClientUserUtil.capvChatUserRequestProcessor = capvChatUserRequestProcessor;
	}*/
	public static void setPropertiesEnvironment(Environment environment) {
		CapvClientUserUtil.environment = environment;
	}
	
	/**
	 * This method is used to return JsonObject for a given JSON string
	 * 
	 * @param jsonMessage The JSON string
	 * @return returns corresponding JsonObject for a given JSON string
	 */
	public static JsonObject convertToJsonObject(String jsonMessage) {
		return gson.fromJson(jsonMessage, JsonObject.class);
	}
	
	/**
	 * This method is used to return JsonArray for a given JSON string
	 * 
	 * @param jsonMessage The JSON string
	 * @return returns corresponding JsonArray for a given JSON string
	 */
	public static JsonArray convertToJsonArray(String jsonMessage) {
		return gson.fromJson(jsonMessage, JsonArray.class);
	}
	
	/**
	 * This method is used to return JSON string for a given object
	 * 
	 * @param object The object needs to convert JSON string
	 * @return returns corresponding JSON string for a given object
	 */
	public static String convertToJsonString(Object object) {
		return gson.toJson(object);
	}
	
	/**
	 * This method is used to convert an object to a JsonElement
	 * 
	 * @param object	The object is require to convert JsonElement
	 * @param type		The type of object	
	 * 
	 * @return returns converted JsonElement for a given object
	 */
	public static JsonElement convertToJsonElement(Object object, Type type) {
		JsonElement element = gson.toJsonTree(object, type);
		return element;
	}
	
	public static <T> T convertJsonStringToJavaObject(String jsonString, Type objectType) {
		return gson.fromJson(jsonString, objectType);
	}
	
	public static long getTimeDiffInSeconds(Date time1, Date time2) {
		long diffInSeconds = 0l;
		
		if(time1 != null && time2 != null) {
			long diff = time2.getTime() - time1.getTime();
			
//			diffInSeconds = diff / 1000 % 60;
			diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diff);
		}
		
		return diffInSeconds;
	}
	
	/**
	 * This method is used to initialize HttpURLConnection for a given url
	 * 
	 * @param url The url is used to initialize HttpURLConnection
	 * @return returns the HttpURLConnection for a given url
	 */
	private static HttpURLConnection getHttpUrlConnection(String url) {
		
		HttpURLConnection httpConnection = null;
		
		try {
			
			URL obj = new URL(url);
			httpConnection = (HttpURLConnection) obj.openConnection();
			
			//add request header
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return httpConnection;
	}
	
	/**
	 * This method is used to close HttpURLConnection
	 * @param connection The HttpURLConnection reference which is require to close
	 */
	private static void closeHttpUrlConnection(HttpURLConnection connection) {
		if(connection != null)
			connection.disconnect();
	}
	
	/**
	 * This method is used to load the client configuration properties
	 * 
	 * @param clientId The client id require to load the configuration properties
	 */
	public static void loadClientConfigProperties(Long clientId) {
		
		String clientConfigServiceURL;
		
		String clientConfigServicePath = CapvClientUserConstants.CLIENT_CONFIG_SERVICE_PATH;
		
		clientConfigServiceURL = environment.getProperty("capv.client_url") + clientConfigServicePath + "/" + clientId + "/config";
		
		String configResponse = null;
		
		//Send service request using HttpUrlConnection
		
		HttpURLConnection httpConnection = null;
		
		try {
			httpConnection = getHttpUrlConnection(clientConfigServiceURL);

			// optional default is GET
			httpConnection.setRequestMethod("GET");


			int responseCode = httpConnection.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + clientConfigServiceURL);
			System.out.println("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(
			        new InputStreamReader(httpConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			//print result
			System.out.println("Config Response Code::"+ httpConnection.getResponseCode());
			System.out.println("Config Response::"+response.toString());
			
			if(httpConnection.getResponseCode() == 200)
				configResponse = response.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeHttpUrlConnection(httpConnection);
		}
		
		if(configResponse != null && configResponse.length() > 0) {
			JsonObject jsonObject = convertToJsonObject(configResponse);
			
			if(jsonObject.get("result") != null) {
				Properties clientConfigProperties = new Properties();
				JsonArray jsonArray = jsonObject.get("result").getAsJsonArray();
				
				for(int i=0; i < jsonArray.size(); i++) {
					JsonObject configObj = jsonArray.get(i).getAsJsonObject();
					JsonObject configTypeObj = configObj.get("configProperty").getAsJsonObject();
					
					clientConfigProperties.put(configTypeObj.get("name").getAsString(), 
													configObj.get("propValue").getAsString());
				}
				
				System.out.println("Config Properties::"+clientConfigProperties);
				
				capvConfigProperties.put(clientId, clientConfigProperties);
				
				
			}
		}
		
	}
	
	/**
	 * This method used to get client properties for a given client id
	 * 
	 * @param clientId The client id require to get client properties
	 * @return returns the client properties for a given client id
	 */
	private static Properties getClientProperties(Long clientId) {
		
		Properties clientProperties = null;
		
		if(capvConfigProperties.get(clientId) != null)
			clientProperties = capvConfigProperties.get(clientId);
		
		else {
			loadClientConfigProperties(clientId);
			clientProperties = capvConfigProperties.get(clientId);
		}
		
		return clientProperties;
	}
	
	/**
	 * This method is used to return the property value for a given property key defined in a properties file
	 * 
	 * @param configKey The property key 
	 * @return returns the property value for a given property key
	 */
	public static String getConfigProperty(String configKey) {
		return (environment.getProperty(configKey));
	}
	
	/**
	 * This method is used to return client configuration property  for a given client id and configuration key
	 * 
	 * @param clientId	The client id require to get the property value
	 * @param configKey	The configuration key
	 * @return returns the property value for a given client id and configuration key
	 */
	public static String getClientConfigProperty(Long clientId, String configKey) {
		
		Properties clientProperties = getClientProperties(clientId);
		
		return (clientProperties != null ? clientProperties.getProperty(configKey) : null);
	}
	public static boolean getMaxFetch(int maxHistory,String toDate) {
		
	
		Date maxDate=new  Date(Long.parseLong(toDate));
		Date now=new Date(System.currentTimeMillis());
		
		long diff= now.getTime()-maxDate.getTime();
		
		long diffDays =diff / (24 * 60 * 60 * 1000);
		
		if(diffDays>=maxHistory)
			return true;
		else
			return false;
	}
	public static String getOneToOneChatHistoryJSON(Long clientId, String fromUser, 
												String toUser, List<OfGroupArchive> chatHistoryRecords, 
												int maxHistory) {
		
		String chatHistoryJsonString = null;
		
		boolean maxFetch = false;
		
		long lastFetched = 0l;
		
		List<ChatHistory> allChatHistory = new ArrayList<>();
		
		List<ChatHistory> chatHistoryFromUserToUser = getChatHistory(chatHistoryRecords);
		
		allChatHistory.addAll(chatHistoryFromUserToUser);
		
		if(!chatHistoryRecords.isEmpty()) {
			lastFetched = chatHistoryRecords.get(0).getSentDate();
			if(chatHistoryRecords.size() < maxHistory)
				maxFetch = true;
		} else
			maxFetch = true;
		
		JsonObject chatHistoryJsonObj = new JsonObject();
		
		chatHistoryJsonObj.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
								CapvClientUserConstants.WS_CHAT_ONE_TO_ONE_HISTORY);
		Type objType = new TypeToken<List<ChatHistory>>() {}.getType();
		
		chatHistoryJsonObj.addProperty("chatHistoryUser", toUser);
		chatHistoryJsonObj.addProperty("lastFetched", lastFetched);
		chatHistoryJsonObj.addProperty("maxFetch", maxFetch);
		chatHistoryJsonObj.add("chatHistory", CapvClientUserUtil.convertToJsonElement(allChatHistory, objType));
		
		chatHistoryJsonString = CapvClientUserUtil.convertToJsonString(chatHistoryJsonObj);
		
		return chatHistoryJsonString;
	}
	public static String getGroupChatHistoryJSON(String toUser, List<OfGroupArchive> groupChatHistory, int maxHistory) {
		
		String chatHistoryJsonString = null;
		
		long lastFetched = 0l;
		boolean maxFetch = false;
		
		List<ChatHistory> allChatHistory = new ArrayList<>();
		
	
		List<ChatHistory> chatHistoryFromUserToUser = getGroupChatHistory(groupChatHistory);
		
		allChatHistory.addAll(chatHistoryFromUserToUser);
		
		if(!groupChatHistory.isEmpty()) {
			lastFetched = groupChatHistory.get(0).getSentDate();
			if(groupChatHistory.size() < maxHistory)
				maxFetch = true;
		} else
			maxFetch = true;
		
		JsonObject chatHistoryJsonObj = new JsonObject();
		
		chatHistoryJsonObj.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
								CapvClientUserConstants.WS_GROUP_CHAT_HISTORY_RESPONSE);
		Type objType = new TypeToken<List<ChatHistory>>() {}.getType();
		
		chatHistoryJsonObj.addProperty("chatHistoryUser", toUser);
		chatHistoryJsonObj.addProperty("lastFetched", lastFetched);
		chatHistoryJsonObj.add("chatHistory", CapvClientUserUtil.convertToJsonElement(allChatHistory, objType));
		chatHistoryJsonObj.addProperty("maxFetch", maxFetch);
		chatHistoryJsonString = CapvClientUserUtil.convertToJsonString(chatHistoryJsonObj);
		
		return chatHistoryJsonString;
	}
	private static List<ChatHistory> getChatHistory(List<OfGroupArchive> chatHistoryURL) {
		
		List<ChatHistory> chatHistory = new ArrayList<>();
		
		HttpURLConnection httpConnection = null;
		
		try {
			
			String json = new Gson().toJson(chatHistoryURL );
			
			JsonArray chatHistoryResposneArray = convertToJsonArray(json.toString());
			
			for(JsonElement chatHistoryJsonElement :chatHistoryResposneArray) {
				JsonObject chatHistoryJsonObject = chatHistoryJsonElement.getAsJsonObject();
				ChatHistory t=new ChatHistory();
				t.setFrom(chatHistoryJsonObject.get("fromJID").getAsString().substring(0, chatHistoryJsonObject.get("fromJID").getAsString().indexOf('@')));
				t.setTo(chatHistoryJsonObject.get("toJID").getAsString().substring(0, chatHistoryJsonObject.get("toJID").getAsString().indexOf('@')));
				t.setBody(chatHistoryJsonObject.get("body").getAsString());
				t.setMessageId(chatHistoryJsonObject.get("messageID").getAsString());
				t.setIsEdited(chatHistoryJsonObject.get("isEdited").getAsString());
				t.setSentTime(Long.parseLong( chatHistoryJsonObject.get("sentDate").toString()));
				t.setMessage_type(chatHistoryJsonObject.get("message_type").toString());
				if(chatHistoryJsonObject.get("reply_message_body")!=null) {
					t.setReply_message_body(chatHistoryJsonObject.get("reply_message_body").toString());
				}
				if(chatHistoryJsonObject.get("toJID").getAsString().contains("conference"))
				t.setType("group");
				
				chatHistory.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeHttpUrlConnection(httpConnection);
		}
		
		
		return chatHistory;
	}
private static List<ChatHistory> getChatHistoryLog(List<OfMessageArchive> chatHistoryURL) {
		
		List<ChatHistory> chatHistory = new ArrayList<>();
		
		HttpURLConnection httpConnection = null;
		
		try {
			
			String json = new Gson().toJson(chatHistoryURL );
			
			JsonArray chatHistoryResposneArray = convertToJsonArray(json.toString());
			
			for(JsonElement chatHistoryJsonElement :chatHistoryResposneArray) {
				JsonObject chatHistoryJsonObject = chatHistoryJsonElement.getAsJsonObject();
				ChatHistory t=new ChatHistory();
				t.setFrom(chatHistoryJsonObject.get("fromJID").getAsString().substring(0, chatHistoryJsonObject.get("fromJID").getAsString().indexOf('@')));
				t.setTo(chatHistoryJsonObject.get("toJID").getAsString().substring(0, chatHistoryJsonObject.get("toJID").getAsString().indexOf('@')));
				t.setBody(chatHistoryJsonObject.get("body").getAsString());
				t.setSentTime(Long.parseLong( chatHistoryJsonObject.get("sentDate").toString()));
				if(chatHistoryJsonObject.get("toJID").getAsString().contains("conference"))
				t.setType("group");
				chatHistory.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeHttpUrlConnection(httpConnection);
		}
		
		
		return chatHistory;
	}
private static List<ChatHistory> getGroupChatHistory(List<OfGroupArchive> chatHistoryURL) {
		
		List<ChatHistory> chatHistory = new ArrayList<>();
		
		HttpURLConnection httpConnection = null;
		
		try {
			
			String json = new Gson().toJson(chatHistoryURL );
			
			JsonArray chatHistoryResposneArray = convertToJsonArray(json.toString());
			
			for(JsonElement chatHistoryJsonElement :chatHistoryResposneArray) {
				JsonObject chatHistoryJsonObject = chatHistoryJsonElement.getAsJsonObject();
				ChatHistory t=new ChatHistory();
				t.setFrom(chatHistoryJsonObject.get("fromJID").getAsString().substring(0, chatHistoryJsonObject.get("fromJID").getAsString().indexOf('@')));
				t.setTo(chatHistoryJsonObject.get("toJID").getAsString().substring(0, chatHistoryJsonObject.get("toJID").getAsString().indexOf('@')));
				t.setBody(chatHistoryJsonObject.get("body").getAsString());
				t.setMessageId(chatHistoryJsonObject.get("messageID").getAsString());
				t.setIsEdited(chatHistoryJsonObject.get("isEdited").getAsString());
				t.setSentTime(Long.parseLong( chatHistoryJsonObject.get("sentDate").toString()));
				t.setMessage_type(chatHistoryJsonObject.get("message_type").toString());
				if(chatHistoryJsonObject.get("reply_message_body")!=null) {
					t.setReply_message_body(chatHistoryJsonObject.get("reply_message_body").toString());
				}
				if(chatHistoryJsonObject.get("toJID").getAsString().contains("conference"))
				t.setType("group");
				
				chatHistory.add(t);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeHttpUrlConnection(httpConnection);
		}
		
		
		return chatHistory;
	}
	
	public static ClientDetails getOAuthClientDetails(String byString,  String withValue) {
		
		HttpURLConnection httpConnection = null;
		ClientDetails oauthClientDetails = null;
		StringBuffer oauthClientDetailsURL = new StringBuffer(getConfigProperty("capv.client_url"));
		//System.out.print("sharath"+"testing"+oauthClientDetailsURL);
		try {
			
			switch(byString) {
			
				case CapvClientUserConstants.GET_OAUTH_CLIENT_DETAILS_BY_DOMAIN:
				{
					oauthClientDetailsURL.append("/client/getOauthClientDetailsByDomainName/").append(URLEncoder.encode(withValue, CharEncoding.UTF_8));
				}
				break;
				
				case CapvClientUserConstants.GET_OAUTH_CLIENT_DETAILS_BY_CLIENT_ID:
				{
					oauthClientDetailsURL.append("/client/getOauthClientDetailsByClientId/").append(URLEncoder.encode(withValue, CharEncoding.UTF_8));
				}
				break;
				
				default: return null;
			}
			
			String oauthClientDetailsEncodedURL = oauthClientDetailsURL.toString();
			
			httpConnection = getHttpUrlConnection(oauthClientDetailsEncodedURL);
			httpConnection.setDoInput(true);
			httpConnection.setConnectTimeout(5000);
			httpConnection.setReadTimeout(5000);
			//System.out.print("sharath"+"testing"+oauthClientDetailsEncodedURL);
			// optional default is GET
			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			
			if(httpConnection.getResponseCode() == HttpStatus.SC_OK) {
				BufferedReader in = new BufferedReader(
        				new InputStreamReader(httpConnection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				JsonObject oauthClientDetailsRespObj = convertToJsonObject(response.toString());
				System.out.print("sharath"+"testing"+response.toString());
				if(oauthClientDetailsRespObj.get("result") != null) {
					ObjectMapper jsonMapper = new ObjectMapper();
					oauthClientDetails = jsonMapper.readValue(oauthClientDetailsRespObj.get("result").toString(), BaseClientDetails.class);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeHttpUrlConnection(httpConnection);
		}
		
		return oauthClientDetails;
	}
public static String getRecordedVideos(String roomId,String name) {
		
		HttpURLConnection httpConnection = null;
		Long clientId=10001L;
		clientId=Long.parseLong(name.substring(name.lastIndexOf("_")+1, name.length()));
		StringBuffer recVideoURL = new StringBuffer(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CONFIG_FETCH_RECORDED_VIDEOS));
		String recVideoRespObj = "";
		try {
			
			recVideoURL.append("/getRecPlaybackList/").append(URLEncoder.encode(roomId, CharEncoding.UTF_8)+"/").append(URLEncoder.encode(name, CharEncoding.UTF_8));
			
			String recVideoEncodedURL = recVideoURL.toString();
			
			httpConnection = getHttpUrlConnection(recVideoEncodedURL);
			httpConnection.setDoInput(true);
			httpConnection.setConnectTimeout(5000);
			httpConnection.setReadTimeout(5000);
			
			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			
			if(httpConnection.getResponseCode() == HttpStatus.SC_OK) {
				BufferedReader in = new BufferedReader(
        				new InputStreamReader(httpConnection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
			 recVideoRespObj = response.toString();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeHttpUrlConnection(httpConnection);
		}
		
		return recVideoRespObj;
	}
public static String getMeetingsRecordedVideos(String roomId,String name) {
	
	HttpURLConnection httpConnection = null;
	Long clientId=10001L;
	clientId=Long.parseLong(name);
	StringBuffer recVideoURL = new StringBuffer(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CONFIG_FETCH_RECORDED_VIDEOS));
	String recVideoRespObj = "";
	try {
		
		recVideoURL.append("/getMeetingRecordedList/").append(URLEncoder.encode(roomId, CharEncoding.UTF_8)+"/").append(URLEncoder.encode(name, CharEncoding.UTF_8));
		
		String recVideoEncodedURL = recVideoURL.toString();
		
		httpConnection = getHttpUrlConnection(recVideoEncodedURL);
		httpConnection.setDoInput(true);
		httpConnection.setConnectTimeout(5000);
		httpConnection.setReadTimeout(5000);
		
		httpConnection.setRequestMethod("GET");
		httpConnection.setRequestProperty("Content-Type", "application/json");
		
		if(httpConnection.getResponseCode() == HttpStatus.SC_OK) {
			BufferedReader in = new BufferedReader(
    				new InputStreamReader(httpConnection.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
		 recVideoRespObj = response.toString();
			
		}
	} catch (Exception e) {
		e.printStackTrace();
	} finally {
		closeHttpUrlConnection(httpConnection);
	}
	
	return recVideoRespObj;
}
	public static void updateCallStatistics(String updatedStatistics) {
		
		HttpURLConnection httpConnection = null;
		StringBuffer updateStatisticsURL = new StringBuffer(getConfigProperty("capv.client_url"));
		
		try {
			
			updateStatisticsURL = updateStatisticsURL.append("/stats/updateAll");
			
			httpConnection = getHttpUrlConnection(updateStatisticsURL.toString());
			httpConnection.setDoOutput(true);
			httpConnection.setConnectTimeout(5000);
			httpConnection.setReadTimeout(5000);
			
			httpConnection.setRequestMethod("PUT");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			httpConnection.setRequestProperty("Accept", "application/json");
			
			OutputStream outputStream = httpConnection.getOutputStream();
			outputStream.write(updatedStatistics.getBytes());
			outputStream.close();
			
			if(httpConnection.getResponseCode() == HttpStatus.SC_OK) {
				System.out.println("Stats updated");
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeHttpUrlConnection(httpConnection);
		}
		
	}
	
	
	
	public static void main(String[] args) throws Exception {
		/*String regEx = "^(?=.*[a-zA-Z0-9])([a-zA-Z0-9.@_-]{6,20})$";
		
		while (true) {
			BufferedReader br =new BufferedReader(new InputStreamReader(System.in));
			String input = br.readLine();
			
			Pattern pattern = Pattern.compile(regEx);
			Matcher matcher = pattern.matcher(input);
			
			if(matcher.matches()) {
				System.out.println("Valid");
			} else {
				System.out.println("Invalid");
			}
		}*/
		System.out.println(System.currentTimeMillis());
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -100);
		
		String historyStartDate = Long.toString(calendar.getTime().getTime());
		System.out.println(historyStartDate);
   		
	}

	public static String getChatLog(Long clientId , String userJid,CapvChatUserRequestProcessor capvChatUserRequestProcessor ,List<OfMessageArchive> chatHistoryAll) {
        String chatHistoryJsonString = null;
        List<ChatHistory> allChatHistory = new ArrayList<>();
        
        List<String> justRooms = new ArrayList<String>();
		
		StringBuilder joinedRoomsString = new StringBuilder();
		
			List<UserChatRoom> joinedRooms;
			try {
				joinedRooms = capvChatUserRequestProcessor.joinedRooms(clientId);
			
			for(UserChatRoom chatRoom : joinedRooms){
				justRooms.add("'" + chatRoom.getJid()+ "'");
    		}
    		for(String room: justRooms){
    			if(joinedRoomsString.length() != 0 )
    				joinedRoomsString.append(",").append(room);
    			else
    				joinedRoomsString.append(room);
    		}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
    		System.out.println(joinedRoomsString);
		
		try {
			List<ChatHistory> chatHistoryFromUserToUser = getChatHistoryLog(chatHistoryAll);
			allChatHistory.addAll(chatHistoryFromUserToUser);
			
			
			JsonObject chatHistoryJsonObj = new JsonObject();
			
			chatHistoryJsonObj.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
						CapvClientUserConstants.WS_GROUP_CHAT_LOG_RESPONSE);
			Type objType = new TypeToken<List<ChatHistory>>() {}.getType();
			
			chatHistoryJsonObj.add("chatHistory", CapvClientUserUtil.convertToJsonElement(allChatHistory, objType));
			
			chatHistoryJsonString = CapvClientUserUtil.convertToJsonString(chatHistoryJsonObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return chatHistoryJsonString;
	}
	
	public static void shareFileToGroup(JsonObject jsonMessageObj, String userName) {

		try {
			String filename = jsonMessageObj.get("fileName").getAsString();
			String stream = jsonMessageObj.get("stream").getAsString();
			String name = jsonMessageObj.get("to").getAsString();
			boolean more = jsonMessageObj.get("more").getAsBoolean();
			int chunkNum = jsonMessageObj.get("chunkNum").getAsInt();
			String file_id = UUID.randomUUID().toString();
			File file;

			String tempFilePath = CapvClientUserUtil.getConfigProperty(
					CapvClientUserConstants.
					CAPV_CHAT_FILE_RECEIVE_TEMP_URL_KEY);

			file = new File(tempFilePath + "/" + filename);
			byte[] bytes = Base64.decodeBase64(stream);		

			long length = bytes.length;
			if(chunkNum==1 && file.exists()){
				file.delete();
			}


			FileOutputStream out = new FileOutputStream(file,true);

			for(int i=0;i<length;i++){
				out.write(bytes[i]);
			}
			out.flush();
			out.close();

			//FileUtils.writeByteArrayToFile( file, bytes );
			if(more==false){
				if(jsonMessageObj.get("type").getAsString() != null){
					File recive=new File(tempFilePath + "/" + filename);
					FileInputStream fileInputStreamReader = new FileInputStream(recive);

					//byte[] bytes1 = new byte[(int)recive.length()];
					//fileInputStreamReader.read(bytes1);
					
					
					byte[] chunk = new byte[16384];
					int chunkLen = 0;
					long currentChunk = 1;
					double fileSize = recive.length();
					long chunkSize = 16384;
					long totalChunks = (long) Math.ceil(fileSize/chunkSize);
					String encodedBase64 = "";
					while ((chunkLen = fileInputStreamReader.read(chunk)) != -1) {
						encodedBase64 = new String(Base64.encodeBase64(chunk));

						//String encodedBase64 = new String(Base64.encodeBase64(bytes1));
						Path path = FileSystems.getDefault().getPath(tempFilePath, filename);
						String mimeType = Files.probeContentType(path);
						JsonObject messageToSend = new JsonObject();
						messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
								CapvClientUserConstants.WS_MESSAGE_GROUP_FILE_REQUEST);
						messageToSend.addProperty("incommingfile", name);
						messageToSend.addProperty("filename",filename );
						messageToSend.addProperty("mimetype", mimeType);
						messageToSend.addProperty("file", encodedBase64);
						messageToSend.addProperty("more", currentChunk!=totalChunks);
						messageToSend.addProperty("chunkNum", currentChunk);
						messageToSend.addProperty("fileSenderUsername", userName);
						messageToSend.addProperty("fileId",file_id );
						String userMessage	= CapvClientUserUtil.convertToJsonString(messageToSend);


						if(jsonMessageObj.get("occupants") != null){
							JsonArray occ= jsonMessageObj.get("occupants").getAsJsonArray();
							for(JsonElement oc : occ){
								System.out.println(oc.getAsString());
								if(!oc.equals(userName))
									CapvChatUserMessageProcessor.sendChatClientMessageToUser(oc.getAsString(), userMessage);
							}
						} else{
							CapvChatUserRequestProcessor capvChatUserRequestProcessor = CapvChatClientManagerRegistry
									.getCapvChatUserRequestProcessorByUserName(userName);
							CapvChatClientConfiguration capvChatClientConfiguration = CapvChatClientManagerRegistry
									.getCapvChatClientManagerByUser(userName)
									.getChatClientConfiguration();
							String service = capvChatClientConfiguration.getService();
							List<Item> occupants = capvChatUserRequestProcessor.getOccupantsByRoom(name+"@conference."+service);
							for(Item item : occupants){
								if(!item.getEntityID().toString().split("/")[1].equals(userName))
									CapvChatUserMessageProcessor.sendChatClientMessageToUser(item.getEntityID().toString().split("/")[1], userMessage);
							}
						}

						currentChunk++;
						Thread.sleep(30);
					}

					if(fileInputStreamReader != null){
						fileInputStreamReader.close();
					}
				}
			}

		}catch(Throwable e){
			e.printStackTrace();
		}}
	
	
	public static String getValidWebCastUser(String roomId,String name,String userName) {
		
		HttpURLConnection httpConnection = null;
		Long clientId=10136L;
		clientId=Long.parseLong(name);
		StringBuffer recVideoURL = new StringBuffer(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CONFIG_FETCH_RECORDED_VIDEOS));
		String recVideoRespObj = "";
		try {
			
			recVideoURL.append("/statistics/getParticipantsByRoom/").append(URLEncoder.encode(roomId, CharEncoding.UTF_8)+"/");
			
			String recVideoEncodedURL = recVideoURL.toString();
			
			httpConnection = getHttpUrlConnection(recVideoEncodedURL);
			httpConnection.setDoInput(true);
			httpConnection.setConnectTimeout(5000);
			httpConnection.setReadTimeout(5000);
			
			httpConnection.setRequestMethod("GET");
			httpConnection.setRequestProperty("Content-Type", "application/json");
			
			if(httpConnection.getResponseCode() == HttpStatus.SC_OK) {
				BufferedReader in = new BufferedReader(
	    				new InputStreamReader(httpConnection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
			 recVideoRespObj = response.toString();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeHttpUrlConnection(httpConnection);
		}
		
		return recVideoRespObj;
	}
	
	
}

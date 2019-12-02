package com.capv.um.spring.aop;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.websocket.CapvWebSocketEndpoint;
import com.capv.um.model.ApiCount;
import com.capv.um.model.AuditLog;
import com.capv.um.service.ApiCountService;
import com.capv.um.service.AuditlogService;
import com.capv.um.util.CapvUtil;
import com.google.gson.JsonObject;
@Component
@Aspect
public class LoggerConfig {

	 @Autowired
	 AuditlogService auditlogService;
	 
	 @Autowired
	 ApiCountService apiCountService;
	 
	public static  Map<Long, List<ApiCount>> auditMap=new HashMap<Long, List<ApiCount>>();
	 
	 long lastSaveTimeMillis=System.currentTimeMillis()+60 * 60 * 1000;
	 
	@Before("execution(public void org.springframework.web.socket.WebSocketHandler.handleMessage(..))")
	public void logAfter(final JoinPoint jp) {
		//System.out.println("Logger Config");
		
		   Long clientId=null;
		   if(jp.getTarget() instanceof CapvWebSocketEndpoint) {
			   
			   Object[] args = jp.getArgs();
			   
			   WebSocketSession session	= null;
			   TextMessage message		= null;
			   
			   for(Object arg :args)
				   if(arg instanceof WebSocketSession)
					   session = (WebSocketSession)arg;
			   		else if(arg instanceof TextMessage)
			   			message = (TextMessage)arg;
			   
			   if(session != null && message != null) {
				  String messagePayload = message.getPayload();
				  
				  if(messagePayload != null) {
					  JsonObject messageObj = null;
					  try {
						  	UserSession userSession = UserRegistry.getUserSessionBySessionId(session.getId());
						  	clientId=userSession.getClientId();
						  	String key = userSession.getKey(); 
						  	String initVector = userSession.getIv();
						  	if(key!=null&&initVector!=null) {
						  		messagePayload=CapvUtil.decrypt(key, initVector,message.getPayload());
						  		if(messagePayload!=null) {
						  			messageObj = CapvClientUserUtil.convertToJsonObject(messagePayload);
						  		}else {
						  			messagePayload=message.getPayload();
						  			messageObj=CapvClientUserUtil.convertToJsonObject(message.getPayload());
						  		}
						  	}else {
						  		messageObj=CapvClientUserUtil.convertToJsonObject(message.getPayload());
						  	}
					 } catch (Exception e){}
					 
					 if(messageObj != null ) {
						 
					/*	 String messageType = messageObj.get("capv_msg").getAsString();*/
						 String messageType =null;
						 
                        try {
                        	  messageType = messageObj.get("id").getAsString();
						} catch (Exception e) {
							try {
								messageType = messageObj.get("capv_msg").getAsString();
							} catch (Exception e2) {
								messageType="";
							}
						}
						 
						if(messageType.equals(CapvClientUserConstants.CALL_TYPE_ONE) ||  messageType.equals(CapvClientUserConstants.CALL_TYPE_GROUP) ||
								 messageType.equals(CapvClientUserConstants.WS_MESSAGE_RECORD_START) || messageType.equals(CapvClientUserConstants.WS_MESSAGE_RECORD_STOP)) {
							 String userName = "";
							 String room = "";
							 AuditLog logger  = new AuditLog( session.getUri().getPath(), messagePayload, userName, 
									 							room, new Date(), session.getRemoteAddress().toString(),messageType);
							 auditlogService.saveLog(logger);
						}
						 
						 if(messageType.equals(CapvClientUserConstants.CALL_TYPE_ONE) ||  messageType.equals(CapvClientUserConstants.CALL_TYPE_GROUP) ||
								 messageType.equals(CapvClientUserConstants.WS_MESSAGE_RECORD_START) || messageType.equals(CapvClientUserConstants.WS_MESSAGE_RECORD_STOP)
								 ||messageType.equals(CapvClientUserConstants.WS_MESSAGE_MESSAGE_EDIT)
								 ||messageType.equals(CapvClientUserConstants.WS_MESSAGE_MESSAGE_DELETE)
								 ||messageType.equals(CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEARCH)
								 ||messageType.equals(CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEND)
								 ||messageType.equals(CapvClientUserConstants.WS_CHAT_ONE_HISTORY)
								 ||messageType.equals(CapvClientUserConstants.WS_CHAT_ONE_TO_ONE_HISTORY)
								 ||messageType.equals(CapvClientUserConstants.WS_MESSAGE_SEARCH_USER )
								 ||messageType.equals(CapvClientUserConstants.VC_MSG_ADD_PARTICIPANT )
								 ||messageType.equals(CapvClientUserConstants.VC_MSG_EXIT )
								 ||messageType.equals(CapvClientUserConstants.WS_MESSAGE_RECORD_STOP)
								 ) {
							 
							if(clientId!=null){
								Boolean flag=false;
								if(auditMap.containsKey(clientId)){
									
									List<ApiCount> apiList=auditMap.get(clientId);
									for (ApiCount apiCount : apiList) {
										if(apiCount.getUriName().equals(messageType)){
											apiCount.setCount(apiCount.getCount()+1);
											flag=true;
											break;
										}
									}
									
									if(!flag){
										ApiCount api=new ApiCount();
										api.setClientId(clientId);
										api.setUriName(messageType);
										api.setCount(1);
										apiList.add(api);
									}
									
									auditMap.put(clientId, apiList);
									
								}else{
									List<ApiCount> apiList=new ArrayList<ApiCount>();
									ApiCount apiCount=new ApiCount(); 
									apiCount.setClientId(clientId);
									apiCount.setUriName(messageType);
									apiCount.setCount(1);
									apiList.add(apiCount);
									auditMap.put(clientId, apiList);
									
								}
							}
							 
						 }
						 
						 
					 }
				  }
			   }
			   
		   }
	}
	


}

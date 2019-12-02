package com.capv.client.user.websocket;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.capv.client.user.UserRegistry;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.video_calling.Capv_VC_WS_ClientEndpoint;
import com.google.gson.JsonObject;

public class CapvSystemStatisticsWebSocketEndpoint extends TextWebSocketHandler {
	
	
	Set<WebSocketSession> sessions = new HashSet<>();
	Thread systemStatsThread = null;
	
	public CapvSystemStatisticsWebSocketEndpoint() {
		initializeSystemStatsThread();
	}
	
	private void initializeSystemStatsThread() {
		systemStatsThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					for(WebSocketSession session:sessions) {
						String systemStatistics = getSystemStatistics();
						TextMessage textMessage = new TextMessage(systemStatistics);
						try {
							session.sendMessage(textMessage);
						} catch (Exception e) {}
					}
					try {
						Thread.sleep(1000);
					} catch (Exception e) {}
				}
				
			}
		});
	}
	
	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		try {
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
		
		if(systemStatsThread == null)
			initializeSystemStatsThread();
		
		if(systemStatsThread.getState().equals(State.NEW) || 
				systemStatsThread.getState().equals(State.TERMINATED))
			systemStatsThread.start();
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		
		System.out.println("Session closed. Close status::"+status.getCode()+"\t Close Reason::"+status.getReason());
		
		sessions.remove(session);
	}
	
	private static String getSystemStatistics(){
		
		JsonObject systemStatistics = new JsonObject();
		
		MemoryMXBean memoryStats = ManagementFactory.getMemoryMXBean();
		OperatingSystemMXBean osStats = ManagementFactory.getOperatingSystemMXBean();
		
		systemStatistics.addProperty("HeapUsage", (memoryStats.getHeapMemoryUsage().getUsed()/1024/1024)+"MB");
		systemStatistics.addProperty("HeapCommittedSize", (memoryStats.getHeapMemoryUsage().getCommitted()/1024/1024)+"MB");
		systemStatistics.addProperty("MaxHeapSize", (memoryStats.getHeapMemoryUsage().getMax()/1024/1024)+"MB");
		systemStatistics.addProperty("NonHeapUsage", (memoryStats.getNonHeapMemoryUsage().getUsed()/1024/1024)+"MB");
		systemStatistics.addProperty("NonHeapCommittedSize", (memoryStats.getNonHeapMemoryUsage().getCommitted()/1024/1024)+"MB");
		systemStatistics.addProperty("MaxNonHeapSize", (memoryStats.getNonHeapMemoryUsage().getMax()/1024/1024)+"MB");
		systemStatistics.addProperty("ObjectsToBeFinalize", memoryStats.getObjectPendingFinalizationCount());
		systemStatistics.addProperty("OperatingSystem", osStats.getName());
		systemStatistics.addProperty("OperatingSystemVersion", osStats.getVersion());
		systemStatistics.addProperty("OperatingSystemArchitecture", osStats.getArch());
		systemStatistics.addProperty("ProcessorsCount", osStats.getAvailableProcessors());
		systemStatistics.addProperty("LoadAverage", osStats.getSystemLoadAverage());
		
		systemStatistics.addProperty("UserSessions", UserRegistry.getActiveSessionsCount());
		systemStatistics.addProperty("SmackConnections", CapvChatClientManagerRegistry.getActiveConnectionsCount());
		systemStatistics.addProperty("VCConnections", Capv_VC_WS_ClientEndpoint.getActiveVideoCallSessionsCount());
		
		return systemStatistics.toString();
		
	}
	
}

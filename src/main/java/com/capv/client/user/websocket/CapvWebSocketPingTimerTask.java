package com.capv.client.user.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import com.capv.client.user.UserSession;

public class CapvWebSocketPingTimerTask extends TimerTask {
	
	private static final Logger log = LoggerFactory.getLogger(CapvWebSocketPingTimerTask.class);
	
	private UserSession userSession;
	
	public CapvWebSocketPingTimerTask(UserSession userSession) {
		this.userSession = userSession;
	}

	@Override
	public void run() {
		/*try {
			WebSocketSession session = userSession.getWebSocketSession();
			String pingString = "capV websocket pings"; 
			ByteBuffer pingData = ByteBuffer.allocate(pingString.getBytes().length); 
			pingData.put(pingString.getBytes()).flip();
			PingMessage pingMessage = new PingMessage(pingData);
			session.sendMessage(pingMessage);
			try {
				Thread.sleep(7000);
			} catch (Exception e){}
			long currentTimestamp = System.currentTimeMillis();
			long lastPongMessageReceived = userSession.getLastPongMessageReceived();
			
			if((currentTimestamp - lastPongMessageReceived) > 10000)
				session.close();
		} catch (IOException ioe) {
			log.error("IO exception raised while ping the websocket. Reason: {}", ioe.getMessage());
		}*/
	}

}

package com.capv.client.user.video_calling;

import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig.Configurator;

public class Capv_VC_ClientEndpointConfigurator extends Configurator {
	
	private Map<String, String> customHeadersMap;
	
	public Capv_VC_ClientEndpointConfigurator(Map<String, String> customHeadersMap){
		this.customHeadersMap = customHeadersMap;
	}
	
	public void beforeRequest(Map<String, List<String>> headers) {
		
		if(customHeadersMap != null && customHeadersMap.size() > 0) {
			if(customHeadersMap.get("Origin") != null) {
				headers.get("Origin").clear();
				headers.get("Origin").add(customHeadersMap.get("Origin"));
			}
		}
    }
	
}

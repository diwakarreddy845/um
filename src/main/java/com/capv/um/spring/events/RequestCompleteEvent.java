package com.capv.um.spring.events;

import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.RequestHandledEvent;

@Component
public class RequestCompleteEvent implements ApplicationListener<RequestHandledEvent> {

	@Override
	public void onApplicationEvent(RequestHandledEvent event) {
		
		event.getSource();
		
	}

}
